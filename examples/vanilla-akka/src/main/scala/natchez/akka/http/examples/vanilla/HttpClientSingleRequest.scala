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

package natchez.akka.http.examples.vanilla

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import natchez.akka.http.NatchezAkkaHttp
import natchez.log.Log
import natchez.{EntryPoint, Trace}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object HttpClientSingleRequest {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem                = ActorSystem("SingleRequest")
    implicit val executionContext: ExecutionContext = system.dispatcher

    // usually you already have these defined
    implicit val log: Logger[IO]  = Slf4jLogger.getLoggerFromName("example-logger")
    val ep: EntryPoint[IO]        = Log.entryPoint[IO]("example-http-client-single-request")
    implicit val trace: Trace[IO] = ep
      .root("example-http-client-single-request-root-span")
      .use { rootSpan =>
        Trace.ioTrace(rootSpan)
      }
      .unsafeRunSync()

    // send your request. Note that you get a Future back (plain akka)
    val responseFuture: Future[HttpResponse] =
      NatchezAkkaHttp.clientSingleRequest(HttpRequest(uri = "http://akka.io"))

    responseFuture.onComplete {
      case Success(res) => println(res)
      case Failure(_)   => sys.error("something wrong")
    }
  }
}
