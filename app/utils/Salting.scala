package utils

import scala.util.Random

object Salting {

  def salt: String = Random.alphanumeric.take(6).mkString
}