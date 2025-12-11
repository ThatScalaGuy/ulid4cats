# ulid4cats

![Cats Friendly Badge](https://typelevel.org/cats/img/cats-badge-tiny.png)
[![ulid4cats Scala version support](https://index.scala-lang.org/thatscalaguy/ulid4cats/ulid4cats/latest-by-scala-version.svg?platform=jvm)](https://index.scala-lang.org/thatscalaguy/ulid4cats/ulid4cats)
[![Maven Central](https://img.shields.io/maven-central/v/de.thatscalaguy/ulid4cats_2.13.svg)](https://maven-badges.herokuapp.com/maven-central/de.thatscalaguy/ulid4cats_2.13)

A pure, tagless-final [ULID](https://github.com/ulid/spec) implementation for Scala, built on [Cats](https://typelevel.org/cats/) and [Cats Effect](https://typelevel.org/cats-effect/).

## Features

- **Pure Scala implementation** — no external ULID dependencies
- **Cross-platform** — JVM, Scala.js, and Scala Native
- **Cross-build** — Scala 2.13 and Scala 3
- **Tagless-final** — polymorphic in effect type `F[_]`
- **Type-safe** — `Ulid` value type (opaque in Scala 3, AnyVal in Scala 2)
- **Monotonic generation** — optional strictly-increasing ULIDs within the same millisecond
- **Testable** — injectable `RandomSource` and `TimestampProvider` for deterministic tests

## Installation

Add the dependency to your `build.sbt`:

```scala
libraryDependencies += "de.thatscalaguy" %%% "ulid4cats" % "2.0.0"
```

For JVM-only projects, you can use `%%` instead of `%%%`.

## Quick Start

### Basic Usage (Scala 3)

```scala
import cats.effect.{IO, IOApp, ExitCode}
import de.thatscalaguy.ulid4cats.{FULID, Ulid}

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] = for {
    // Generate a typed Ulid
    ulid <- FULID[IO].generateUlid
    _    <- IO.println(s"ULID: ${ulid.value}")
    _    <- IO.println(s"Timestamp: ${ulid.timestamp}")

    // Or generate as String (backward compatible)
    ulidStr <- FULID[IO].generate
    _       <- IO.println(s"ULID String: $ulidStr")
  } yield ExitCode.Success
```

### Basic Usage (Scala 2)

```scala
import cats.effect.{IO, IOApp, ExitCode}
import de.thatscalaguy.ulid4cats.{FULID, Ulid}

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = for {
    ulid <- FULID[IO].generateUlid
    _    <- IO.println(s"ULID: ${ulid.value}")
  } yield ExitCode.Success
}
```

### Using `UlidGen` Directly

For more control, use the `UlidGen` algebra directly:

```scala
import cats.effect.IO
import de.thatscalaguy.ulid4cats.UlidGen

// Random generator (new randomness each call)
val randomGen: UlidGen[IO] = UlidGen.randomDefault[IO]

// Monotonic generator (strictly increasing within same millisecond)
val monotonicGen: IO[UlidGen[IO]] = UlidGen.monotonicDefault[IO]
```

### Parsing and Validation

```scala
import de.thatscalaguy.ulid4cats.{Ulid, UlidCodec}

// Safe parsing
val parsed: Either[UlidError, Ulid] = Ulid.fromString("01ARZ3NDEKTSV4RRFFQ69G5FAV")
val parsedOpt: Option[Ulid] = Ulid.fromStringOption("01ARZ3NDEKTSV4RRFFQ69G5FAV")

// Unsafe parsing (throws on invalid input)
val ulid: Ulid = Ulid.unsafeFromString("01ARZ3NDEKTSV4RRFFQ69G5FAV")

// Validation
val isValid: Boolean = UlidCodec.isValid("01ARZ3NDEKTSV4RRFFQ69G5FAV")

// Extract components
val timestamp: Long = ulid.timestamp
val bytes: Array[Byte] = ulid.toBytes
```

### Deterministic Testing

Inject test doubles for reproducible tests:

```scala
import cats.effect.IO
import de.thatscalaguy.ulid4cats.{UlidGen, RandomSource, TimestampProvider}

val fixedTimestamp = 1702300800000L
val fixedRandomness = Array.fill[Byte](10)(0x42)

implicit val randomSource: RandomSource[IO] = RandomSource.constant[IO](fixedRandomness)
implicit val timestampProvider: TimestampProvider[IO] = TimestampProvider.constant[IO](fixedTimestamp)

val deterministicGen: UlidGen[IO] = UlidGen.random[IO]
// All generated ULIDs will have the same timestamp and randomness
```

## API Overview

### `Ulid` — Value Type

- `Ulid.fromString(s: String): Either[UlidError, Ulid]`
- `Ulid.fromStringOption(s: String): Option[Ulid]`
- `Ulid.unsafeFromString(s: String): Ulid`
- `Ulid.fromBytes(bytes: Array[Byte]): Either[UlidError, Ulid]`
- `ulid.value: String` — the 26-character ULID string
- `ulid.timestamp: Long` — milliseconds since Unix epoch
- `ulid.toBytes: Array[Byte]` — 16-byte representation

### `UlidGen[F[_]]` — Generator Algebra

- `UlidGen.random[F]` — random generator (requires implicit `RandomSource` and `TimestampProvider`)
- `UlidGen.randomDefault[F]` — random generator with default impls
- `UlidGen.monotonic[F]` — monotonic generator (strictly increasing)
- `UlidGen.monotonicDefault[F]` — monotonic generator with default impls

### `FULID[F[_]]` — Backward-Compatible API

- `FULID[F].generate: F[String]` — generate ULID as String
- `FULID[F].generateUlid: F[Ulid]` — generate typed Ulid
- `FULID[F].isValid(s: String): F[Boolean]`
- `FULID[F].timeStamp(s: String): F[Option[Long]]`
- `FULID[F].parseUlid(s: String): F[Option[Ulid]]`

## ULID Specification

This library implements the [ULID specification](https://github.com/ulid/spec):

- 128-bit identifier (same size as UUID)
- 26-character Crockford Base32 encoding
- Lexicographically sortable
- Case-insensitive (normalized to uppercase)
- 48-bit timestamp (milliseconds since Unix epoch)
- 80-bit randomness

## License

MIT License — see [LICENSE](LICENSE) for details.
