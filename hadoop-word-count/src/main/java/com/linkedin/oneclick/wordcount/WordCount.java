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
package com.linkedin.oneclick.wordcount;


import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Source: http://wiki.apache.org/hadoop/WordCount
 */
public class WordCount extends Configured implements Tool
{
  static Logger log= LoggerFactory.getLogger(WordCount.class);

  static public class WordCountMapper extends Mapper<LongWritable, Text, Text, LongWritable>
  {
    final private static LongWritable ONE = new LongWritable(1);
    private Text tokenValue = new Text();

    @Override
    protected void map(LongWritable offset, Text text, Context context) throws IOException, InterruptedException
    {
      for (String token : text.toString().split("(\\s|[;.:\\-!,'\"\\)\\(\\?_])+")) {
        tokenValue.set(token.toLowerCase());
        context.write(tokenValue, ONE);
      }
    }
  }

  static public class WordCountReducer extends Reducer<Text, LongWritable, Text, LongWritable>
  {
    private LongWritable total = new LongWritable();

    @Override
    protected void reduce(Text token, Iterable<LongWritable> counts, Context context)
        throws IOException, InterruptedException
    {
      long n = 0;
      for (LongWritable count : counts)
        n += count.get();
      total.set(n);
      context.write(token, total);
    }
  }

  public int run(String[] args) throws Exception
  {
    Configuration conf = getConf();

    Job job = new Job(conf, "Word Count");
    job.setJarByClass(WordCount.class);

    String workDirectory= args.length >= 1?  args[0] : "wordcount";
    Path input= new Path(workDirectory, "input.txt");
    FileSystem fs= input.getFileSystem(conf);
    fs.mkdirs(input.getParent());
    copy(resourceInputStream(getClass().getResource("/onegin.txt")), createOutputStream(conf, input), conf);
    job.setInputFormatClass(TextInputFormat.class);
    job.setMapperClass(WordCountMapper.class);
    FileInputFormat.addInputPath(job, input);

    job.setCombinerClass(WordCountReducer.class);
    job.setReducerClass(WordCountReducer.class);

    job.setOutputFormatClass(TextOutputFormat.class);
    Path output= clean(conf, new Path(workDirectory, "wordcount"));
    FileOutputFormat.setOutputPath(job, output);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(LongWritable.class);

    return job.waitForCompletion(true) ? 0 : -1;
  }

  public static void main(String[] args) throws Exception
  {
    System.exit(ToolRunner.run(new WordCount(), args));
  }

  static InputStream resourceInputStream(URL resource) throws IOException
  {
    return resource.openStream();
  }

  static OutputStream createOutputStream(Configuration conf, Path path) throws IOException
  {
    return path.getFileSystem(conf).create(path, /*overwrite*/true);
  }

  static void copy(InputStream input, OutputStream output, Configuration conf) throws IOException
  {
    try {
      IOUtils.copyBytes(input, output, conf);
    } finally {
      input.close();
      output.close();
    }
  }

  static Path clean(Configuration conf, Path path) throws IOException
  {
    FileSystem fs= path.getFileSystem(conf);
    if (fs.exists(path))
      fs.delete(path, true);
    return path;
  }

  static Path tempFile(Configuration conf, String name)
  {
    try {
      Path result= new Path(conf.get("hadoop.tmp.dir"), name);
      log.info("tempFile=" + result.toString());
      FileSystem fs= result.getFileSystem(conf);
      fs.mkdirs(result.getParent());
      fs.deleteOnExit(result);
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
