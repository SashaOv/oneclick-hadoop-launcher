package com.linkedin.oneclick.hadoop;


import com.linkedin.oneclick.hadoop.LauncherConfig;
import java.io.File;
import java.io.IOException;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.Test;
import static org.testng.Assert.*;


/**
 * @author sovsankin@linkedin.com
 */
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
