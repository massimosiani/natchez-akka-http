package natchez.akka.http

import akka.http.scaladsl.server.Route
import cats.data.Kleisli
import cats.effect.Async
import cats.syntax.all.*
import natchez.akka.http.AkkaRequest.toKernel
import natchez.akka.http.HasFuture.HasFutureOps
import natchez.{EntryPoint, Span}

trait EntryPointOps[F[_]] {

  def self: EntryPoint[F]

  def liftT(routes: Kleisli[F, Span[F], Route])(implicit A: Async[F], HF: HasFuture[F]): Route = { requestContext =>
    val request = requestContext.request
    val kernel  = toKernel(request)
  self
    .continueOrElseRoot(name = request.uri.path.toString(), kernel = kernel)
    .use(span => routes.run(span).flatMap(route => A.fromFuture(A.delay(route(requestContext)))))
    .unsafeToFuture()
  }
}

trait ToEntryPointOps {

  implicit def toEntryPointOps[F[_]](ep: EntryPoint[F]): EntryPointOps[F] =
    new EntryPointOps[F] {
      val self: EntryPoint[F] = ep
    }
}

object entrypoint extends ToEntryPointOps
