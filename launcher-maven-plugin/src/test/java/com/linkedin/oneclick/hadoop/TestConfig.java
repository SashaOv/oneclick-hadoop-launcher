/*
   Copyright (c) 2013 LinkedIn Corp.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.linkedin.oneclick.hadoop;


import com.linkedin.oneclick.hadoop.LauncherConfig;
import java.io.File;
import java.io.IOException;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class TestConfig
{
  @Test public void testJsonRoundTrip() throws IOException
  {
    ObjectMapper mapper= new ObjectMapper();
    LauncherConfig config= mapper.readValue(getClass().getResourceAsStream("/hadoop-runner.json"), LauncherConfig.class);
    String configString= mapper.writeValueAsString(config);
    LauncherConfig config1= mapper.readValue(configString, LauncherConfig.class);
    assertEquals(configString, mapper.writeValueAsString(config1));
    config1.getDependencies().get(1).file= new File("testFile");
    assertNotEquals(configString, mapper.writeValueAsString(config1));
  }
}
