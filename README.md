# ulid4cats

![Cats Friendly Badge](https://typelevel.org/cats/img/cats-badge-tiny.png)
[![ulid4cats Scala version support](https://index.scala-lang.org/thatscalaguy/ulid4cats/ulid4cats/latest-by-scala-version.svg?platform=jvm)](https://index.scala-lang.org/thatscalaguy/ulid4cats/ulid4cats)
[![Maven Central](https://img.shields.io/maven-central/v/de.thatscalaguy/ulid4cats_2.13.svg)](https://maven-badges.herokuapp.com/maven-central/de.thatscalaguy/ulid4cats_2.13)

Small cats-effect wrapper around the [airframe-ulid](https://github.com/wvlet/airframe/tree/master/airframe-ulid) library. 

## Scala3 Sample

Add dependency to yor sbt file
```scala
libraryDependencies ++= Seq(
  "org.typelevel"   %% "cats-effect" % "3.5.0", // must be provided
  "de.thatscalaguy" %% "ulid4cats"   % "1.3.0",
)
```
Application print a new ulid to the console.
```scala
import cats.effect.{IO, IOApp, ExitCode}
import de.thatscalaguy.ulid4cats.FULID

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] = for {
    id <- FULID[IO].generate
    _  <- IO.println(id)
  } yield ExitCode.Success
```