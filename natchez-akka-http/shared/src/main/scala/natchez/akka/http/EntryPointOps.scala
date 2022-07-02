package natchez.akka.http

import akka.http.scaladsl.server.Route
import cats.data.Kleisli
import cats.effect.Async
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import natchez.akka.http.AkkaRequest.toKernel
import natchez.{EntryPoint, Span}

trait EntryPointOps[F[_]] {

  def self: EntryPoint[F]

  def liftT(routes: Kleisli[F, Span[F], Route])(implicit A: Async[F], D: Dispatcher[F]): Route = { requestContext =>
    val request = requestContext.request
    val kernel  = toKernel(request)
  D.unsafeToFuture(
    self
      .continueOrElseRoot(name = request.uri.path.toString(), kernel = kernel)
      .use(span => routes.run(span).flatMap(route => A.fromFuture(A.delay(route(requestContext)))))
  )
  }
}

trait ToEntryPointOps {

  implicit def toEntryPointOps[F[_]](ep: EntryPoint[F]): EntryPointOps[F] =
    new EntryPointOps[F] {
      val self: EntryPoint[F] = ep
    }
}

object entrypoint extends ToEntryPointOps
