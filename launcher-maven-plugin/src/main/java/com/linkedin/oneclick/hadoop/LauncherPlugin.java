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


import com.google.common.collect.Sets;
import com.linkedin.oneclick.utils.Utils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.slf4j.impl.StaticLoggerBinder;


@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.PACKAGE)
@Execute(phase=LifecyclePhase.PACKAGE)
public class LauncherPlugin extends AbstractMojo
{
  @Component
  protected MavenProject project;

  @Parameter(property = "descriptorFile", defaultValue = "${project.build.directory}/hadoop-runner.json")
  protected File descriptorFile;

  @Parameter(property = "excludes")
  protected List<String> excludes;

  @Parameter(property = "mainClass")
  protected String mainClass;

  @Parameter(property = "artifactFile", defaultValue = "${project.build.directory}/${project.build.finalName}")
  protected File artifactFile;

  @Parameter(property = "deployment", required = true)
  protected Deployment deployment;

  @Parameter(property = "params")
  protected List<String> params;

  @Parameter(property = "commands")
  protected Map<String, String> commands;

  public void execute()
      throws MojoExecutionException
  {
    try
    {
      // See https://code.google.com/p/slf4j-maven-plugin-log/
      StaticLoggerBinder.getSingleton().setLog(getLog());

      LauncherConfig config= new LauncherConfig();
      buildConfig(config);
      showConfig(config);

      Launcher launcher= new Launcher(config);
      launcher.run();
    }
    catch (IOException e)
    {
      throw new MojoExecutionException("Executing:", e);
    }
  }

  static class Excludes extends LinkedList<LauncherConfig.Exclude> {
     public boolean matches(Artifact check) {
       for(LauncherConfig.Exclude exclude : this) {
         if (exclude.matches(check))
           return true;
       }
       return false;
     }
  }

  private void buildConfig(LauncherConfig config)
  {
    LauncherConfig.Artifact mainArtifact= new LauncherConfig.Artifact();
    convert(project.getArtifact(), mainArtifact);
    if (mainArtifact.getFile()==null)
      mainArtifact.setFile(new File(artifactFile.getAbsolutePath() + "." + project.getPackaging()));
    config.setArtifact(mainArtifact);
    Excludes excludesObject= new Excludes();
    if (excludes!=null) {
      for (String excludeString: excludes) {
        excludesObject.add(new LauncherConfig.Exclude(excludeString));
      }
      config.setExcludes(excludes);   // Not sure why we need it but...just in case
    }
    if (params!=null)
      config.setRunParams(params);
    List<LauncherConfig.Artifact> dependencies= new ArrayList<LauncherConfig.Artifact>();
    for (Artifact dependency : project.getArtifacts())
    {
      if (INCLUDED_SCOPES.contains(dependency.getScope())) {
        if (excludesObject.matches(dependency)) {
          getLog().debug("Rejected by excludes: " + dependency);
        } else {
          LauncherConfig.Artifact destDependency= new LauncherConfig.Artifact();
          convert(dependency, destDependency);
          dependencies.add(destDependency);
        }
      }
    }
    if (commands!=null && !commands.isEmpty())
      config.setCommands(commands);
    config.setDependencies(dependencies);
    config.setMainClass(mainClass);
    config.setDeployment(deployment);
  }

  static Set<String> INCLUDED_SCOPES= Sets.newHashSet("compile", "runtime");

  private void showConfig(LauncherConfig config) throws IOException
  {
    StringWriter writer= new StringWriter();
    writeJson(config, writer);
    getLog().debug("Descriptor:" + writer.toString());
  }

  void writeJson(LauncherConfig config, Writer writer) throws IOException
  {
    ObjectMapper mapper= new ObjectMapper();
    ObjectWriter jsonWriter = mapper.defaultPrettyPrintingWriter();
    jsonWriter.writeValue(writer, config);
  }

  void writeJson(LauncherConfig config, File output) throws IOException
  {
    FileWriter fileWriter= new FileWriter(output);
    writeJson(config, fileWriter);
    fileWriter.close();
  }

  static void convert(Artifact src, LauncherConfig.Artifact dest)
  {
    dest.setGroupId(src.getGroupId());
    dest.setArtifactId(src.getArtifactId());
    dest.setVersion(src.getVersion());
    dest.setFile(src.getFile());
    dest.setScope(src.getScope());
  }

  static void convert(LauncherConfig.Artifact src, Artifact dest)
  {
    dest.setGroupId(src.getGroupId());
    dest.setArtifactId(src.getArtifactId());
    dest.setVersion(src.getVersion());
    dest.setFile(src.getFile());
    dest.setScope(src.getScope());
  }
}
