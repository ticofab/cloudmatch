name := "cloudmatch-db"
version := "0.1.0"
scalaVersion := "2.12.8"

libraryDependencies ++= {
  lazy val akkaVersion = "2.5.23"
  lazy val akkaHttpVersion = "10.1.9"
  lazy val circeVersion = "0.11.1"

  Seq(
    // SLICK
    "com.typesafe.slick" %% "slick" % "3.3.1",
    "org.slf4j" % "slf4j-nop" % "1.7.26",

    // AKKA
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "de.heikoseeberger" %% "akka-http-circe" % "1.27.0",

    // CONFIG
    "com.github.pureconfig" %% "pureconfig" % "0.11.1",

    // LOGGING
    "org.wvlet.airframe" %% "airframe-log" % "19.7.0"
  )
}
