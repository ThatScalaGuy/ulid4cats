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

/** Pure encode/decode utilities for ULIDs following the ULID specification.
  *
  * A ULID is a 128-bit identifier represented as a 26-character string using
  * Crockford's Base32 encoding. The structure is:
  *   - First 10 characters: 48-bit timestamp (milliseconds since Unix epoch)
  *   - Last 16 characters: 80-bit random component
  *
  * @see
  *   [[https://github.com/ulid/spec ULID Specification]]
  */
object UlidCodec {

  /** Crockford's Base32 encoding alphabet (excludes I, L, O, U to avoid
    * ambiguity).
    */
  private[ulid4cats] val EncodingChars: Array[Char] =
    "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray

  /** Decoding lookup table mapping ASCII characters to their Base32 values.
    * Invalid characters are marked with -1.
    */
  private[ulid4cats] val DecodingChars: Array[Byte] = {
    val arr = Array.fill[Byte](128)(-1)
    arr('0') = 0; arr('O') = 0; arr('o') = 0
    arr('1') = 1; arr('I') = 1; arr('i') = 1; arr('L') = 1; arr('l') = 1
    arr('2') = 2
    arr('3') = 3
    arr('4') = 4
    arr('5') = 5
    arr('6') = 6
    arr('7') = 7
    arr('8') = 8
    arr('9') = 9
    arr('A') = 10; arr('a') = 10
    arr('B') = 11; arr('b') = 11
    arr('C') = 12; arr('c') = 12
    arr('D') = 13; arr('d') = 13
    arr('E') = 14; arr('e') = 14
    arr('F') = 15; arr('f') = 15
    arr('G') = 16; arr('g') = 16
    arr('H') = 17; arr('h') = 17
    arr('J') = 18; arr('j') = 18
    arr('K') = 19; arr('k') = 19
    arr('M') = 20; arr('m') = 20
    arr('N') = 21; arr('n') = 21
    arr('P') = 22; arr('p') = 22
    arr('Q') = 23; arr('q') = 23
    arr('R') = 24; arr('r') = 24
    arr('S') = 25; arr('s') = 25
    arr('T') = 26; arr('t') = 26
    arr('V') = 27; arr('v') = 27
    arr('W') = 28; arr('w') = 28
    arr('X') = 29; arr('x') = 29
    arr('Y') = 30; arr('y') = 30
    arr('Z') = 31; arr('z') = 31
    arr
  }

  /** Maximum timestamp value (2^48 - 1 = 281474976710655).
    */
  val MaxTimestamp: Long = 0xffffffffffffL

  /** Length of a ULID string (26 characters).
    */
  val UlidStringLength: Int = 26

  /** Length of a ULID in bytes (16 bytes = 128 bits).
    */
  val UlidByteLength: Int = 16

  /** Validates a ULID string and returns either an error or the normalized
    * (uppercased) string.
    *
    * @param ulid
    *   the ULID string to validate
    * @return
    *   Either an error or the validated (uppercased) ULID string
    */
  def validate(ulid: String): Either[UlidError, String] = {
    if (ulid.length != UlidStringLength)
      return Left(UlidError.InvalidLength(ulid.length))

    val upper = ulid.toUpperCase
    var i = 0
    while (i < UlidStringLength) {
      val c = upper.charAt(i)
      if (c >= 128 || DecodingChars(c) < 0)
        return Left(UlidError.InvalidCharacter(ulid.charAt(i), i))
      i += 1
    }

    val firstChar = upper.charAt(0)
    if (DecodingChars(firstChar) > 7)
      return Left(UlidError.TimestampOverflow)

    Right(upper)
  }

  /** Checks if a string is a valid ULID.
    *
    * @param ulid
    *   the string to check
    * @return
    *   true if the string is a valid ULID, false otherwise
    */
  def isValid(ulid: String): Boolean = validate(ulid).isRight

  /** Extracts the timestamp (milliseconds since Unix epoch) from a ULID string.
    *
    * @param ulid
    *   a valid, uppercase ULID string (26 characters)
    * @return
    *   the timestamp in milliseconds
    */
  def extractTimestamp(ulid: String): Long = {
    var timestamp = 0L
    var i = 0
    while (i < 10) {
      timestamp = (timestamp << 5) | DecodingChars(ulid.charAt(i))
      i += 1
    }
    timestamp
  }

