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

import cats.Applicative

/** Abstraction for generating random bytes.
  *
  * This trait allows for platform-specific implementations (JVM, JS, Native)
  * and enables deterministic testing by injecting a mock random source.
  *
  * @tparam F
  *   the effect type
  */
trait RandomSource[F[_]] {

  /** Generates 10 random bytes for the randomness portion of a ULID.
    *
    * @return
    *   an effect producing a 10-byte array
    */
  def nextRandomness: F[Array[Byte]]
}

object RandomSource extends RandomSourcePlatform {

  /** Summons the implicit RandomSource instance.
    */
  def apply[F[_]](implicit ev: RandomSource[F]): RandomSource[F] = ev

  /** Creates a deterministic RandomSource for testing purposes.
    *
    * @param bytes
    *   the fixed bytes to return (will be padded/truncated to 10 bytes)
    * @tparam F
    *   the effect type with Applicative constraint
    * @return
    *   a RandomSource that always returns the same bytes
    */
  def constant[F[_]: Applicative](bytes: Array[Byte]): RandomSource[F] =
    new RandomSource[F] {
      private val paddedBytes: Array[Byte] = {
        val result = new Array[Byte](10)
        System.arraycopy(bytes, 0, result, 0, math.min(bytes.length, 10))
        result
      }
      def nextRandomness: F[Array[Byte]] =
        Applicative[F].pure(paddedBytes.clone())
    }
}
