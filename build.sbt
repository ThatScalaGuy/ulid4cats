import Dependencies._

ThisBuild / scalaVersion       := "2.13.3"
ThisBuild / crossScalaVersions := Seq("2.12.12", "2.13.3")
ThisBuild / version            := "0.9.2"
ThisBuild / organization       := "de.thatscalaguy"
ThisBuild / organizationName   := "thatscalaguy"

lazy val root = (project in file("."))
  .settings(
    name := "ulid4cats",
    libraryDependencies ++= Seq(catsCore, ulid4s),
    libraryDependencies += scalaTest % Test
  )
