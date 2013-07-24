package com.linkedin.oneclick.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author sovsankin@linkedin.com
 */
public class ShellProcess
{
  static Logger log= LoggerFactory.getLogger(ShellProcess.class);

  ProcessBuilder builder;
  String workingDir = null;

  public ShellProcess(String... cmd)
  {
    builder = new ProcessBuilder(cmd);
    log.debug("cmd=" + toString(builder.command()));
  }

  public ShellProcess(List<String> cmd)
  {
    builder = new ProcessBuilder(cmd);
    log.debug("cmd=" + toString(builder.command()));
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
      return process.waitFor();
    } catch (InterruptedException interrupted) {
      Utils.ignore(interrupted);
      return INTERRUPTED;
    } catch (IOException e) {
      throw OCException.unchecked(e);
    }
  }

  static String toString(List<?> list)
  {
    StringBuffer buf= new StringBuffer();
    for (Object obj : list) {
      if (buf.length() > 0)
        buf.append(' ');
      buf.append(CommandBuffer.escape(obj.toString()));
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
