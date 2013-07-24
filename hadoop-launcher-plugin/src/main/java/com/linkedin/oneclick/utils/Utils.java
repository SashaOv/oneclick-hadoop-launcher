package com.linkedin.oneclick.utils;


import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


/**
 * Small self-contained utilities.
 *
 * @author sovsankin@linkedin.com
 */
public class Utils
{
  static private Logger log= LoggerFactory.getLogger(Utils.class);

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
   * Document the fact that we are ignoring exceptions
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
