import config.AppConfig
import play.api.{Configuration, Environment, inject}

class Module extends inject.Module {

  val cfg = pureconfig.loadConfigOrThrow[AppConfig]

  def bindings(environment: Environment, configuration: Configuration): Seq[inject.Binding[_]] = {
    Seq(
      bind[AppConfig].toInstance(cfg)
    )
  }
}