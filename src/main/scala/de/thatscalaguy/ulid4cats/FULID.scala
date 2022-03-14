package de.thatscalaguy.ulid4cats

import net.petitviolet.ulid4s.ULID
import cats.effect.kernel.Sync
import cats._
import cats.data._
import cats.implicits._

trait FULID[F[_]] {
  def generate: F[String]

  def timeStamp(ulid: String): F[Option[Long]]

  def isValid(ulid: String): F[Boolean]
}

object FULID {
  def apply[F[_]](implicit ev: FULID[F]): FULID[F] = ev

  implicit def instance[F[_]: Sync] = new FULID[F] {
    override def generate: F[String] = Sync[F].delay(ULID.generate)

    override def timeStamp(ulid: String): F[Option[Long]] =
      Sync[F].delay(ULID.timestamp(ulid))

    override def isValid(ulid: String): F[Boolean] =
      Sync[F].delay(ULID.isValid(ulid))

  }
}
