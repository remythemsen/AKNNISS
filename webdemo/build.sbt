name := "webdemo"

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

scalaVersion := "2.11.7"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq( jdbc , cache , ws   , specs2 % Test )

maintainer := "Remy Themsen <remt@itu.dk>"

dockerRepository := Some("remeeh")

dockerExposedPorts := Seq(2552)

dockerBaseImage := "java"
