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
