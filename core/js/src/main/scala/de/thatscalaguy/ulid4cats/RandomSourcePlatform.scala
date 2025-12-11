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
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

private[ulid4cats] trait RandomSourcePlatform {

  /** Creates a RandomSource that uses the Web Crypto API for cryptographically
    * secure random number generation. This is the recommended default for
    * production use in Scala.js/browser environments.
    *
    * @tparam F
    *   the effect type with Sync constraint
    * @return
    *   a RandomSource using Web Crypto API
    */
  def secureRandom[F[_]: Sync]: RandomSource[F] = new RandomSource[F] {
    def nextRandomness: F[Array[Byte]] = Sync[F].delay {
      val uint8Array = new Uint8Array(10)
      js.Dynamic.global.crypto.getRandomValues(uint8Array)
      val bytes = new Array[Byte](10)
      var i = 0
      while (i < 10) {
        bytes(i) = uint8Array(i).toByte
        i += 1
      }
      bytes
    }
  }

  /** Creates a deterministic RandomSource from a seed for testing purposes.
    * Uses a simple LCG (Linear Congruential Generator) for Scala.js
    * compatibility.
    *
    * @param seed
    *   the random seed
    * @tparam F
    *   the effect type with Sync constraint
    * @return
    *   a RandomSource that produces deterministic values
    */
  def fromSeed[F[_]: Sync](seed: Long): F[RandomSource[F]] = Sync[F].delay {
    var state = seed
    new RandomSource[F] {
      def nextRandomness: F[Array[Byte]] = Sync[F].delay {
        val bytes = new Array[Byte](10)
        var i = 0
        while (i < 10) {
          state = (state * 6364136223846793005L + 1442695040888963407L)
          bytes(i) = ((state >> 33) & 0xff).toByte
          i += 1
        }
        bytes
      }
    }
  }
}
