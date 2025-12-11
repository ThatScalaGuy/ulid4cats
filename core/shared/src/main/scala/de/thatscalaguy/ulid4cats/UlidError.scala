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

import cats.{Eq, Show}

/** Sealed ADT representing errors that can occur during ULID parsing and
  * validation.
  */
sealed abstract class UlidError(val message: String)
    extends Product
    with Serializable

object UlidError {

  /** The input string has an invalid length (must be exactly 26 characters).
    *
    * @param actual
    *   the actual length of the input
    */
  final case class InvalidLength(actual: Int)
      extends UlidError(s"ULID must be 26 characters, got $actual")

  /** The input string contains an invalid character at the specified position.
    *
    * @param char
    *   the invalid character
    * @param position
    *   the 0-based position of the invalid character
    */
  final case class InvalidCharacter(char: Char, position: Int)
      extends UlidError(s"Invalid character '$char' at position $position")

  /** The timestamp portion of the ULID overflows the maximum allowed value (max
    * timestamp is 281474976710655, i.e., 2^48 - 1).
    */
  case object TimestampOverflow
      extends UlidError("Timestamp exceeds maximum value (281474976710655)")

  /** The randomness portion overflows when incrementing during monotonic
    * generation.
    */
  case object RandomnessOverflow
      extends UlidError("Randomness overflow during monotonic generation")

  /** The byte array has an invalid length (must be exactly 16 bytes).
    *
    * @param actual
    *   the actual length of the byte array
    */
  final case class InvalidByteLength(actual: Int)
      extends UlidError(s"ULID must be 16 bytes, got $actual")

  implicit val eqUlidError: Eq[UlidError] = Eq.fromUniversalEquals

  implicit val showUlidError: Show[UlidError] = Show.show(_.message)
}
