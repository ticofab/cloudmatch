lazy val `cloudmatch-stream` =
  project
    .in(file("."))
    .enablePlugins(DockerPlugin, JavaAppPackaging)
    .settings(dockerSettings: _*)
    .settings(
      libraryDependencies ++= {
        lazy val akkaVersion = "2.5.23"
        lazy val akkaHttpVersion = "10.1.9"
        lazy val circeVersion = "0.11.1"
        lazy val flinkVersion = "1.8.1"

        Seq(
          // FLINK
          "org.apache.flink" %% "flink-scala" % flinkVersion,
          "org.apache.flink" %% "flink-streaming-scala" % flinkVersion,
          "org.apache.flink" %% "flink-connector-rabbitmq" % flinkVersion,

          // CONFIG
          "com.github.pureconfig" %% "pureconfig" % "0.11.1",

          // LOGGING
          "org.wvlet.airframe" %% "airframe-log" % "19.7.0"
        )
      },
      name := "cloudmatch-stream",
      version := "1.0.0",
      organization := "ticofab.io",
      scalaVersion := "2.12.8"
    )

lazy val dockerSettings =
  Seq(
    packageSummary := "Cloudmatch Stream",
    packageDescription := "Analytics data for cloudmatch",
    dockerExposedPorts := Seq(8080),
    dockerBaseImage := "openjdk:11-jdk"
  )
