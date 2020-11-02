package com.skarna3.hw2

import java.lang

import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Reducer

import scala.collection.mutable
import org.slf4j.LoggerFactory

class NumSingleAuthorsVenueReducer extends Reducer[Text,Text,Text,Text] {

  val map:mutable.HashMap[String,Int] = new mutable.HashMap[String,Int]()

  override def reduce(key: Text, values: lang.Iterable[Text], context: Reducer[Text, Text, Text, Text]#Context): Unit = {
    //    Clearing the values so that Hashmaps would not be combined with other operations.
    map.clear()
    val venue:Text = key

    values.forEach(author => {
      val authorString = author.toString
      if (map.contains(authorString)) {
        val count:Int = map.getOrElse(authorString,0)
        map.update(authorString, count+1)
      } else {
        map.put(authorString,1)
      }
    })
    sort(venue,context)
  }

  def sort(venue:Text,context: Reducer[Text, Text, Text, Text]#Context) = {
    val list = map.toList
    val a:Int = 2

    val list2 = list.sortWith(sortHelperFunction)

    LoggerFactory.getLogger("MRDriver.class").debug(list2.toString())
    val res:Text = new Text()
    res.set(list2.slice(0,10).toString())
    //LoggerFactory.getLogger("jaffa").debug(res.toString)
    context.write(venue,res)
  }

  def sortHelperFunction(a:(String,Int),b:(String,Int)): Boolean  = {
    a._2 > b._2
  }
}
