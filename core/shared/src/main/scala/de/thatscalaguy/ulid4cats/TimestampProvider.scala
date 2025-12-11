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

import cats.{Applicative, Functor}
import cats.effect.Clock
import cats.syntax.all._

/** Abstraction for obtaining the current timestamp.
  *
  * This trait wraps Clock semantics and enables deterministic testing by
  * injecting a mock timestamp provider.
  *
  * @tparam F
  *   the effect type
  */
trait TimestampProvider[F[_]] {

  /** Returns the current timestamp in milliseconds since Unix epoch.
    *
    * @return
    *   an effect producing the current timestamp
    */
  def currentTimeMillis: F[Long]
}

object TimestampProvider {

  /** Summons the implicit TimestampProvider instance.
    */
  def apply[F[_]](implicit ev: TimestampProvider[F]): TimestampProvider[F] = ev

  /** Creates a TimestampProvider from a cats-effect Clock.
    *
    * @tparam F
    *   the effect type with Clock and Functor constraints
    * @return
    *   a TimestampProvider using the Clock
    */
  def fromClock[F[_]: Clock: Functor]: TimestampProvider[F] =
    new TimestampProvider[F] {
      def currentTimeMillis: F[Long] = Clock[F].realTime.map(_.toMillis)
    }

  /** Creates a TimestampProvider that always returns a constant timestamp.
    * Useful for deterministic testing.
    *
    * @param millis
    *   the fixed timestamp in milliseconds
    * @tparam F
    *   the effect type with Applicative constraint
    * @return
    *   a TimestampProvider that always returns the same timestamp
    */
  def constant[F[_]: Applicative](millis: Long): TimestampProvider[F] =
    new TimestampProvider[F] {
      def currentTimeMillis: F[Long] = Applicative[F].pure(millis)
    }
}
