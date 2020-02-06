name := "Ship Simulator"

version := "1.0"

scalaVersion := "2.12.7"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

//herokuAppName in Compile := "akka-ship-simulator"


libraryDependencies ++= Seq(
  jdbc,
  "org.postgresql" % "postgresql" % "9.4.1208.jre7",
  "com.vividsolutions" % "jts" % "1.13",
  "com.typesafe.play" %% "play-ws" % "2.6.20",
  "com.typesafe.play" %% "play-json" % "2.6.10",
  "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.0.1",
  "com.typesafe.play" %% "play-ws-standalone-json" % "1.0.1",
  "com.typesafe.play" %% "play-ws-standalone-xml" % "1.0.1"
)

libraryDependencies += guice
