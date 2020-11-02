package com.skarna3.hw2

import org.apache.hadoop.io.{IntWritable, Text}
import org.apache.hadoop.mapreduce.Reducer

import scala.jdk.javaapi.CollectionConverters.asScala

class AuthorReducer extends Reducer[Text,IntWritable,Text,IntWritable] {

  /**
   *
   * This is the reducer used in the following jobs:
   * - TupleChecker
   * - NumberAuthors (NumberAuthorsMapper)
   * - AuthorVenue (VenueMapper)
   *
   * The reducer sums the values in the values array and returns a tuple with the format (key, sum of values)
   *
   * @param key Key for which tuples handled by this reducer are grouped.
   * @param values List of values of the tuples sent to this reducer with key "key".
   * @param context Output stream
   */
  override def reduce(key: Text, values: java.lang.Iterable[IntWritable], context:Reducer[Text, IntWritable, Text, IntWritable]#Context): Unit = {

    //All tuples contain value 1 indicating that the mapper found 1 record with that key
    val sum = asScala(values).foldLeft(0) { (t,i) => t + i.get }

    //Write output tuple (e.g. ("2-3", 100))
    context.write(key, new IntWritable(sum))
  }
}
