package com.skarna3.hw2

import java.io.File

import com.skarna3.hw2.helpers.BinHelper
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.io.FileUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{DoubleWritable, IntWritable, Text}
import org.apache.hadoop.mapred.JobConf
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.chain.{ChainMapper, ChainReducer}
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.{FileOutputFormat, TextOutputFormat}
import org.slf4j.{Logger, LoggerFactory}


/**
 *
 * This class contains the Map/Reduce driver for all the tasks assigned.
 *
 * The jobs implemented as the following:
 * - TupleChecker (job0)
 * - NumberAuthors (job1)
 * - AuthorVenue (job2)
 * - AuthorScore (job3)
 * - AuthorScoreOrdered (job5)
 * - AuthorStatistics (job4)
 * - AuthorVenueStatistics (job6)
 * - Authors by Venue (Job7)
 * - Single author Publications by Venue (Job8)
 *
 */
object MapReduceDriver {

  //Initialize Config and Logger objects from 3rd party libraries
  val configuration: Config = ConfigFactory.load("configuration.conf")
  val LOG: Logger = LoggerFactory.getLogger(getClass)

  //Read input and output file paths from configuration file
  val inputFile: String = configuration.getString("configuration.inputFile")
  val outputFile: String = configuration.getString("configuration.outputFile")

  def main(args: Array[String]): Unit = {

    val startTime = System.nanoTime
    LOG.info("*** Starting MapReduceDriver ***")

    val verbose = configuration.getBoolean("configuration.verbose")
    val startTags = configuration.getString("configuration.startTags")
    val endTags = configuration.getString("configuration.endTags")

    val jobName0 = configuration.getString("configuration.jobName0")
    val jobName1 = configuration.getString("configuration.jobName1")
    val jobName2 = configuration.getString("configuration.jobName2")
    val jobName3 = configuration.getString("configuration.jobName3")
    val jobName4 = configuration.getString("configuration.jobName4")
    val jobName7 = configuration.getString("configuration.jobName7")
    val jobName8 = configuration.getString("configuration.jobName8")

    //Delete output_dir each time the map/reduce is run
    LOG.info("Deleting output directory..")
    FileUtils.deleteDirectory(new File(outputFile));

    val conf: Configuration = new Configuration()

    //Set start and end tags for XmlInputFormat
    conf.set(InputFormatter.START_TAGS, startTags)
    conf.set(InputFormatter.END_TAGS, endTags)

    //Format as CSV output
    conf.set("mapred.textoutputformat.separator", Constants.COMMA)



    //NumberAuthors Job
    val job1 = Job.getInstance(conf, jobName1)
    job1.setJarByClass(classOf[NumberAuthorsMapper])
    job1.setMapperClass(classOf[NumberAuthorsMapper])
    job1.setReducerClass(classOf[AuthorReducer])
    job1.setInputFormatClass(classOf[InputFormatter])
    job1.setOutputKeyClass(classOf[Text])
    job1.setOutputValueClass(classOf[IntWritable])
    job1.setOutputFormatClass(classOf[TextOutputFormat[Text, IntWritable]])
    FileInputFormat.addInputPath(job1, new Path(inputFile))
    FileOutputFormat.setOutputPath(job1, new Path((outputFile+Constants.SLASH+jobName1)))

    //AuthorVenue Job
    val job2 = Job.getInstance(conf, jobName2)
    job2.setJarByClass(classOf[VenueMapper])
    job2.setMapperClass(classOf[VenueMapper])
    job2.setReducerClass(classOf[AuthorReducer])
    job2.setInputFormatClass(classOf[InputFormatter])
    job2.setOutputKeyClass(classOf[Text])
    job2.setOutputValueClass(classOf[IntWritable])
    job2.setOutputFormatClass(classOf[TextOutputFormat[Text, IntWritable]])
    FileInputFormat.addInputPath(job2, new Path(inputFile))
    FileOutputFormat.setOutputPath(job2, new Path((outputFile+Constants.SLASH+jobName2)))

    //AuthorScore Job
    val job3 = Job.getInstance(conf, jobName3)
    job3.setJarByClass(classOf[AuthorScoreMapper])
    job3.setMapperClass(classOf[AuthorScoreMapper])
    job3.setReducerClass(classOf[AuthorScoreReducer])
    job3.setInputFormatClass(classOf[InputFormatter])
    job3.setOutputKeyClass(classOf[Text])
    job3.setOutputValueClass(classOf[DoubleWritable])
    job3.setOutputFormatClass(classOf[TextOutputFormat[Text, DoubleWritable]])
    FileInputFormat.addInputPath(job3, new Path(inputFile))
    FileOutputFormat.setOutputPath(job3, new Path((outputFile+Constants.SLASH+jobName3)))

    //Num_Author_Venues Job
    val job7 = Job.getInstance(conf, jobName7)
    job7.setJarByClass(classOf[NumAuthorsVenueMapper])
    job7.setMapperClass(classOf[NumAuthorsVenueMapper])
    job7.setReducerClass(classOf[NumAuthorsVenueReducer])
    job7.setInputFormatClass(classOf[InputFormatter])
    job7.setOutputKeyClass(classOf[Text])
    job7.setOutputValueClass(classOf[Text])
    job7.setOutputFormatClass(classOf[TextOutputFormat[Text, Text]])
    FileInputFormat.addInputPath(job7, new Path(inputFile))
    FileOutputFormat.setOutputPath(job7, new Path((outputFile+Constants.SLASH+jobName7)))

    //SingleAuthor Publication Job
    val job8 = Job.getInstance(conf, jobName8)
    job8.setJarByClass(classOf[NumSingleAuthorsVenueMapper])
    job8.setMapperClass(classOf[NumSingleAuthorsVenueMapper])
    job8.setReducerClass(classOf[NumSingleAuthorsVenueReducer])
    job8.setInputFormatClass(classOf[InputFormatter])
    job8.setOutputKeyClass(classOf[Text])
    job8.setOutputValueClass(classOf[Text])
    job8.setOutputFormatClass(classOf[TextOutputFormat[Text, Text]])
    FileInputFormat.addInputPath(job7, new Path(inputFile))
    FileOutputFormat.setOutputPath(job7, new Path((outputFile+Constants.SLASH+jobName8)))


    LOG.info("*** Starting Job(s) NOW ***")
    if (  job7.waitForCompletion(verbose) && job8.waitForCompletion(verbose)) {
    //if (job0.waitForCompletion(verbose)) {
      val endTime = System.nanoTime
      val totalTime = endTime - startTime
      LOG.info("*** FINISHED (Execution completed in: "+totalTime/1_000_000_000+" sec) ***")
    } else {
      LOG.info("*** FAILED ***")
    }
  }
  }
