name := """backend"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"
scalacOptions += "-Ypartial-unification"

libraryDependencies += guice

libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.16.5-play27"
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.9.2"
libraryDependencies += "org.typelevel" %% "cats-core" % "1.6.0"
libraryDependencies += "eu.timepit" %% "refined" % "0.9.5"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.1" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
