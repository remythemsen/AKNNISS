name := "AKNNISS"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.11.8",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.4",
  "org.scalanlp" %% "breeze" % "0.12",
  "org.scalanlp" %% "breeze-natives" % "0.12",
  "org.scalanlp" %% "breeze-viz" % "0.12",
  "org.scalactic" %% "scalactic" % "3.0.0",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "com.github.scopt" %% "scopt" % "3.5.0",
  "com.typesafe.akka" %% "akka-actor" % "2.4.14",
  "com.github.romix.akka" %% "akka-kryo-serialization" % "0.5.0"
)

dockerRepository := Some("remeeh")

lazy val AKNNISS = project in file(".")
lazy val utils = project in file("utils")
lazy val LSH = project in file("LSH") dependsOn(utils)
lazy val performance = project in file("performance") dependsOn(utils, LSH)
lazy val speedtest = project in file("speedtest") dependsOn(utils, LSH)
lazy val tablehandler = project in file("tablehandler") dependsOn(utils, LSH)
lazy val reducer = project in file("reducer") dependsOn(utils)
lazy val knntablebuilder = project in file("knntablebuilder") dependsOn (utils)
lazy val querygenerator = project in file("querygenerator") dependsOn (utils)
lazy val knntablereducer = project in file("knntablereducer") dependsOn(utils, LSH)
lazy val samplegenerator = project in file("samplegenerator") dependsOn(utils, LSH)
lazy val hashingtime = project in file("hashingtime") dependsOn(utils, LSH)
lazy val probingtime = project in file("probingtime") dependsOn(utils, LSH)
lazy val webdemo = (project in file("webdemo")).enablePlugins(play.sbt.PlayScala, JavaAppPackaging).dependsOn(utils, LSH).
  settings(
    scalaVersion := "2.11.7",
    version := "1.0"
  )

enablePlugins(PlayScala)
enablePlugins(JavaAppPackaging)