name := "AKNNISS"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.11.8",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.4",
  "org.scalanlp" %% "breeze" % "0.12",
  "org.scalanlp" %% "breeze-natives" % "0.12",
  "org.scalanlp" %% "breeze-viz" % "0.12",
  "org.scalactic" %% "scalactic" % "3.0.0",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "com.github.scopt" %% "scopt" % "3.5.0",
  "com.typesafe.akka" % "akka-actor" % "2.0"
)

lazy val AKNNISS = project in file(".")
lazy val LSH = project in file("LSH") dependsOn utils
lazy val performance = project in file("performance") dependsOn utils
lazy val utils = project in file("utils")
lazy val demo = project in file("demo") dependsOn(LSH, utils)


