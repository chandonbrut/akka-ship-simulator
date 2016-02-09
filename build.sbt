name := "Ship Simulator"

version := "1.0"

scalaVersion := "2.11.7"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % "2.4.1",
	"com.vividsolutions" % "jts" % "1.13",
	"com.typesafe.akka" %% "akka-slf4j" % "2.4.1",
	"org.apache.httpcomponents" % "httpclient" % "4.5.1"
)
