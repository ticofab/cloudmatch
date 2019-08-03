lazy val `cloudmatch-service` =
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

          "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion,
          "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
          "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion,

          // HTTP
          "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
          "io.spray" %%  "spray-json" % "1.3.5",

          // CONFIG
          "com.iheart" %% "ficus" % "1.4.7",

          // LOGGING
          "org.wvlet.airframe" %% "airframe-log" % "19.7.0",

          // TEST
          "com.typesafe.akka" %% "akka-testkit" % akkaVersion
        )
      },
      version := "1.0.0",
      name := "cloudmatch-service",
      organization := "ticofab.io",
      scalaVersion := "2.13.0"
    )

lazy val dockerSettings =
  Seq(
    packageSummary := "Cloudmatch Service",
    packageDescription := "Handling device connections",
    dockerExposedPorts := Seq(8080, 8558),
    dockerBaseImage := "openjdk:11-jdk"
  )
