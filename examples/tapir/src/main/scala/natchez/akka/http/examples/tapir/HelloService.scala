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

package natchez.akka.http.examples.tapir

import cats.Applicative
import natchez.Trace
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint

class HelloService[F[_]: Applicative: Trace] {

  // tapir endpoint
  val helloEndpoint: ServerEndpoint[Any, F] = endpoint.out(stringBody).serverLogicSuccess(_ => helloLogic)

  // this will show a child span for the http request
  def helloLogic: F[String] = Trace[F].span("hello")(Applicative[F].pure("Hello!"))
}
