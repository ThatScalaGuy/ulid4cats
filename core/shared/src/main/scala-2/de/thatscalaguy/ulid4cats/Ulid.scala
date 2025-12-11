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

import cats.{Eq, Hash, Order, Show}

/** A Universally Unique Lexicographically Sortable Identifier (ULID).
  *
  * This Scala 2 implementation uses a value class for minimal allocation
  * overhead. ULIDs are 128-bit identifiers represented as 26-character Base32
  * strings.
  *
  * Structure:
  *   - First 10 characters: 48-bit timestamp (milliseconds since Unix epoch)
  *   - Last 16 characters: 80-bit random component
  *
  * @param value
  *   the underlying ULID string (uppercase, 26 characters)
  * @see
  *   [[https://github.com/ulid/spec ULID Specification]]
  */
final class Ulid private (val value: String) extends AnyVal {

  /** Extracts the timestamp (milliseconds since Unix epoch) from this ULID.
    */
  def timestamp: Long = UlidCodec.extractTimestamp(value)

  /** Extracts the 80-bit randomness from this ULID as a 10-byte array.
    */
  def randomness: Array[Byte] = UlidCodec.extractRandomness(value)

  /** Converts this ULID to a 16-byte array.
    */
  def toBytes: Array[Byte] = UlidCodec.toBytes(value)

  /** Returns the canonical string representation of this ULID.
    */
  override def toString: String = value
}

object Ulid {

  /** Parses a string into a ULID, returning an error if invalid.
    *
    * @param s
    *   the string to parse
    * @return
    *   Either an UlidError or a valid Ulid
    */
  def fromString(s: String): Either[UlidError, Ulid] =
    UlidCodec.validate(s).map(new Ulid(_))

  /** Parses a string into a ULID, returning None if invalid.
    *
    * @param s
    *   the string to parse
    * @return
    *   Some(Ulid) if valid, None otherwise
    */
  def fromStringOption(s: String): Option[Ulid] =
    fromString(s).toOption

  /** Parses a string into a ULID, throwing an exception if invalid.
    *
    * @param s
    *   the string to parse
    * @return
    *   a valid Ulid
    * @throws java.lang.IllegalArgumentException
    *   if the string is not a valid ULID
    */
  def unsafeFromString(s: String): Ulid =
    fromString(s).fold(
      err => throw new IllegalArgumentException(err.message),
      identity
    )

  /** Creates a ULID from a 16-byte array.
    *
    * @param bytes
    *   a 16-byte array
    * @return
    *   Either an UlidError or a valid Ulid
    */
  def fromBytes(bytes: Array[Byte]): Either[UlidError, Ulid] =
    UlidCodec.fromBytes(bytes).map(new Ulid(_))

  /** Creates a ULID from a 16-byte array, throwing an exception if invalid.
    *
    * @param bytes
    *   a 16-byte array
    * @return
    *   a valid Ulid
    * @throws java.lang.IllegalArgumentException
    *   if the byte array is invalid
    */
  def unsafeFromBytes(bytes: Array[Byte]): Ulid =
    fromBytes(bytes).fold(
      err => throw new IllegalArgumentException(err.message),
      identity
    )

  /** Creates a ULID from a timestamp and randomness. This is an internal
    * factory method.
    *
    * @param timestamp
    *   milliseconds since Unix epoch
    * @param randomness
    *   10 bytes of randomness
    * @return
    *   a Ulid
    */
  private[ulid4cats] def fromParts(
      timestamp: Long,
      randomness: Array[Byte]
  ): Ulid =
    new Ulid(UlidCodec.encode(timestamp, randomness))

  /** Lexicographic ordering for ULIDs. ULIDs are designed to be
    * lexicographically sortable.
    */
  implicit val ordering: Ordering[Ulid] = Ordering.by(_.value)

  /** Cats Order instance for Ulid.
    */
  implicit val order: Order[Ulid] = Order.by(_.value)

  /** Cats Eq instance for Ulid.
    */
  implicit val eq: Eq[Ulid] = Eq.by(_.value)

  /** Cats Hash instance for Ulid.
    */
  implicit val hash: Hash[Ulid] = Hash.by(_.value)

  /** Cats Show instance for Ulid.
    */
  implicit val show: Show[Ulid] = Show.show(_.value)
}
