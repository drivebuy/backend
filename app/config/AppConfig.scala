package config

import pureconfig.{CamelCase, KebabCase, ProductHint}

import scala.concurrent.duration.Duration

case class AppConfig(mongodb: Mongo)

object AppConfig {
  implicit val hint: ProductHint[AppConfig] = ProductHint((fieldName: String) =>
    KebabCase.fromTokens(CamelCase.toTokens(fieldName)))
}

case class Mongo(uri: String)