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

class UlidSpec extends CatsEffectSuite {

  test("Ulid.fromString parses valid ULIDs") {
    val validUlid = "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    val result = Ulid.fromString(validUlid)
    assert(result.isRight)
    result.foreach { ulid =>
      assertEquals(ulid.value, validUlid)
    }
  }

  test("Ulid.fromString rejects invalid ULIDs") {
    val invalidUlid = "invalid"
    val result = Ulid.fromString(invalidUlid)
    assert(result.isLeft)
  }

  test("Ulid.fromStringOption returns Some for valid ULIDs") {
    val validUlid = "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    val result = Ulid.fromStringOption(validUlid)
    assert(result.isDefined)
    result.foreach { ulid =>
      assertEquals(ulid.value, validUlid)
    }
  }

  test("Ulid.fromStringOption returns None for invalid ULIDs") {
    val invalidUlid = "invalid"
    val result = Ulid.fromStringOption(invalidUlid)
    assertEquals(result, None)
  }

  test("Ulid.unsafeFromString throws for invalid ULIDs") {
    val invalidUlid = "invalid"
    intercept[IllegalArgumentException] {
      Ulid.unsafeFromString(invalidUlid)
    }
  }

  test("Ulid.timestamp extracts correct timestamp") {
    val ulid = Ulid.unsafeFromString("01ARZ3NDEKTSV4RRFFQ69G5FAV")
    val timestamp = ulid.timestamp
    assert(timestamp > 0L)
    assert(timestamp <= UlidCodec.MaxTimestamp)
  }

  test("Ulid.toBytes and fromBytes are inverses") {
    val original = Ulid.unsafeFromString("01ARZ3NDEKTSV4RRFFQ69G5FAV")
    val bytes = original.toBytes
    val restored = Ulid.fromBytes(bytes)

    assert(restored.isRight)
    restored.foreach { ulid =>
      assertEquals(ulid.value, original.value)
    }
  }

  test("Ulid ordering is lexicographic") {
    val ulid1 = Ulid.unsafeFromString("01ARZ3NDEKTSV4RRFFQ69G5FAV")
    val ulid2 = Ulid.unsafeFromString("01BX5ZZKBKACTAV9WEVGEMMVRY")

    assert(Ulid.ordering.lt(ulid1, ulid2))
    assert(!Ulid.ordering.lt(ulid2, ulid1))
  }

  test("Ulid.toString returns the value") {
    val ulidStr = "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    val ulid = Ulid.unsafeFromString(ulidStr)
    assertEquals(ulid.toString, ulidStr)
  }

  test("Ulid normalizes to uppercase") {
    val lowercase = "01arz3ndektsv4rrffq69g5fav"
    val ulid = Ulid.fromString(lowercase)
    assert(ulid.isRight)
    ulid.foreach { u =>
      assertEquals(u.value, lowercase.toUpperCase)
    }
  }
}
