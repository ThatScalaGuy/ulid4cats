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

import cats.effect.{Clock, Sync}
import cats.syntax.all._

/** Backward-compatible ULID generator trait.
  *
  * This trait provides the original FULID API for backward compatibility while
  * also exposing typed Ulid methods for new code.
  *
  * For new code, prefer using [[UlidGen]] directly.
  *
  * @tparam F
  *   the effect type
  */
trait FULID[F[_]] {

  /** Generates a new ULID as a String.
    *
    * @return
    *   an effect producing a 26-character ULID string
    */
  def generate: F[String]

  /** Generates a new typed Ulid.
    *
    * @return
    *   an effect producing a Ulid
    */
  def generateUlid: F[Ulid]

  /** Extracts the timestamp from a ULID string.
    *
    * @param ulid
    *   the ULID string
    * @return
    *   an effect producing Some(timestamp) if valid, None otherwise
    */
  def timeStamp(ulid: String): F[Option[Long]]

  /** Extracts the timestamp from a typed Ulid.
    *
    * @param ulid
    *   the Ulid
    * @return
    *   an effect producing the timestamp
    */
  def ulidTimeStamp(ulid: Ulid): F[Long]

  /** Checks if a string is a valid ULID.
    *
    * @param ulid
    *   the string to check
    * @return
    *   an effect producing true if valid, false otherwise
    */
  def isValid(ulid: String): F[Boolean]

  /** Parses a string into a typed Ulid.
    *
    * @param ulid
    *   the string to parse
    * @return
    *   an effect producing Some(Ulid) if valid, None otherwise
    */
  def parseUlid(ulid: String): F[Option[Ulid]]
}

object FULID {

  /** Summons the implicit FULID instance.
    */
  def apply[F[_]](implicit ev: FULID[F]): ev.type = ev

  /** Creates an implicit FULID instance using secure random and system clock.
    *
    * @tparam F
    *   the effect type with Sync and Clock constraints
    * @return
    *   a FULID instance
    */
  implicit def instance[F[_]: Sync: Clock]: FULID[F] = {
    val gen = UlidGen.randomDefault[F]
    new FULID[F] {
      override def generate: F[String] =
        gen.next.map(_.value)

      override def generateUlid: F[Ulid] =
        gen.next

      override def timeStamp(ulid: String): F[Option[Long]] =
        Sync[F].delay {
          Ulid.fromStringOption(ulid).map(_.timestamp)
        }

      override def ulidTimeStamp(ulid: Ulid): F[Long] =
        Sync[F].pure(ulid.timestamp)

      override def isValid(ulid: String): F[Boolean] =
        Sync[F].delay(UlidCodec.isValid(ulid))

      override def parseUlid(ulid: String): F[Option[Ulid]] =
        Sync[F].delay(Ulid.fromStringOption(ulid))
    }
  }

  /** Creates a FULID instance from an existing UlidGen.
    *
    * @param gen
    *   the UlidGen to use for generation
    * @tparam F
    *   the effect type with Sync constraint
    * @return
    *   a FULID instance
    */
  def fromUlidGen[F[_]: Sync](gen: UlidGen[F]): FULID[F] =
    new FULID[F] {
      override def generate: F[String] =
        gen.next.map(_.value)

      override def generateUlid: F[Ulid] =
        gen.next

      override def timeStamp(ulid: String): F[Option[Long]] =
        Sync[F].delay {
          Ulid.fromStringOption(ulid).map(_.timestamp)
        }

      override def ulidTimeStamp(ulid: Ulid): F[Long] =
        Sync[F].pure(ulid.timestamp)

      override def isValid(ulid: String): F[Boolean] =
        Sync[F].delay(UlidCodec.isValid(ulid))

      override def parseUlid(ulid: String): F[Option[Ulid]] =
        Sync[F].delay(Ulid.fromStringOption(ulid))
    }
}
