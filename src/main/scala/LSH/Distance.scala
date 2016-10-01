package LSH

import math._;

object Distance {

  //L2 Norm -> vector length
  def magnitude(x: Vector[Double]): Double = {
    val m = sqrt((x).map { case (x) => pow(x, 2) }.sum);
    return m;
  }

  //ISSUE returns every time a new vector because vectors immutable!!!
  //create unit vectors
  def normalize(x:Vector[Double]):Vector[Double]={
    val m=magnitude(x);
    val u=(x).map { case (x) =>(x/m) };
    return u;
  }
  //x.y
  def dotProduct(x: Vector[Double], y: Vector[Double]): Double = {
    val result = ((x zip y).map { case (x, y) => (y * x) }.sum);
    return result;
  }

  //Cosine Distance
  def cosineDistance(x: Vector[Double], y: Vector[Double]): Double = {
    return 1-((dotProduct(x, y)) / (magnitude(x) * magnitude(y)));
  }

  // Euclidean Distance
  def euclideanDistance(x: Vector[Double], y: Vector[Double]): Double = {
    val distance = sqrt((x zip y).map { case (x, y) => pow(y - x, 2) }.sum);
    return distance;
  }

  // L1Norm -> |x1-y1|+|x2-y2|+....
  def l1Norm(x: Vector[Double], y: Vector[Double]): Double = {
    val distance = ((x zip y).map { case (x, y) => abs(y - x) }.sum);
    return distance;
  }

  //L-infinity-Norm -> max(|x1-y1|,|x2-y2|,....)
  def linfinityNorm(x: Vector[Double], y: Vector[Double]): Double = {
    val distance = ((x zip y).map { case (x, y) => abs(y - x) }.max);
    return distance;
  }

  //Hamming Distance
  def hammingDistance(x: Vector[Double], y: Vector[Double]): Double = {
    val distance = ((x zip y).count { case (x, y) => (y != x) });
    return distance;
  }
}


