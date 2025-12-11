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

class UlidCodecSpec extends CatsEffectSuite {

  test("encode and decode are inverses") {
    val timestamp = 1702300800000L
    val randomness = Array.fill[Byte](10)(0x42)
    val encoded = UlidCodec.encode(timestamp, randomness)

    assertEquals(encoded.length, 26)
    assertEquals(UlidCodec.extractTimestamp(encoded), timestamp)
  }

  test("validate accepts valid ULIDs") {
    val validUlids = List(
      "01ARZ3NDEKTSV4RRFFQ69G5FAV",
      "01BX5ZZKBKACTAV9WEVGEMMVRY",
      "7ZZZZZZZZZZZZZZZZZZZZZZZZZ"
    )
    validUlids.foreach { ulid =>
      assert(UlidCodec.validate(ulid).isRight, s"Expected $ulid to be valid")
    }
  }

  test("validate rejects invalid length") {
    assertEquals(
      UlidCodec.validate("01ARZ3NDEK"),
      Left(UlidError.InvalidLength(10))
    )
    assertEquals(
      UlidCodec.validate("01ARZ3NDEKTSV4RRFFQ69G5FAVX"),
      Left(UlidError.InvalidLength(27))
    )
  }

  test("validate rejects invalid characters") {
    val result = UlidCodec.validate("01ARZ3NDEKTSV4RRFFQ69G5FA!")
    assert(result.isLeft)
    result match {
      case Left(UlidError.InvalidCharacter(_, _)) => ()
      case other => fail(s"Expected InvalidCharacter, got $other")
    }
  }

  test("validate rejects timestamp overflow") {
    val result = UlidCodec.validate("8ZZZZZZZZZZZZZZZZZZZZZZZZZ")
    assertEquals(result, Left(UlidError.TimestampOverflow))
  }

  test("isValid returns correct results") {
    assert(UlidCodec.isValid("01ARZ3NDEKTSV4RRFFQ69G5FAV"))
    assert(!UlidCodec.isValid("invalid"))
    assert(!UlidCodec.isValid("01ARZ3NDEK"))
  }

  test("extractTimestamp returns correct value") {
    val ulid = "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    val timestamp = UlidCodec.extractTimestamp(ulid)
    assert(timestamp > 0L)
    assert(timestamp <= UlidCodec.MaxTimestamp)
  }

  test("toBytes and fromBytes are inverses") {
    val ulid = "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    val bytes = UlidCodec.toBytes(ulid)
    assertEquals(bytes.length, 16)

    val result = UlidCodec.fromBytes(bytes)
    assertEquals(result, Right(ulid))
  }

  test("fromBytes rejects invalid length") {
    assertEquals(
      UlidCodec.fromBytes(Array.fill[Byte](10)(0)),
      Left(UlidError.InvalidByteLength(10))
    )
  }

  test("incrementRandomness works correctly") {
    val randomness = Array.fill[Byte](10)(0x00)
    val result = UlidCodec.incrementRandomness(randomness)
    assert(result.isRight)
    result.foreach { incremented =>
      assertEquals(incremented(9), 1.toByte)
    }
  }

  test("incrementRandomness detects overflow") {
    val randomness = Array.fill[Byte](10)(0xff.toByte)
    val result = UlidCodec.incrementRandomness(randomness)
    assertEquals(result, Left(UlidError.RandomnessOverflow))
  }

  test("case insensitive parsing") {
    val upper = "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    val lower = "01arz3ndektsv4rrffq69g5fav"
    val mixed = "01ArZ3nDeKtSv4RrFfQ69g5FaV"

    val resultUpper = UlidCodec.validate(upper)
    val resultLower = UlidCodec.validate(lower)
    val resultMixed = UlidCodec.validate(mixed)

    assert(resultUpper.isRight)
    assert(resultLower.isRight)
    assert(resultMixed.isRight)

    assertEquals(resultUpper, resultLower)
    assertEquals(resultUpper, resultMixed)
  }
}
