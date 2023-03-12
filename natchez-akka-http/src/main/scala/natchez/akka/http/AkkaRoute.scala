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

import akka.http.scaladsl.server.Route
import cats.data.Kleisli
import cats.effect.IO
import cats.effect.std.Dispatcher
import natchez.{Span, Trace}

object AkkaRoute {
  def liftedRoute(f: Trace[IO] => Route)(implicit D: Dispatcher[IO]): Kleisli[IO, Span[IO], Route] =
    liftedRouteIO(t => IO.delay(f(t)))

  def liftedRouteIO(f: Trace[IO] => IO[Route])(implicit D: Dispatcher[IO]): Kleisli[IO, Span[IO], Route] = Kleisli {
    span =>
      Trace.ioTrace(span).flatMap { implicit t =>
        NatchezAkkaHttp.server(f(t))
      }
  }
}
