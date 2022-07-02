package natchez.akka.http

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server.{RequestContext, Route}
import cats.effect.kernel.Outcome
import cats.effect.std.Dispatcher
import cats.effect.syntax.all.*
import cats.effect.unsafe.IORuntime
import cats.effect.{Async, IO, Sync}
import cats.syntax.all.*
import natchez.*
import natchez.akka.http.AkkaRequest.toKernel

object NatchezAkkaHttp {

  def legacyServer(entryPoint: EntryPoint[IO])(routes: Route)(implicit ioRuntime: IORuntime): Route = {
    requestContext =>
      val request = requestContext.request
      val kernel  = toKernel(request)
      entryPoint
        .continueOrElseRoot(name = request.uri.path.toString(), kernel = kernel)
        .use(span => Trace.ioTrace(span).flatMap(implicit trace => add[IO](routes, requestContext)))
        .unsafeToFuture()
  }

  def server[F[_]: Async: Trace](routes: F[Route])(implicit D: Dispatcher[F]): F[Route] = Sync[F].delay {
    (requestContext: RequestContext) => D.unsafeToFuture(routes.flatMap(route => add(route, requestContext)))
  }

  private def add[F[_]: Async: Trace](route: Route, requestContext: RequestContext) =
    Async[F].fromFuture(Sync[F].delay(route(requestContext))).guaranteeCase {
      case Outcome.Succeeded(fa) =>
        fa.flatMap {
          case Complete(response) => addRequestFields(requestContext.request) *> addResponseFields(response)
          case _: Rejected        => Async[F].unit
        }
      case Outcome.Errored(e)    => addRequestFields(requestContext.request) *> addErrorFields(e)
      case Outcome.Canceled()    =>
        addRequestFields(requestContext.request) *> Trace[F]
          .put(("cancelled", TraceValue.BooleanValue(true)), Tags.error(true))
    }

  private def addErrorFields[F[_]: Trace](e: Throwable): F[Unit] =
    Trace[F].put(Tags.error(true), "error.message" -> e.getMessage())

  private def addRequestFields[F[_]: Trace](req: HttpRequest): F[Unit] =
    Trace[F].put(Tags.http.method(req.method.name), Tags.http.url(req.uri.toString()))

  private def addResponseFields[F[_]: Trace](res: HttpResponse): F[Unit] =
    Trace[F].put(Tags.http.status_code(res.status.intValue().toString))
}
