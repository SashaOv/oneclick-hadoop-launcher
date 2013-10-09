#!/usr/bin/env python
import sys
from subprocess import Popen

def setVersion(version, dir):
    cmd= ["mvn", "-DnewVersion=" + version, "versions:set"]
    print("executing " + str(cmd) + " in " + dir)
    Popen(cmd, cwd=dir).wait()

version= sys.argv[1]

setVersion(version, ".")
setVersion(version, "hadoop-word-count")
setVersion(version, "launcher-maven-plugin")
