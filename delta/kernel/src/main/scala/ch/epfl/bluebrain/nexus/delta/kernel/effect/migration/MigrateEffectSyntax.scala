package ch.epfl.bluebrain.nexus.delta.kernel.effect.migration

import cats.effect.IO
import monix.bio.{IO => BIO, UIO}
import monix.execution.Scheduler.Implicits.global

import scala.reflect.ClassTag

trait MigrateEffectSyntax {

  implicit def toCatsIO[E <: Throwable, A](io: BIO[E, A]): IO[A] = io.to[IO]

  implicit def toMonixBIOOps[A](io: IO[A]): CatsIOToBioOps[A] = new CatsIOToBioOps(io)

}

final class CatsIOToBioOps[A](private val io: IO[A]) extends AnyVal {
  def toBIO[E <: Throwable](implicit E: ClassTag[E]): BIO[E, A] =
    BIO.from(io).mapErrorPartialWith {
      case E(e)  => monix.bio.IO.raiseError(e)
      case other => BIO.terminate(other)
    }

  def toUIO: UIO[A] = BIO.from(io).hideErrors
}