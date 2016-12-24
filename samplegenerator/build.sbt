
name := "samplegenerator"

organization := "dk.aknniss"

version := "1.0"

homepage := Some(url("https://github.com/remythemsen/AKNNISS"))

startYear := Some(2016)

scalaVersion := "2.11.8"

maintainer := "Remy Themsen <remt@itu.dk>"

dockerRepository := Some("remeeh")

dockerBaseImage := "java"

enablePlugins(JavaAppPackaging)