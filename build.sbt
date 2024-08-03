val scala3Version = "3.4.2"
val circeVersion = "0.14.9"
val tapirVersion = "1.11.0"
val http4sVersion = "0.23.16"

lazy val root = project
  .in(file("."))
  .settings(
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-armeria-server-zio" % tapirVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "org.scalameta" %% "munit" % "1.0.0" % Test
      )
  )
