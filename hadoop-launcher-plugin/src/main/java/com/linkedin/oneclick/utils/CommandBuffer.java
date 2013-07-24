package com.linkedin.oneclick.utils;


/**
* @author sovsankin@linkedin.com
*/
public class CommandBuffer
{
  StringBuffer buffer= new StringBuffer();

  public CommandBuffer()
  {

  }

  public CommandBuffer(String... args)
  {
    for (String arg:args)
      add(arg);
  }

  /**
   * TODO look for ", $, ` and if they exist in the string, escape them with \ and enclose the result in "
   */
  public static String escape(String in)
  {
    return in;
  }

  public void add(String argument)
  {
    if (buffer.length() > 0)
      buffer.append(' ');
    buffer.append(escape(argument));
  }

  public String toString()
  {
    return buffer.toString();
  }

}
