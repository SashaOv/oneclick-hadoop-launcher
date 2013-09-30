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


import com.linkedin.oneclick.utils.OCException;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class TestExcludes
{
  @Test public void testExcludes()
  {
    LauncherConfig.Artifact dep1org= fromResource("/dep1.json", LauncherConfig.Artifact.class);
    String nullString= null;
    String emptyString= "";
    ArtifactHandler nullAh= null;
    Artifact dep1= new DefaultArtifact("groupId", emptyString, emptyString, emptyString, emptyString, emptyString, nullAh);
    LauncherPlugin.convert(dep1org, dep1);
    LauncherPlugin.Excludes excludes= new LauncherPlugin.Excludes();
    excludes.add(new LauncherConfig.Exclude("org.apache.hadoop:*"));
    excludes.add(new LauncherConfig.Exclude("org.mortbay.jetty:*"));
    excludes.add(new LauncherConfig.Exclude("org.mortbay.jetty:*"));
    excludes.add(new LauncherConfig.Exclude("tomcat:*"));
    assertTrue(excludes.matches(dep1));
  }


  public static<T> T fromResource(String name, Class<T> klass)
  {
    try {
      ObjectMapper mapper= new ObjectMapper();
      return mapper.readValue(TestExcludes.class.getResourceAsStream(name), klass);
    } catch (IOException e) {
      throw OCException.unchecked(e);
    }
  }
}
