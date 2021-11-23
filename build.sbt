import Dependencies._

ThisBuild / scalaVersion     := "2.13.7"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val http4sVersion = "1.0.0-M29"

lazy val root = (project in file("."))
  .settings(
    name := "EvolutionGame",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "org.typelevel" %% "kittens" % "3.0.0-M1",
      "org.typelevel" %% "cats-core" % "2.6.1",
      "org.typelevel" %% "cats-effect" % "3.2.9",

      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-dsl"          % http4sVersion,
      //"org.http4s"      %% "http4s-circe"        % http4sVersion,

      "org.slf4j" % "slf4j-simple" % "1.7.12",
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.6" % Test
    ),
    initialCommands :=
      """
        | import cats.implicits._, cats._, cats.syntax.all._
        | import cats.effect.{IO, SyncIO}, cats.effect.unsafe.implicits.global, cats.effect.std._
        | import evolution.Data._, evolution.Logic._, evolution.Util._
        |""".stripMargin,

    testFrameworks += new TestFramework("munit.Framework"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)

  )
