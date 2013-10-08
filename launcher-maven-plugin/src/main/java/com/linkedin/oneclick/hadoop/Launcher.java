/*
 * Copyright (c) 2013 LinkedIn Corp.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.linkedin.oneclick.hadoop;

import com.google.common.collect.Lists;
import com.linkedin.oneclick.utils.CommandBuffer;
import com.linkedin.oneclick.utils.Customizer;
import com.linkedin.oneclick.utils.OCException;
import com.linkedin.oneclick.utils.ShellProcess;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher
{
  static Logger log = LoggerFactory.getLogger(Launcher.class);

  LauncherConfig config;
  String jobDir;
  Customizer customizer;

  public Launcher(LauncherConfig config)
  {
    try {
      this.config = config;
      this.jobDir = "~/" + config.getArtifact().getArtifactId();
      this.customizer= new Customizer();
      Properties commandProperties= new Properties();
      commandProperties.load(getClass().getResourceAsStream("/commands.properties"));
      for(String variable : commandProperties.stringPropertyNames())
        customizer.set(variable, commandProperties.getProperty(variable));
      if (config.hasCommands()) {
        Map<String, String> configuredCommands= config.getCommands();
        for(Map.Entry<String, String> entry : configuredCommands.entrySet())
          customizer.set(entry.getKey(), entry.getValue());
      }
    } catch (IOException e) {
      throw OCException.unchecked(e);
    }
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
    customizer.set("remote.host", config.getDeployment().getHost());
    customizer.set("remote.command",  new CommandBuffer("mkdir", "-p", jobDir + "/lib", jobDir + "/work").toString());
    new ShellProcess(customizer.twoPhaseEvaluate("${ssh.run}")).run();

    List<String> rsyncCommand= Lists.newArrayList(customizer.twoPhaseEvaluate("${rsync.start}"));
    rsyncCommand.add(config.getArtifact().getFile().getAbsolutePath());
    Set<String> depNames = new HashSet<String>();
    for (LauncherConfig.Artifact dep : config.getDependencies()) {
      String fileName = dep.getFile().getName();
      boolean alreadyExisted = !depNames.add(fileName);
      if (alreadyExisted)
        log.warn("Duplicate dependency name:" + fileName);
      rsyncCommand.add(dep.getFile().getAbsolutePath());
    }
    rsyncCommand.add(config.getDeployment().getHost() + ":" + jobDir + "/lib/");
    new ShellProcess(rsyncCommand).run();
  }

  void execute()
  {
    CommandBuffer launchCommand = new CommandBuffer("cd", jobDir + "/work ;");
    StringBuffer classPath= new StringBuffer("HADOOP_CLASSPATH=");
    boolean first= true;
    for(LauncherConfig.Artifact dep : config.getDependencies()) {
      if (first)
        first= false;
      else
        classPath.append(":");
      classPath.append("../lib/");
      classPath.append(dep.getFile().getName());
    }
    launchCommand.add(classPath.toString());
    launchCommand.add("hadoop jar");
    launchCommand.add("../lib/" + config.getArtifact().getFile().getName());
    launchCommand.add(config.getMainClass());
    List<String> runParams = config.getRunParams();
    if (runParams != null) {
      for (String param : runParams)
        launchCommand.add(param);
    }
    customizer.set("remote.command", launchCommand.toString());
    new ShellProcess(customizer.twoPhaseEvaluate("${ssh.run}")).run();
  }

  /**
   * This entry point can be used for testing, debugging and troubleshooting
   */
  public static void main(String[] args) throws IOException
  {
    String configPath = args[0];
    ObjectMapper mapper = new ObjectMapper();
    LauncherConfig testConfig = mapper.readValue(new FileInputStream(configPath), LauncherConfig.class);
    Launcher launcher = new Launcher(testConfig);
    launcher.run();
  }
}
