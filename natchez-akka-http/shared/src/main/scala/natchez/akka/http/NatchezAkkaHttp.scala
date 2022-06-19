package natchez.akka.http

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import cats.effect.IO
import cats.effect.kernel.Outcome
import cats.effect.unsafe.IORuntime
import natchez._

object NatchezAkkaHttp {

  def server(entryPoint: EntryPoint[IO])(routes: Route)(implicit ioRuntime: IORuntime): Route = { requestContext =>
    val request = requestContext.request
    val kernel  = toKernel(request)
    entryPoint
      .continueOrElseRoot(name = request.uri.path.toString(), kernel = kernel)
      .use { span =>
        Trace.ioTrace(span).flatMap { implicit trace =>
          IO.fromFuture(IO.delay(routes(requestContext))).guaranteeCase {
            case Outcome.Succeeded(fa) =>
              fa.flatMap {
                case Complete(response) => addRequestFields(request) *> addResponseFields(response)
                case _: Rejected        => IO.unit
              }
            case Outcome.Errored(e)    => addRequestFields(request) *> addErrorFields(e)
            case Outcome.Canceled()    =>
              addRequestFields(request) *> trace.put(("cancelled", TraceValue.BooleanValue(true)), Tags.error(true))
          }
        }
      }
      .unsafeToFuture()
  }

  private def addErrorFields[F[_]: Trace](e: Throwable): F[Unit] =
    Trace[F].put(Tags.error(true), "error.message" -> e.getMessage())

  private def addRequestFields[F[_]: Trace](req: HttpRequest): F[Unit] =
    Trace[F].put(Tags.http.method(req.method.name), Tags.http.url(req.uri.toString()))

  private def addResponseFields[F[_]: Trace](res: HttpResponse): F[Unit] =
    Trace[F].put(Tags.http.status_code(res.status.intValue().toString))

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
