import Dependencies._
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val Scala213 = "2.13.18"
val Scala3 = "3.3.7"

ThisBuild / tlBaseVersion := "2.0"

ThisBuild / scalaVersion := Scala3
ThisBuild / crossScalaVersions := Seq(Scala213, Scala3)

ThisBuild / organization := "de.thatscalaguy"
ThisBuild / organizationName := "ThatScalaGuy"

ThisBuild / startYear := Some(2025)
ThisBuild / licenses := Seq(License.MIT)
ThisBuild / developers := List(
  tlGitHubDev("ThatScalaGuy", "Sven Herrmann")
)

ThisBuild / githubWorkflowJavaVersions := Seq(
  JavaSpec.temurin("11"),
  JavaSpec.temurin("17"),
  JavaSpec.temurin("21")
)

ThisBuild / tlVersionIntroduced := Map("3" -> "1.2.0")

lazy val root = tlCrossRootProject.aggregate(core)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Full)
  .in(file("core"))
  .settings(
    name := "ulid4cats",
    libraryDependencies ++= Seq(
      catsCore.value,
      catsEffect.value,
      munit.value % Test,
      munitCatsEffect.value % Test
    )
  )
  .jvmSettings(
    // JVM-specific settings
  )
  .jsSettings(
    // Scala.js specific settings
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )
