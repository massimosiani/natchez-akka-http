/*
 * Copyright 2022 Massimo Siani
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

package natchez.akka.http.examples.vanilla

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import cats.effect.IO
import cats.effect.unsafe.IORuntime
import natchez.EntryPoint
import natchez.akka.http.NatchezAkkaHttp
import natchez.log.Log
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object Main extends App {
  // adapted from the Akka Http documentation
  implicit private val system: ActorSystem                        = ActorSystem("my-system")
  implicit private val executionContext: ExecutionContextExecutor = system.dispatcher

  private val route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }

  implicit private val log: Logger[IO] = Slf4jLogger.getLoggerFromName("example-logger")
  private val ep: EntryPoint[IO]       = Log.entryPoint[IO]("example-service")
  private val tracedRoute: Route       = NatchezAkkaHttp.legacyServer(ep)(route)(IORuntime.global)

  private val bindingFuture = Http().newServerAt("localhost", 8080).bind(tracedRoute)

  println("Server now online. Please navigate to http://localhost:8080/hello\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
