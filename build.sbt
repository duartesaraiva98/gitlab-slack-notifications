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
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-client" % tapirVersion,
      "com.softwaremill.sttp.client3" %% "armeria-backend-zio" % "3.9.7",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.slack.api" % "slack-api-client" % "1.40.3",
      "org.scalameta" %% "munit" % "1.0.0" % Test
    )
  )
