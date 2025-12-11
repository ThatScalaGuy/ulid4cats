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

import cats.effect.Sync

private[ulid4cats] trait RandomSourcePlatform {

  /** Creates a RandomSource that uses a cryptographically secure random number
    * generator. This is the recommended default for production use on the JVM.
    *
    * @tparam F
    *   the effect type with Sync constraint
    * @return
    *   a RandomSource using SecureRandom
    */
  def secureRandom[F[_]: Sync]: RandomSource[F] = new RandomSource[F] {
    def nextRandomness: F[Array[Byte]] = Sync[F].delay {
      val bytes = new Array[Byte](10)
      new java.security.SecureRandom().nextBytes(bytes)
      bytes
    }
  }

  /** Creates a deterministic RandomSource from a seed for testing purposes.
    * Uses java.util.Random which is deterministic given a seed.
    *
    * @param seed
    *   the random seed
    * @tparam F
    *   the effect type with Sync constraint
    * @return
    *   a RandomSource that produces deterministic values
    */
  def fromSeed[F[_]: Sync](seed: Long): F[RandomSource[F]] = Sync[F].delay {
    val rng = new java.util.Random(seed)
    new RandomSource[F] {
      def nextRandomness: F[Array[Byte]] = Sync[F].delay {
        val bytes = new Array[Byte](10)
        rng.nextBytes(bytes)
        bytes
      }
    }
  }
}
