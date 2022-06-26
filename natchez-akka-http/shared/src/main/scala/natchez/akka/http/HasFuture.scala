package natchez.akka.http

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.{Id, ~>}
import natchez.Span

import scala.concurrent.Future

trait HasFuture[F[_]] extends ~>[F, Future] {
  def unsafeToFuture[A](fa: F[A]): Future[A] = apply(fa)
}

object HasFuture {
  def apply[F[_]](implicit ev: HasFuture[F]): ev.type = ev

  implicit class HasFutureOps[F[_]: HasFuture, A](fa: F[A]) {
    def unsafeToFuture(): Future[A] = implicitly[HasFuture[F]].unsafeToFuture(fa)
  }

  implicit def FkHasFuture[F[_]](implicit fk: ~>[F, Future]): HasFuture[F] = new HasFuture[F] {
    override def apply[A](fa: F[A]): Future[A] = fk(fa)
  }

  implicit val ioHasFuture: HasFuture[IO] = new HasFuture[IO] {
    override def apply[A](fa: IO[A]): Future[A] = fa.unsafeToFuture()
  }

  implicit val idHasFuture: HasFuture[Id] = new HasFuture[Id] {
    override def apply[A](fa: Id[A]): Future[A] = Future.successful(fa)
  }

  implicit def kleisliHasFuture[F[_]](implicit HF: HasFuture[F], span: Span[F]): HasFuture[Kleisli[F, Span[F], *]] =
    new HasFuture[Kleisli[F, Span[F], *]] {
      override def apply[A](fa: Kleisli[F, Span[F], A]): Future[A] = HF.unsafeToFuture(fa.run(span))
    }
}
