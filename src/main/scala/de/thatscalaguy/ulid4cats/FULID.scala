package de.thatscalaguy.ulid4cats

import cats.Applicative
import net.petitviolet.ulid4s.ULID
import javax.swing.text.StyledEditorKit.BoldAction

trait FULID[F[_]] {
    def generate: F[String]

    def timeStamp(ulid: String): F[Option[Long]]

    def isValid(ulid: String): F[Boolean]
}

object FULID {
    def apply[F[_]](implicit ev: FULID[F]): FULID[F] = ev

    implicit def instance[F[_]: Applicative] = new FULID[F] {

      override def generate: F[String] = Applicative[F].pure(ULID.generate)

      override def timeStamp(ulid: String): F[Option[Long]] = Applicative[F].pure(ULID.timestamp(ulid))

      override def isValid(ulid: String): F[Boolean] = Applicative[F].pure(ULID.isValid(ulid))

    }
}
