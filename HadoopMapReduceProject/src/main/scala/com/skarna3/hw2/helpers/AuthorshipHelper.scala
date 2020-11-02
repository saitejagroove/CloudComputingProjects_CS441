package com.skarna3.hw2.helpers

object AuthorshipHelper {

  /**
   *
   * This function computes the authorship score for the author at position "position"
   *
   * @param position Author position in current paper (first position is 1, last position is numAuthors)
   * @param numAuthors Number of authors for this paper
   * @return Authorship score for the current author
   */
  def getAuthorshipScore(position: Int, numAuthors: Int): Double = {

    if (numAuthors==1) {
      return 1
    }

    //1/N
    val baseScore: Double = 1.0/numAuthors

    //1/4*N
    val increment: Double = 1.0/(4.0*numAuthors)

    if (position==1) {
      return baseScore+increment
    } else if  (position==numAuthors) {
      return baseScore-increment
    } else {
      return baseScore
    }

  }

}
