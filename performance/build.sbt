name := "performance"

organization := "dk.aknniss"

version := "1.0"

homepage := Some(url("https://github.com/remythemsen/AKNNISS"))

startYear := Some(2016)

scmInfo := Some(
  ScmInfo(
    url("https://github.com/remythemsen/AKNNISS"),
    "scm:git:https://github.com/remythemsen/AKNNISS.git",
    Some("scm:git:git@github.com:remythemsen/AKNNISS.git")
  )
)

scalaVersion := "2.11.8"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.14",
  "com.typesafe.akka" %% "akka-remote" % "2.4.14",
  "com.typesafe" % "config" % "1.2.0",
  "com.github.romix.akka" %% "akka-kryo-serialization" % "0.5.0"
)

maintainer := "Remy Themsen <remt@itu.dk>"

dockerRepository := Some("remythemsen")

dockerBaseImage := "java"
enablePlugins(JavaAppPackaging)
