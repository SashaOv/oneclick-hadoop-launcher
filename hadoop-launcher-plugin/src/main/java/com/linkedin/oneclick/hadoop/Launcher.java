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

import com.google.common.collect.Lists;
import com.linkedin.oneclick.utils.CommandBuffer;
import com.linkedin.oneclick.utils.ShellProcess;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher
{
  static Logger log = LoggerFactory.getLogger(Launcher.class);

  LauncherConfig config;
  String jobDir;

  public Launcher(LauncherConfig config)
  {
    this.config = config;
    this.jobDir = "~/" + config.getArtifact().getArtifactId();
  }

  public void run()
  {
    deploy();
    execute();
  }

  static final String LOG_LEVEL_OPTION = "LogLevel=Error";
  static final String RSYNC_SSH_COMMAND = "ssh -o " + LOG_LEVEL_OPTION;

  /*
    Remote job directory layout
    ${jobdir} = ~/job/${job-id}
      lib/ -- dependencies
      work/ -- work directory

    Command: cd ${jobdir}/work; HADOOP_CLASSPATH=../bin/lib/* hadoop jar ../bin/${job-main-jar} ${class-name}
   */

  void deploy()
  {
    CommandBuffer mkdir = new CommandBuffer("mkdir", "-p", jobDir + "/lib", jobDir + "/work");
    new ShellProcess("ssh", "-o", LOG_LEVEL_OPTION, config.getDeployment().getHost(), mkdir.toString()).run();
    new ShellProcess("rsync", "-azv", "-e", RSYNC_SSH_COMMAND,
                     config.getArtifact().getFile().getAbsolutePath(),
                     config.getDeployment().getHost() + ":" + jobDir + "/lib/").run();

    List<String> depsCommand = Lists.newArrayList("rsync", "-azv", "-e", RSYNC_SSH_COMMAND);
    Set<String> depNames = new HashSet<String>();
    for (LauncherConfig.Artifact dep : config.getDependencies()) {
      String fileName = dep.getFile().getName();
      boolean alreadyExisted = !depNames.add(fileName);
      if (alreadyExisted)
        log.warn("Duplicate dependency name:" + fileName);
      depsCommand.add(dep.getFile().getAbsolutePath());
    }
    depsCommand.add(config.getDeployment().getHost() + ":" + jobDir + "/lib/");
    new ShellProcess(depsCommand).run();
  }

  void execute()
  {
    CommandBuffer launchCommand = new CommandBuffer("cd", jobDir + "/work ;");
    launchCommand.add("HADOOP_CLASSPATH=../lib/*");
    launchCommand.add("hadoop jar");
    launchCommand.add("../lib/" + config.getArtifact().getFile().getName());
    launchCommand.add(config.getMainClass());
    List<String> runParams = config.getRunParams();
    if (runParams != null) {
      for (String param : runParams)
        launchCommand.add(param);
    }
    new ShellProcess("ssh", "-o", LOG_LEVEL_OPTION, config.getDeployment().getHost(), launchCommand.toString()).run();
  }

  public static void main(String[] args) throws IOException
  {
    String configPath = args[0];
    ObjectMapper mapper = new ObjectMapper();
    LauncherConfig testConfig = mapper.readValue(new FileInputStream(configPath), LauncherConfig.class);
    Launcher launcher = new Launcher(testConfig);
    launcher.run();
  }
}
