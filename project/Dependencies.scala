import sbt._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  lazy val catsCore = Def.setting("org.typelevel" %%% "cats-core" % "2.12.0")
  lazy val catsEffect = Def.setting("org.typelevel" %%% "cats-effect" % "3.6.1")
  lazy val munit = Def.setting("org.scalameta" %%% "munit" % "1.0.3")
  lazy val munitCatsEffect =
    Def.setting("org.typelevel" %%% "munit-cats-effect" % "2.0.0")
}
