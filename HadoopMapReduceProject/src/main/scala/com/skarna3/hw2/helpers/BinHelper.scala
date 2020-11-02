package com.skarna3.hw2.helpers

object BinHelper {

  /**
   * This function generates the bins from the author count
   *
   * @param count Author count value
   * @param currentPass Internal parameter for recursive call, current call pass
   * @param currentMin Internal parameter for recursive call, current minimum at current iteration
   * @param currentMax Internal parameter for recursive call, current maximum at current iteration
   * @return bin identifier as String
   */
  //Start: x,0,1,1
  private def getBinFromCountImpl(count: Int, currentPass: Int, currentMin: Int, currentMax: Int): String = {

    //Avoid StackOverFlow, this should never happen however. Check input params
    if (currentMin>currentMax || count==0) {
      return null
    }

    //Override for output format reasons
    if (count == 1) {
      return "1";
    }

    //If count is inside, then return bin defined by min and max
    if (count>=currentMin && count<=currentMax) {
      return s"$currentMin-$currentMax";
    }

    getBinFromCountImpl(count, currentPass+1, currentMin+1+currentPass, currentMax+2+currentPass)
  }

  /*
   1
   2,3
   4,5,6
   7,8,9,10
   11,12,13,14,15
    */

  /**
   * Recursive function wrapper. This function generates the bins from the author count.
   *
   * This function implements the bin generation policy for task 1.
   * Bin size grows incrementally by 1; in this way we counter-balance the increased sparsity of the dataset when the number of authors increases.
   *
   * @param count Number of authors
   * @return bin identifier as String
   */
  def getBinFromCount(count: Int) : String = {
    return getBinFromCountImpl(count, 0, 1, 1)
  }

  /**
   *
   * Recursive function used in order to compute the year bins in blocks of 10 years.
   *
   * @param year Year for which we wish to compute the bin
   * @param currentMin Internal parameter for recursive call, current minimum at current iteration
   * @param currentMax Internal parameter for recursive call, current maximum at current iteration
   * @return bin identifier as String
   */
  private def getBinForYearsImpl(year: Int, currentMin: Int, currentMax: Int) : String = {

    if (year>=currentMin && year<=currentMax) {
      return s"$currentMin-$currentMax";
    }

    return getBinForYearsImpl(year, currentMin+10, currentMax+10)
  }

  /**
   *
   * Recursive function wrapper. Recursive function used in order to compute the year bins in blocks of 10 years.
   *
   * @param year Year for which we wish to compute the bin
   * @return bin identifier as String
   */
  /*
  1901-1910
  1911-1920
  1921-1930
   */
  def getBinForYears(year: Int) : String = {
    return getBinForYearsImpl(year, 1901, 1910)
  }
}
