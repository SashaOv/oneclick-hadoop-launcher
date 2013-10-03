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
package com.linkedin.oneclick.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ShellProcess
{
  static Logger log= LoggerFactory.getLogger(ShellProcess.class);

  ProcessBuilder builder;
  String workingDir = null;

  public ShellProcess(String... cmd)
  {
    builder = new ProcessBuilder(cmd);
    log.debug("cmd=" + cmdToString(builder.command()));
  }

  public ShellProcess(List<String> cmd)
  {
    builder = new ProcessBuilder(cmd);
    log.debug("cmd=" + cmdToString(builder.command()));
  }

  public void setWorkingDir(String workingDir)
  {
    this.workingDir = workingDir;
  }

  public static final int INTERRUPTED= -10;

  public int run()
  {
    try {
      if (workingDir != null)
        builder.directory(new File(workingDir));
//    builder.environment().putAll(env);
      Process process = builder.start();
      ConsoleStreamer.consumeFrom(process.getInputStream(), null);
      ConsoleStreamer.consumeFrom(process.getErrorStream(), "! ");
      int returnCode= process.waitFor();
      if (returnCode!=0)
        throw new OCException("The process failed, return code=" + Integer.toString(returnCode) +
          "\n\tcmd=" + cmdToString(builder.command()));
      return returnCode;
    } catch (InterruptedException interrupted) {
      Utils.ignore(interrupted);
      return INTERRUPTED;
    } catch (IOException e) {
      throw OCException.unchecked(e);
    }
  }

  static String cmdToString(List<?> list)
  {
    StringBuffer buf= new StringBuffer();
    for (Object obj : list) {
      if (buf.length() > 0)
        buf.append(' ');
      String s= obj.toString();
      boolean quote= s.indexOf(' ') >= 0;
      if (quote)
        buf.append('"');
      buf.append(s);
      if (quote)
        buf.append('"');
    }
    return buf.toString();
  }

  /**
   * Source: org.eclipse.jetty.maven.plugin.JettyRunForkedMojo.ConsoleStreamer
   */
  static class ConsoleStreamer implements Runnable
  {
    private String prefix;
    private BufferedReader reader;

    public ConsoleStreamer(InputStream is, String prefix)
    {
      this.prefix = prefix == null ? "" : prefix;
      this.reader = new BufferedReader(new InputStreamReader(is));
    }

    public ConsoleStreamer(InputStream is)
    {
      this(is, null);
    }

    public void run()
    {
      String line;
      try {
        while ((line = reader.readLine()) != (null)) {
          System.out.println(prefix + line);
        }
      } catch (IOException ex) {
        Utils.ignore(ex);
      } finally {
        Utils.closeQuietly(reader);
      }
    }

    public static Thread consumeFrom(InputStream inputStream, String prefix)
    {
      ConsoleStreamer pump = new ConsoleStreamer(inputStream, prefix);
      Thread thread = new Thread(pump,"ConsoleStreamer/" + prefix);
      thread.setDaemon(true);
      thread.start();
      return thread;
    }

  }
}
