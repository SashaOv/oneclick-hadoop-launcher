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

public class OCException extends RuntimeException
{
  public OCException(Throwable cause)
  {
    super(cause);
  }

  public OCException(String message)
  {
    super(message);
  }

  public OCException(String message, Throwable cause)
  {
    super(message, cause);
  }

  /**
   * Robust alternative to incorrect ex.printStackTrace automatically generated by most IDEs.
   *
   * Convert checked exception into unchecked one, for example:
   * <pre>
   *   try {
   *      // declares 3 types of useless exceptions
   *      Object test= Class.forName("Test").newInstance();
   *   } catch(Exception e) {
   *      throw OCException.unchecked(e);
   *   }
   * </pre>
   *
   * Inspired by com.google.common.base.Throwables#propagate
   */
  public static RuntimeException unchecked(Throwable ex)
  {
    if (ex instanceof RuntimeException)
      return (RuntimeException) ex; // no need to wrap
    else
      return new OCException(ex);
  }
}