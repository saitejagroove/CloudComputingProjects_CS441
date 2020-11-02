package com.skarna3.hw2.helpers

import scala.collection.mutable.ListBuffer

object StatisticsHelper {

  /**
   *
   * This function computes the median given a sorted ListBuffer as input
   *
   * @param array Sorted array on which to perform the median computation
   * @return median value
   */
  def computeMedian(array: ListBuffer[Int]): Double = {

    if (array.length % 2 == 0) { //If array has even number of elements
      val median = (array(array.length/2).toDouble + array((array.length/2)-1).toDouble) / 2.0
      return median
    } else { //Else array has odd number of elements, so we just get the element in the middle
      val median = array(array.length/2).toDouble
      return median
    }
  }

  /**
   *
   * This function computes the average given a ListBuffer as input
   *
   * @param array Array on which to perform the average computation
   * @return average value
   */
  def computeAvg(array: ListBuffer[Int]): Double = {
    val sum = array.foldLeft(0.0) { (t,i) => t + i }
    val avg: Double = sum/array.size.toDouble
    return avg
  }

}
