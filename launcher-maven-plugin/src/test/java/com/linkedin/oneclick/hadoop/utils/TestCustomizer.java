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
package com.linkedin.oneclick.hadoop.utils;


import com.linkedin.oneclick.utils.Customizer;

import static org.testng.Assert.*;
import org.testng.annotations.Test;


/**
 * @author sovsankin@linkedin.com
 */
public class TestCustomizer
{
  @Test public void testCustomizer()
  {
    Customizer customizer= new Customizer();

    customizer.set("logLevel", "-o LogLevel=Error");
    customizer.set("host", "my-cluster");
    customizer.set("command", "hadoop and all that jazz");
    customizer.set("ssh.shell", "ssh ${logLevel}");
    customizer.set("rsync.shell", "-e @{ssh.shell}");
    customizer.set("rsync.options", "-azv");

    assertEquals(customizer.twoPhaseEvaluate("rsync ${rsync.options} ${rsync.shell} @{host} @{command}"),
        new String[] { "rsync", "-azv", "-e", "ssh -o LogLevel=Error", "my-cluster", "hadoop and all that jazz"});
  }

}
