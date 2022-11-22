import Dependencies._

val Scala212 = "2.12.17"
val Scala213 = "2.13.10"
val Scala3 = "3.2.1"

ThisBuild / tlBaseVersion := "1.2"

ThisBuild / scalaVersion := Scala213
ThisBuild / crossScalaVersions := Seq(Scala212, Scala213, Scala3)

ThisBuild / organization := "de.thatscalaguy"
ThisBuild / organizationName := "ThatScalaGuy"

ThisBuild / startYear := Some(2021)
ThisBuild / licenses := Seq(License.MIT)
ThisBuild / developers := List(
  tlGitHubDev("ThatScalaGuy", "Sven Herrmann")
)

ThisBuild / githubWorkflowJavaVersions := Seq(
  JavaSpec.temurin("8"),
  JavaSpec.temurin("11"),
  JavaSpec.temurin("17")
)

ThisBuild / tlVersionIntroduced := Map("3" -> "1.2.0")

ThisBuild / tlSonatypeUseLegacyHost := false // deploy to s01.oss.sonatype.org

lazy val root = (project in file("."))
  .settings(
    name := "ulid4cats",
    libraryDependencies ++= Seq(
      catsEffect,
      ulid4s cross CrossVersion.for3Use2_13
    ),
    libraryDependencies += scalaTest % Test
  )
