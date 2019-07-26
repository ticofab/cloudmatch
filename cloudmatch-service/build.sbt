name := "cloudmatch-service"
organization := "ticofab.io"
version := "0.1.0"
scalaVersion := "2.13.0"

libraryDependencies ++= {
  lazy val akkaVersion = "2.5.23"
  lazy val akkaHttpVersion = "10.1.9"
  Seq(
    // AKKA
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,

    // LOGGING
    "org.wvlet.airframe" %% "airframe-log" % "19.7.0",

    // TEST
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion
  )
}
