lazy val `cloudmatch` =
  project
    .in(file("."))
    .enablePlugins(DockerPlugin, JavaAppPackaging)
    .settings(dockerSettings: _*)
    .settings(
      libraryDependencies ++= {
        lazy val akkaVersion = "2.5.23"
        lazy val akkaHttpVersion = "10.1.9"
        lazy val akkaManagementVersion = "1.0.1"
        Seq(
          // AKKA
          "com.typesafe.akka" %% "akka-actor" % akkaVersion,
          "com.typesafe.akka" %% "akka-stream" % akkaVersion,
          "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
          "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
          "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion,
          "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
          "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion,

          // CONFIG
          "com.github.pureconfig" %% "pureconfig" % "0.11.1",

          // LOGGING
          "org.wvlet.airframe" %% "airframe-log" % "19.7.0",

          // TEST
          "com.typesafe.akka" %% "akka-testkit" % akkaVersion
        )
      },
      version := "1.0.0",
      name := "cloudmatch",
      organization := "ticofab.io",
      scalaVersion := "2.13.0"
    )

lazy val dockerSettings =
  Seq(
    packageSummary := "Trip Service",
    packageDescription := "Handling trips",
    dockerExposedPorts := Seq(8080, 8558),
    dockerBaseImage := "openjdk:11-jdk"
  )
