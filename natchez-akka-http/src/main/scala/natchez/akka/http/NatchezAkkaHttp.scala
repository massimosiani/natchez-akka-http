/*
 * Copyright 2023 Massimo Siani
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package natchez.akka.http

import akka.actor.ClassicActorSystemProvider
import akka.http.scaladsl.Http
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

import scala.concurrent.Future

object NatchezAkkaHttp {

  /** Adds the current span's kernel to the outgoing request, performs the request in a span called
    * `akka-http-client-request`, and adds the following fields to that span.
    *   - "client.http.method" -> "GET", "PUT", etc.
    *   - "client.http.uri" -> request URI
    *   - "client.http.status_code" -> "200", "403", etc.
    */
  def clientSingleRequest(
      request: HttpRequest
  )(implicit T: Trace[IO], as: ClassicActorSystemProvider, ioRuntime: IORuntime): Future[HttpResponse] = T
    .span("akka-http-client-request") {
      for {
        kernel    <- T.kernel
        _         <- T.put("client.http.uri" -> request.uri.path.toString(), "client.http.method" -> request.method.name)
        sendingReq = AkkaRequest.withKernelHeaders(request, kernel)
        res       <- IO.fromFuture(IO.delay(Http().singleRequest(sendingReq)))
        _         <- T.put("client.http.status_code" -> res.status.intValue().toString)
      } yield res
    }
    .unsafeToFuture()

  /** Does not allow to use the current span beyond the http layer.
    *
    * Adds the following standard fields to the current span:
    *   - "http.method" -> "GET", "PUT", etc.
    *   - "http.url" -> request URI (not URL)
    *   - "http.status_code" -> "200", "403", etc.
    *   - "error" -> true // only present in case of error
    *
    * In addition the following non-standard fields are added in case of error:
    *   - "error.message" -> Exception message
    *   - "cancelled" -> true // only present in case of cancellation
    */
  def legacyServer(entryPoint: EntryPoint[IO])(routes: Route)(implicit ioRuntime: IORuntime): Route = {
    requestContext =>
      val request = requestContext.request
      val kernel  = toKernel(request)
      entryPoint
        .continueOrElseRoot(name = request.uri.path.toString(), kernel = kernel)
        .use(span => Trace.ioTrace(span).flatMap(implicit trace => add[IO](routes, requestContext)))
        .unsafeToFuture()
  }

  /** Adds the following standard fields to the current span:
    *   - "http.method" -> "GET", "PUT", etc.
    *   - "http.url" -> request URI (not URL)
    *   - "http.status_code" -> "200", "403", etc.
    *   - "error" -> true // only present in case of error
    *
    * In addition the following non-standard fields are added in case of error:
    *   - "error.message" -> Exception message
    *   - "cancelled" -> true // only present in case of cancellation
    */
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

  private def addErrorFields[F[_]: Trace](e: Throwable): F[Unit] = Trace[F].attachError(e)

  private def addRequestFields[F[_]: Trace](req: HttpRequest): F[Unit] =
    Trace[F].put(Tags.http.method(req.method.name), Tags.http.url(req.uri.toString()))

  private def addResponseFields[F[_]: Trace](res: HttpResponse): F[Unit] =
    Trace[F].put(Tags.http.status_code(res.status.intValue().toString))
}
