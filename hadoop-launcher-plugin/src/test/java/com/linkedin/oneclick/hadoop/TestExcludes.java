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


/**
 * @author sovsankin@linkedin.com
 */
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
