package utils

import scala.util.Random

object Salting {

  def salt: String = Random.nextString(6)
}