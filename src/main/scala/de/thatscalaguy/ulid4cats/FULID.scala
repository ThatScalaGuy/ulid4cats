/*
 * Copyright (c) 2021 ThatScalaGuy
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

import cats.implicits._
import wvlet.airframe.ulid.ULID
import cats.effect.kernel.Sync

trait FULID[F[_]] {
  def generate: F[String]

  def timeStamp(ulid: String): F[Option[Long]]

  def isValid(ulid: String): F[Boolean]
}

object FULID {
  def apply[F[_]](implicit ev: FULID[F]): ev.type = ev

  implicit def instance[F[_]: Sync]: FULID[F] = new FULID[F] {
    override def generate: F[String] = Sync[F].delay(ULID.newULIDString)

    override def timeStamp(ulid: String): F[Option[Long]] =
      Sync[F]
        .delay(ULID.fromString(ulid).epochMillis.some)
        .recover { case _ =>
          none[Long]
        }

    override def isValid(ulid: String): F[Boolean] =
      Sync[F].delay(ULID.isValid(ulid))

  }
}
