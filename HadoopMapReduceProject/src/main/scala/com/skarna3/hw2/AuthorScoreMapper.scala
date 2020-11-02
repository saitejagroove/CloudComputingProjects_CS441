package com.skarna3.hw2

import com.skarna3.hw2.helpers.{AuthorshipHelper, BinHelper}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.hadoop.io.{DoubleWritable, IntWritable, Text}
import org.apache.hadoop.mapreduce.Mapper
import org.slf4j.{Logger, LoggerFactory}

/**
 * This is the Mapper used in the following jobs:
 * - AuthorScore
 */
class AuthorScoreMapper extends Mapper[Object, Text, Text, DoubleWritable] {

  val score = new DoubleWritable
  val authorText = new Text

  //Initialize Config and Logger objects from 3rd party libraries
  val configuration: Config = ConfigFactory.load("configuration.conf")
  val LOG: Logger = LoggerFactory.getLogger(getClass)

  /**
   *
   * This function iterates over authors, calls the Helper to compute the authorship score and adds the tuple to the result.
   *
   * @param author Author name
   * @param position Author position
   * @param size Number of authors
   */
  private def handleAuthor(author: String, position: Int, size: Int, context:Mapper[Object,Text,Text,DoubleWritable]#Context): Unit = {

    //LOG.debug("authorScore: "+position+" "+size)

    //Compute the authorship score
    val authorScore = AuthorshipHelper.getAuthorshipScore(position+1, size)

    authorText.set(author)
    score.set(authorScore)

    //LOG.debug("author: "+ author + " score: "+authorScore)

    //Write tuple
    context.write(authorText, score)
  }

  /**
   *
   * This mapper invokes the related helper function (handleAuthor) for each author found in the XML data segment.
   * The output tuple has the following format (AuthorName, AuthorScore)
   *
   * @param key Don't care
   * @param value XML data segment
   * @param context
   */
  override def map(key:Object, value:Text, context:Mapper[Object,Text,Text,DoubleWritable]#Context): Unit = {

    //Get dtd resource on filesystem
    val res = getClass.getClassLoader.getResource("dblp.dtd").toURI.toString

    val xmlComp =  s"""<?xml version="1.0" encoding="ISO-8859-1"?><!DOCTYPE dblp SYSTEM "$res"><dblp>${value.toString}</dblp>"""

    //Convert to XML
    val xml = scala.xml.XML.loadString(xmlComp)

    //Look for author tags
    val authors = (xml \\ "author")

    //Safety check, return immediately without adding tuples if no authors
    if (authors.isEmpty) {
      return
    }

    //Iterate over authors with index (https://stackoverflow.com/questions/6821194/get-index-of-current-element-in-a-foreach-method-of-traversable)
    authors.zipWithIndex.foreach{ case(x,i) => handleAuthor(x.text, i, authors.size, context) }
  }

}
