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

import cats.Apply
import cats.effect.{Clock, Ref, Sync}
import cats.syntax.all._

/** Algebra for generating ULIDs in an effectful context.
  *
  * This is the primary API for ULID generation, providing both random and
  * monotonic generation strategies.
  *
  * @tparam F
  *   the effect type
  */
trait UlidGen[F[_]] {

  /** Generates a new ULID.
    *
    * @return
    *   an effect producing a new Ulid
    */
  def next: F[Ulid]
}

object UlidGen {

  /** Summons the implicit UlidGen instance.
    */
  def apply[F[_]](implicit ev: UlidGen[F]): UlidGen[F] = ev

  /** Creates a random ULID generator.
    *
    * Each call to `next` generates a completely random ULID based on the
    * current timestamp and random bytes. This is suitable for most use cases
    * where strict monotonicity within the same millisecond is not required.
    *
    * @param R
    *   the RandomSource for generating random bytes
    * @param T
    *   the TimestampProvider for getting the current time
    * @param F
    *   the effect type with Apply constraint
    * @return
    *   a UlidGen that produces random ULIDs
    */
  def random[F[_]: Apply](implicit
      R: RandomSource[F],
      T: TimestampProvider[F]
  ): UlidGen[F] =
    new UlidGen[F] {
      def next: F[Ulid] =
        (T.currentTimeMillis, R.nextRandomness).mapN {
          (timestamp, randomness) =>
            Ulid.fromParts(timestamp, randomness)
        }
    }

  /** Creates a random ULID generator using secure random and system clock.
    *
    * This is a convenience constructor that uses the default platform
    * implementations for randomness and time.
    *
    * @tparam F
    *   the effect type with Sync and Clock constraints
    * @return
    *   a UlidGen that produces random ULIDs
    */
  def randomDefault[F[_]: Sync: Clock]: UlidGen[F] = {
    implicit val R: RandomSource[F] = RandomSource.secureRandom[F]
    implicit val T: TimestampProvider[F] = TimestampProvider.fromClock[F]
    random[F]
  }

  /** State for monotonic ULID generation.
    *
    * @param lastTimestamp
    *   the timestamp of the last generated ULID
    * @param lastRandomness
    *   the randomness of the last generated ULID
    */
  private final case class MonotonicState(
      lastTimestamp: Long,
      lastRandomness: Array[Byte]
  )

  /** Creates a monotonic ULID generator wrapped in an effect.
    *
    * Monotonic generation ensures that ULIDs generated within the same
    * millisecond are strictly increasing by incrementing the randomness
    * portion. This is useful when generating many ULIDs in quick succession and
    * strict ordering is required.
    *
    * Returns an error if the randomness overflows (after generating 2^80 ULIDs
    * in the same millisecond, which is practically impossible).
    *
    * @param R
    *   the RandomSource for generating random bytes
    * @param T
    *   the TimestampProvider for getting the current time
    * @param F
    *   the effect type with Sync constraint
    * @return
    *   an effect producing a UlidGen with monotonic ordering
    */
  def monotonic[F[_]: Sync](implicit
      R: RandomSource[F],
      T: TimestampProvider[F]
  ): F[UlidGen[F]] =
    Ref[F].of(MonotonicState(0L, new Array[Byte](10))).map { stateRef =>
      new UlidGen[F] {
        def next: F[Ulid] =
          T.currentTimeMillis.flatMap { timestamp =>
            stateRef
              .modify { state =>
                if (timestamp > state.lastTimestamp) {
                  (
                    MonotonicState(timestamp, new Array[Byte](10)),
                    R.nextRandomness.map { randomness =>
                      Ulid.fromParts(timestamp, randomness)
                    }
                  )
                } else {
                  UlidCodec.incrementRandomness(state.lastRandomness) match {
                    case Right(newRandomness) =>
                      val ulid =
                        Ulid.fromParts(state.lastTimestamp, newRandomness)
                      (
                        MonotonicState(state.lastTimestamp, newRandomness),
                        Sync[F].pure(ulid)
                      )
                    case Left(_) =>
                      (
                        state,
                        Sync[F].raiseError[Ulid](
                          new RuntimeException(
                            "Randomness overflow during monotonic ULID generation"
                          )
                        )
                      )
                  }
                }
              }
              .flatMap { result =>
                result.flatMap { ulid =>
                  stateRef
                    .update(s => s.copy(lastRandomness = ulid.randomness))
                    .as(ulid)
                }
              }
          }
      }
    }

  /** Creates a monotonic ULID generator using secure random and system clock.
    *
    * This is a convenience constructor that uses the default platform
    * implementations for randomness and time.
    *
    * @tparam F
    *   the effect type with Sync and Clock constraints
    * @return
    *   an effect producing a UlidGen with monotonic ordering
    */
  def monotonicDefault[F[_]: Sync: Clock]: F[UlidGen[F]] = {
    implicit val R: RandomSource[F] = RandomSource.secureRandom[F]
    implicit val T: TimestampProvider[F] = TimestampProvider.fromClock[F]
    monotonic[F]
  }
}