  /** Extracts the 80-bit randomness from a ULID string as a byte array (10
    * bytes).
    *
    * @param ulid
    *   a valid, uppercase ULID string (26 characters)
    * @return
    *   the randomness as a 10-byte array
    */
  def extractRandomness(ulid: String): Array[Byte] = {
    val randomBytes = new Array[Byte](10)
    var bitBuffer = 0L
    var bitCount = 0
    var byteIndex = 0
    var i = 10

    while (i < 26 && byteIndex < 10) {
      bitBuffer = (bitBuffer << 5) | DecodingChars(ulid.charAt(i))
      bitCount += 5
      while (bitCount >= 8 && byteIndex < 10) {
        bitCount -= 8
        randomBytes(byteIndex) = ((bitBuffer >> bitCount) & 0xff).toByte
        byteIndex += 1
      }
      i += 1
    }

    randomBytes
  }

  /** Encodes a timestamp and randomness into a ULID string.
    *
    * @param timestamp
    *   milliseconds since Unix epoch (must be <= MaxTimestamp)
    * @param randomness
    *   10 bytes of randomness
    * @return
    *   a 26-character ULID string
    */
  def encode(timestamp: Long, randomness: Array[Byte]): String = {
    val chars = new Array[Char](UlidStringLength)

    chars(0) = EncodingChars(((timestamp >> 45) & 0x1f).toInt)
    chars(1) = EncodingChars(((timestamp >> 40) & 0x1f).toInt)
    chars(2) = EncodingChars(((timestamp >> 35) & 0x1f).toInt)
    chars(3) = EncodingChars(((timestamp >> 30) & 0x1f).toInt)
    chars(4) = EncodingChars(((timestamp >> 25) & 0x1f).toInt)
    chars(5) = EncodingChars(((timestamp >> 20) & 0x1f).toInt)
    chars(6) = EncodingChars(((timestamp >> 15) & 0x1f).toInt)
    chars(7) = EncodingChars(((timestamp >> 10) & 0x1f).toInt)
    chars(8) = EncodingChars(((timestamp >> 5) & 0x1f).toInt)
    chars(9) = EncodingChars((timestamp & 0x1f).toInt)

    var i = 0
    var bitBuffer = 0L
    var bitCount = 0
    var charIndex = 10

    while (i < 10) {
      bitBuffer = (bitBuffer << 8) | (randomness(i) & 0xff)
      bitCount += 8
      while (bitCount >= 5 && charIndex < 26) {
        bitCount -= 5
        chars(charIndex) = EncodingChars(((bitBuffer >> bitCount) & 0x1f).toInt)
        charIndex += 1
      }
      i += 1
    }

    new String(chars)
  }

  /** Converts a ULID string to a 16-byte array.
    *
    * @param ulid
    *   a valid, uppercase ULID string
    * @return
    *   a 16-byte array representing the ULID
    */
  def toBytes(ulid: String): Array[Byte] = {
    val bytes = new Array[Byte](16)
    val timestamp = extractTimestamp(ulid)

    bytes(0) = ((timestamp >> 40) & 0xff).toByte
    bytes(1) = ((timestamp >> 32) & 0xff).toByte
    bytes(2) = ((timestamp >> 24) & 0xff).toByte
    bytes(3) = ((timestamp >> 16) & 0xff).toByte
    bytes(4) = ((timestamp >> 8) & 0xff).toByte
    bytes(5) = (timestamp & 0xff).toByte

    val randomness = extractRandomness(ulid)
    System.arraycopy(randomness, 0, bytes, 6, 10)

    bytes
  }

  /** Converts a 16-byte array to a ULID string.
    *
    * @param bytes
    *   a 16-byte array
    * @return
    *   Either an error or a ULID string
    */
  def fromBytes(bytes: Array[Byte]): Either[UlidError, String] = {
    if (bytes.length != UlidByteLength)
      return Left(UlidError.InvalidByteLength(bytes.length))

    val timestamp =
      ((bytes(0).toLong & 0xff) << 40) |
        ((bytes(1).toLong & 0xff) << 32) |
        ((bytes(2).toLong & 0xff) << 24) |
        ((bytes(3).toLong & 0xff) << 16) |
        ((bytes(4).toLong & 0xff) << 8) |
        (bytes(5).toLong & 0xff)

    if (timestamp > MaxTimestamp)
      return Left(UlidError.TimestampOverflow)

    val randomness = new Array[Byte](10)
    System.arraycopy(bytes, 6, randomness, 0, 10)

    Right(encode(timestamp, randomness))
  }

  /** Increments the randomness portion of a ULID for monotonic generation.
    *
    * @param randomness
    *   a 10-byte randomness array
    * @return
    *   Either a RandomnessOverflow error or the incremented randomness
    */
  def incrementRandomness(
      randomness: Array[Byte]
  ): Either[UlidError, Array[Byte]] = {
    val result = randomness.clone()
    var i = 9
    var carry = true

    while (i >= 0 && carry) {
      val value = (result(i) & 0xff) + 1
      result(i) = (value & 0xff).toByte
      carry = value > 0xff
      i -= 1
    }

    if (carry) Left(UlidError.RandomnessOverflow)
    else Right(result)
  }
}
