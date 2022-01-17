import Dependencies._

ThisBuild / scalaVersion       := "2.13.3"
ThisBuild / crossScalaVersions := Seq("2.12.15", "2.13.8")
ThisBuild / version            := "1.0.0"
ThisBuild / organization       := "de.thatscalaguy"
ThisBuild / organizationName   := "thatscalaguy"

lazy val root = (project in file("."))
  .settings(
    name := "ulid4cats",
    libraryDependencies ++= Seq(catsCore, ulid4s),
    libraryDependencies += scalaTest % Test
  )
