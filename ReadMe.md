One Click Hadoop Launcher
=========================

One Click Hadoop Launcher is a [Maven](http://maven.apache.org) plugin that allows to shorten the compile-deploy-test
cycle of Hadoop development to under a minute, even if you are on the VPN.

More importantly, you can associate the
plugin with a button in your IntelliJ IDE (maybe Eclipse as well) so you can execute the whole compile-deploy-run
sequence with one button click.
Done many times a day, this saves you mental energy by not needing to switch context to "deployment mode".

Using the plugin
----------------
The easiest way is to use [My Github Maven Repository](https://github.com/SashaOv/mvn-repo)

Just add this to your POM file:

	    <pluginRepositories>
	        <pluginRepository>
	            <id>Sashas-mvn-repo</id>
	            <name>SashaOv Maven Repo</name>
	            <layout>default</layout>
	            <url>https://raw.github.com/SashaOv/mvn-repo/master</url>
	        </pluginRepository>
	    </pluginRepositories>

The [hadoop-word-count](hadoop-word-count) project contains example of the plugin usage in the
[POM file](hadoop-word-count/pom.xml).

