/*
 * Copyright (c) 2025 ThatScalaGuy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.thatscalaguy.ulid4cats

import cats.effect.IO
import cats.Applicative
import munit.CatsEffectSuite

class UlidGenSpec extends CatsEffectSuite {

  test("random generator produces valid ULIDs") {
    val fixedTimestamp = 1702300800000L
    val fixedRandomness = Array.fill[Byte](10)(0x42)

    implicit val randomSource: RandomSource[IO] =
      RandomSource.constant[IO](fixedRandomness)
    implicit val timestampProvider: TimestampProvider[IO] =
      TimestampProvider.constant[IO](fixedTimestamp)

    val gen = UlidGen.random[IO]

    gen.next.map { ulid =>
      assertEquals(ulid.timestamp, fixedTimestamp)
      assert(UlidCodec.isValid(ulid.value))
    }
  }

  test("random generator produces different ULIDs with different randomness") {
    val fixedTimestamp = 1702300800000L

    implicit val timestampProvider: TimestampProvider[IO] =
      TimestampProvider.constant[IO](fixedTimestamp)

    for {
      randomSource <- RandomSource.fromSeed[IO](42L)
      gen = {
        implicit val rs: RandomSource[IO] = randomSource
        UlidGen.random[IO]
      }
      ulid1 <- gen.next
      ulid2 <- gen.next
    } yield {
      assert(ulid1.value != ulid2.value)
      assertEquals(ulid1.timestamp, fixedTimestamp)
      assertEquals(ulid2.timestamp, fixedTimestamp)
    }
  }

  test("monotonic generator produces monotonically increasing ULIDs") {
    val fixedTimestamp = 1702300800000L

    implicit val randomSource: RandomSource[IO] =
      RandomSource.constant[IO](Array.fill[Byte](10)(0x00))
    implicit val timestampProvider: TimestampProvider[IO] =
      TimestampProvider.constant[IO](fixedTimestamp)

    for {
      gen <- UlidGen.monotonic[IO]
      ulid1 <- gen.next
      ulid2 <- gen.next
      ulid3 <- gen.next
    } yield {
      assert(
        Ulid.ordering.lt(ulid1, ulid2),
        s"${ulid1.value} should be < ${ulid2.value}"
      )
      assert(
        Ulid.ordering.lt(ulid2, ulid3),
        s"${ulid2.value} should be < ${ulid3.value}"
      )
    }
  }

  test("monotonic generator uses new randomness for new timestamp") {
    var currentTimestamp = 1702300800000L

    implicit val randomSource: RandomSource[IO] = new RandomSource[IO] {
      var counter = 0
      def nextRandomness: IO[Array[Byte]] = IO {
        counter += 1
        Array.fill[Byte](10)(counter.toByte)
      }
    }

    implicit val timestampProvider: TimestampProvider[IO] =
      new TimestampProvider[IO] {
        def currentTimeMillis: IO[Long] = IO {
          val ts = currentTimestamp
          currentTimestamp += 1
          ts
        }
      }

    for {
      gen <- UlidGen.monotonic[IO]
      ulid1 <- gen.next
      ulid2 <- gen.next
    } yield {
      assert(ulid1.timestamp < ulid2.timestamp)
    }
  }

  test("randomDefault creates a working generator") {
    val gen = UlidGen.randomDefault[IO]

    for {
      ulid1 <- gen.next
      ulid2 <- gen.next
    } yield {
      assert(UlidCodec.isValid(ulid1.value))
      assert(UlidCodec.isValid(ulid2.value))
      assert(ulid1.value != ulid2.value)
    }
  }

  test("monotonicDefault creates a working generator") {
    for {
      gen <- UlidGen.monotonicDefault[IO]
      ulid1 <- gen.next
      ulid2 <- gen.next
    } yield {
      assert(UlidCodec.isValid(ulid1.value))
      assert(UlidCodec.isValid(ulid2.value))
    }
  }
}
