# natchez-akka-http

An integration library for Natchez and Akka Http.

Only a server middleware has been implemented so far.

## I have some legacy Route
If you only have the routes, then the following code will add a single span for the Http layer only.
You will get the continuation from the other services for free.
```scala
import akka.http.scaladsl.server.Route
import cats.effect.IO
import natchez.akka.http.NatchezAkkaHttp
import natchez.EntryPoint

val entryPoint: EntryPoint[IO] = ???
val myRoutes: Route = ???

val tracedRoutes: Route = NatchezAkkaHttp.legacyServer(entryPoint)(legacyRoutes)
```

## Services built with cats effect IO or tagless final
If you can build your services using cats effect `IO` or tagless final style,
e.g. using [tapir](https://tapir.softwaremill.com/en/latest/),
build the corresponding services with a `Trace` constraint in scope.

### Example: tapir
The main idea is writing all services forgetting about `Future`, and mapping the `IO` to `Future` at the
very end.

```scala
import akka.http.scaladsl.server.Route
import cats.~>
import cats.data.Kleisli
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import natchez.akka.http.NatchezAkkaHttp
import natchez.akka.http.entrypoint.toEntryPointOps
import natchez.{EntryPoint, Span, Trace}
import sttp.tapir.*
import sttp.tapir.integ.cats.syntax.*
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

val entryPoint: EntryPoint[IO] = ???

// tapir endpoint
val helloEndpoint: PublicEndpoint[Unit, Unit, String, Any] = endpoint.out(stringBody)
// this will show a child span for the http request
def helloLogic(implicit trace: Trace[IO]): IO[String] = trace.span("hello")(IO("Hello!"))

// for tapir-cats integration
implicit val fromFuture: (Future ~> IO) = new (Future ~> IO) {
  override def apply[A](fa: Future[A]): IO[A] = IO.fromFuture(IO(fa))
}
implicit val toFuture: (IO ~> Future) = new (IO ~> Future) {
  override def apply[A](fa: IO[A]): Future[A] = fa.unsafeToFuture()
}

// lift the Route in a Kleisli to pass the span around implicitly
// use the Trace constraint on the services
val liftedRoutes: Kleisli[IO, Span[IO], Route] = Kleisli { span =>
  Trace.ioTrace(span).flatMap { implicit t =>
    val helloRoute: Route = AkkaHttpServerInterpreter().toRoute(
      helloEndpoint
        .serverLogicSuccess(helloLogic)
        .imapK(toFuture)(fromFuture)  // converting to Future after the Trace constraint has been satisfied
    )
    NatchezAkkaHttp.server(IO.delay(helloRoute))
  }
}

val tracedRoutes: Route = entryPoint.liftT(liftedRoutes)
// start the server
```
