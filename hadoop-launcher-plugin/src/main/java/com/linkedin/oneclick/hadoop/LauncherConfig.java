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


import java.io.File;
import java.util.List;
import org.apache.maven.artifact.Artifact;

public class LauncherConfig
{
  Artifact artifact;
  List<Artifact> dependencies;
  List<String> excludes;
  List<String> runParams;
  String mainClass;
  Deployment deployment;

  public Artifact getArtifact()
  {
    return artifact;
  }

  public void setArtifact(Artifact artifact)
  {
    this.artifact = artifact;
  }

  public List<Artifact> getDependencies()
  {
    return dependencies;
  }

  public void setDependencies(List<Artifact> dependencies)
  {
    this.dependencies = dependencies;
  }

  public List<String> getExcludes()
  {
    return excludes;
  }

  public void setExcludes(List<String> excludes)
  {
    this.excludes = excludes;
  }

  public String getMainClass()
  {
    return mainClass;
  }

  public void setMainClass(String mainClass)
  {
    this.mainClass = mainClass;
  }

  public Deployment getDeployment()
  {
    return deployment;
  }

  public void setDeployment(Deployment deployment)
  {
    this.deployment = deployment;
  }

  public List<String> getRunParams()
  {
    return runParams;
  }

  public void setRunParams(List<String> runParams)
  {
    this.runParams = runParams;
  }

  public static class Artifact {
    String groupId;
    String artifactId;
    String version;
    String scope;
    File file;

    public String getGroupId()
    {
      return groupId;
    }

    public void setGroupId(String groupId)
    {
      this.groupId = groupId;
    }

    public String getArtifactId()
    {
      return artifactId;
    }

    public void setArtifactId(String artifactId)
    {
      this.artifactId = artifactId;
    }

    public String getVersion()
    {
      return version;
    }

    public void setVersion(String version)
    {
      this.version = version;
    }

    public File getFile()
    {
      return file;
    }

    public void setFile(File file)
    {
      this.file = file;
    }

    public String getScope()
    {
      return scope;
    }

    public void setScope(String scope)
    {
      this.scope = scope;
    }
  }

  public static class Exclude
  {
    String group;
    String artifact;
    // TODO String version;

    public Exclude(String param)
    {
      String[] parsed= param.split(":");
      if (parsed[0].length() > 0)
        group= parsed[0];
      if (parsed.length > 1 && (parsed[1].length() > 0 && !parsed[1].startsWith("*")))
        artifact= parsed[1];
    }

    public boolean matches(org.apache.maven.artifact.Artifact check)
    {
      if (group!=null && check.getGroupId().equals(group)) {
        if (artifact==null)
          return true;
        if (artifact.equals(check.getArtifactId()))
          return true;
      }
      return false;
    }
  }
}
