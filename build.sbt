import Dependencies._

val Scala212 = "2.12.17"
val Scala213 = "2.13.10"

ThisBuild / tlBaseVersion := "1.1"

ThisBuild / scalaVersion := Scala213
ThisBuild / crossScalaVersions := Seq(Scala212, Scala213)

ThisBuild / organization := "de.thatscalaguy"
ThisBuild / organizationName := "ThatScalaGuy"

ThisBuild / startYear := Some(2021)
ThisBuild / licenses := Seq(License.MIT)
ThisBuild / developers := List(
  tlGitHubDev("ThatScalaGuy", "Sven Herrmann")
)

ThisBuild / tlSonatypeUseLegacyHost := false // deploy to s01.oss.sonatype.org

lazy val root = (project in file("."))
  .settings(
    name := "ulid4cats",
    libraryDependencies ++= Seq(catsEffect, ulid4s),
    libraryDependencies += scalaTest % Test
  )
