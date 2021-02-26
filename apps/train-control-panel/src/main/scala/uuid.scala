package es.eriktorr

import cats.effect.Sync

import java.util.UUID

object uuid {
  trait UUIDGenerator[F[_]] {
    def next: F[UUID]
  }

  object UUIDGenerator {
    def apply[F[_]](implicit ev: UUIDGenerator[F]): UUIDGenerator[F] = ev

    implicit def syncUUIDs[F[_]: Sync]: UUIDGenerator[F] = new UUIDGenerator[F] {
      override def next: F[UUID] = F.delay(UUID.randomUUID())
    }
  }
}
