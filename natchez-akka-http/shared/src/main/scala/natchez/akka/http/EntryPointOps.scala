package natchez.akka.http

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Route
import cats.data.Kleisli
import cats.effect.Async
import cats.syntax.all.*
import natchez.akka.http.HasFuture.HasFutureOps
import natchez.{EntryPoint, Kernel, Span}

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

  private def toKernel(request: HttpRequest): Kernel = {
    val headers           = request.headers
    val traceId           = "X-Natchez-Trace-Id"
    val parentSpanId      = "X-Natchez-Parent-Span-Id"
    val maybeTraceId      =
      headers.find(_.lowercaseName() == traceId.toLowerCase).map(header => (traceId, header.value()))
    val maybeParentSpanId =
      headers.find(_.lowercaseName() == parentSpanId.toLowerCase).map(header => (parentSpanId, header.value()))
    Kernel((maybeTraceId.toList ++ maybeParentSpanId.toList).toMap)
  }
}

trait ToEntryPointOps {

  implicit def toEntryPointOps[F[_]](ep: EntryPoint[F]): EntryPointOps[F] =
    new EntryPointOps[F] {
      val self: EntryPoint[F] = ep
    }
}

object entrypoint extends ToEntryPointOps
