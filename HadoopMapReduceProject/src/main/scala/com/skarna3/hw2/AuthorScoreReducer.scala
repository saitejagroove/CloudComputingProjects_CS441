package com.skarna3.hw2

import org.apache.hadoop.io.{DoubleWritable, IntWritable, Text}
import org.apache.hadoop.mapreduce.Reducer

import scala.jdk.javaapi.CollectionConverters.asScala

/**
 * This is the Reducer used in the following jobs:
 * - AuthorScore
 */
class AuthorScoreReducer extends Reducer[Text,DoubleWritable,Text,DoubleWritable] {

  /**
   * The reducer sums double values in the values array and returns a tuple with the format (key, sum of values)
   * This is identical to the Reducer in the AuthorReducer class, but not Double values.
   *
   * @param key Key for which tuples handled by this reducer are grouped.
   * @param values List of values of the tuples sent to this reducer with key "key".
   * @param context Output stream
   */
  override def reduce(key: Text, values: java.lang.Iterable[DoubleWritable], context:Reducer[Text, DoubleWritable, Text, DoubleWritable]#Context): Unit = {

    val initial: Double = 0.0
    val sum = asScala(values).foldLeft(initial) {
      (t,i) => t + i.get()
    }

    //Write output tuple
    context.write(key, new DoubleWritable(sum))
  }
}
