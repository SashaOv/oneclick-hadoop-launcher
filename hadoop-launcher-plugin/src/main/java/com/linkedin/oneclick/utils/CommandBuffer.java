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
package com.linkedin.oneclick.utils;

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
