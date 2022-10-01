import Dependencies._

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / crossScalaVersions := Seq("2.12.17", "2.13.8")
ThisBuild / version := "1.1.0"
ThisBuild / organization := "de.thatscalaguy"
ThisBuild / organizationName := "thatscalaguy"

lazy val root = (project in file("."))
  .settings(
    name := "ulid4cats",
    libraryDependencies ++= Seq(catsEffect, ulid4s),
    libraryDependencies += scalaTest % Test
  )
