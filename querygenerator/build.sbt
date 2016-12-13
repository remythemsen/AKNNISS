name := "querygenerator"

version := "1.0"

scalaVersion := "2.11.8"

organization := "dk.aknniss"

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

maintainer := "Remy Themsen <remt@itu.dk>"

dockerRepository := Some("remeeh")

dockerBaseImage := "java"
enablePlugins(JavaAppPackaging)
