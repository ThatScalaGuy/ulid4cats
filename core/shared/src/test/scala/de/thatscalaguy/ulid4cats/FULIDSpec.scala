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
import munit.CatsEffectSuite

class FULIDSpec extends CatsEffectSuite {

  test("FULID[IO].generate produces valid ULID strings") {
    val fulid = FULID[IO]

    for {
      ulidStr <- fulid.generate
    } yield {
      assertEquals(ulidStr.length, 26)
      assert(UlidCodec.isValid(ulidStr))
    }
  }

  test("FULID[IO].generateUlid produces valid Ulid instances") {
    val fulid = FULID[IO]

    for {
      ulid <- fulid.generateUlid
    } yield {
      assertEquals(ulid.value.length, 26)
      assert(UlidCodec.isValid(ulid.value))
    }
  }

  test("FULID[IO].timeStamp extracts timestamp from valid string") {
    val fulid = FULID[IO]
    val validUlid = "01ARZ3NDEKTSV4RRFFQ69G5FAV"

    for {
      timestampOpt <- fulid.timeStamp(validUlid)
    } yield {
      assert(timestampOpt.isDefined)
      timestampOpt.foreach { ts =>
        assert(ts > 0L)
      }
    }
  }

  test("FULID[IO].timeStamp returns None for invalid string") {
    val fulid = FULID[IO]

    for {
      timestampOpt <- fulid.timeStamp("invalid")
    } yield {
      assertEquals(timestampOpt, None)
    }
  }

  test("FULID[IO].ulidTimeStamp extracts timestamp from Ulid") {
    val fulid = FULID[IO]
    val ulid = Ulid.unsafeFromString("01ARZ3NDEKTSV4RRFFQ69G5FAV")

    for {
      timestamp <- fulid.ulidTimeStamp(ulid)
    } yield {
      assert(timestamp > 0L)
    }
  }

  test("FULID[IO].isValid returns true for valid ULIDs") {
    val fulid = FULID[IO]

    for {
      isValid <- fulid.isValid("01ARZ3NDEKTSV4RRFFQ69G5FAV")
    } yield {
      assert(isValid)
    }
  }

  test("FULID[IO].isValid returns false for invalid ULIDs") {
    val fulid = FULID[IO]

    for {
      isValid <- fulid.isValid("invalid")
    } yield {
      assert(!isValid)
    }
  }

  test("FULID[IO].parseUlid returns Some for valid ULIDs") {
    val fulid = FULID[IO]

    for {
      ulidOpt <- fulid.parseUlid("01ARZ3NDEKTSV4RRFFQ69G5FAV")
    } yield {
      assert(ulidOpt.isDefined)
    }
  }

  test("FULID[IO].parseUlid returns None for invalid ULIDs") {
    val fulid = FULID[IO]

    for {
      ulidOpt <- fulid.parseUlid("invalid")
    } yield {
      assertEquals(ulidOpt, None)
    }
  }

  test("FULID.fromUlidGen works with custom generator") {
    val fixedTimestamp = 1702300800000L
    val fixedRandomness = Array.fill[Byte](10)(0x42)

    implicit val randomSource: RandomSource[IO] =
      RandomSource.constant[IO](fixedRandomness)
    implicit val timestampProvider: TimestampProvider[IO] =
      TimestampProvider.constant[IO](fixedTimestamp)

    val gen = UlidGen.random[IO]
    val fulid = FULID.fromUlidGen[IO](gen)

    for {
      ulid <- fulid.generateUlid
    } yield {
      assertEquals(ulid.timestamp, fixedTimestamp)
    }
  }
}
