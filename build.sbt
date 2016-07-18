name := "Ship Simulator"

version := "1.0"

scalaVersion := "2.11.7"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
	"com.vividsolutions" % "jts" % "1.13",
  "com.typesafe.play" %% "play-ws" % "2.4.6"
)
