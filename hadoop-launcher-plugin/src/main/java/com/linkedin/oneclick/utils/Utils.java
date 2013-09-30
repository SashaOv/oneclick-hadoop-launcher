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


import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


/**
 * Small self-contained utilities.
 */
public class Utils
{
  static private Logger log= LoggerFactory.getLogger(Utils.class);

  /** @see com.google.common.io.Closeables#closeQuietly(java.io.Closeable) */
  public static void closeQuietly(Closeable closeable)
  {
    if (closeable!=null) {
      try {
        closeable.close();
      } catch (IOException e) {
        log.debug("closing", e);
      }
    }
  }

  /**
   * Document the fact that we are ignoring this exception
   */
  public static void ignore(Throwable exception)
  {
  }

  public static InputStream getResourceAsStreamChecked(Class<?> classLoaderDonor, String name)
  {
    InputStream result= classLoaderDonor.getResourceAsStream(name);
    if (result==null)
      throw new OCException("Resource was not found - " + name);
    return result;
  }

  public static<T> String toString(T[] arr)
  {
    if (arr==null)
      return "(null)";
    else {
      StringBuffer result= new StringBuffer();
      for (T i : arr) {
        if (result.length() > 0)
          result.append(' ');
        if (i==null) {
          result.append("(null)");
        } else {
          result.append('\'');
          result.append(i.toString());
          result.append('\'');
        }
      }
      return result.toString();
    }
  }
}
