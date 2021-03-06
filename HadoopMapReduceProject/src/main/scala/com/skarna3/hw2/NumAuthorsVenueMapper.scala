package com.skarna3.hw2

import com.skarna3.hw2.MapReduceDriver.getClass
import com.skarna3.hw2.helpers.BinHelper
import com.typesafe.config.{Config, ConfigFactory}
import javax.xml.parsers.SAXParserFactory
import org.apache.hadoop.io.{IntWritable, Text}
import org.apache.hadoop.mapreduce.Mapper
import org.slf4j.{Logger, LoggerFactory}
import java.io.File
import java.nio.file.Paths

import scala.xml.XML

/**
 * This is the Mapper used in the following jobs:
 * - NumberAuthors
 */
class NumAuthorsVenueMapper extends Mapper[Object, Text, Text, Text] {

  val one = new IntWritable(1)
  val bin = new Text
  val ven = new Text

  //Initialize Config and Logger objects from 3rd party libraries
  val configuration: Config = ConfigFactory.load("configuration.conf")
  val LOG: Logger = LoggerFactory.getLogger(getClass)

  /**
   *
   * This Mapper function computes tuple with the following format (bin, num of publications)
   *
   * @param key Input key -> generic object, never used
   * @param value Input value -> RAW XML input segment, full tag block. e.g. <article> ... </article>
   * @param context Output stream
   */
  override def map(key:Object, value:Text, context:Mapper[Object,Text,Text,Text]#Context): Unit = {
    //Get dtd resource on filesystem
    val res = getClass.getClassLoader.getResource("dblp.dtd").toURI.toString

    val xmlComp =  s"""<?xml version="1.0" encoding="ISO-8859-1"?><!DOCTYPE dblp SYSTEM "$res"><dblp>${value.toString}</dblp>"""

    //Convert to XML
    val xml = scala.xml.XML.loadString(xmlComp)

    //Look for author tags
    val authors = (xml \\ "author")

    val venue = (xml \\ "journal")
    ven.set(venue.text)

    if (authors.nonEmpty) {
      authors.foreach( author =>{
        bin.set(author.text)
        context.write(ven,bin)
      })
    }
  }
}
