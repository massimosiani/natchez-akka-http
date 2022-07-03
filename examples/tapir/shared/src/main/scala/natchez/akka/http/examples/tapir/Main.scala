package natchez.akka.http.examples.tapir

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import cats.data.Kleisli
import cats.effect.std.Dispatcher
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, IOApp, Resource}
import cats.~>
import natchez.akka.http.NatchezAkkaHttp
import natchez.akka.http.entrypoint.toEntryPointOps
import natchez.log.Log
import natchez.{EntryPoint, Span, Trace}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.tapir.integ.cats.syntax.*
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import scala.concurrent.{ExecutionContext, Future}

object Main extends IOApp.Simple {
  implicit val system: ActorSystem  = ActorSystem("my-actor-system")
  implicit val ec: ExecutionContext = system.dispatcher

  implicit val log: Logger[IO] = Slf4jLogger.getLoggerFromName("example-logger")

  // choose your backend
  val entryPointR: Resource[IO, EntryPoint[IO]] = Resource.pure(Log.entryPoint[IO]("example-service"))

  // one shot definitions for IO/Future interop in tapir
  implicit val fromFuture: (Future ~> IO) = new (Future ~> IO) {
    override def apply[A](fa: Future[A]): IO[A] = IO.fromFuture(IO(fa))
  }
  implicit val toFuture: (IO ~> Future)   = new (IO ~> Future) {
    override def apply[A](fa: IO[A]): Future[A] = fa.unsafeToFuture()
  }

  override def run: IO[Unit] = (for {
    ep <- entryPointR
    d  <- Dispatcher[IO]
  } yield (ep, d)).use {
    case (entryPoint, dispatcher) =>
      implicit val d = dispatcher

      // lift the Route in a Kleisli to pass the span around implicitly
      // use the Trace constraint on the services
      val liftedRoutes: Kleisli[IO, Span[IO], Route] = Kleisli { span =>
        Trace.ioTrace(span).flatMap { implicit t =>
          IO.executionContext.flatMap { implicit ec =>
            val helloService      = new HelloService[IO]
            val helloRoute: Route =
              AkkaHttpServerInterpreter().toRoute(helloService.helloEndpoint.imapK(toFuture)(fromFuture))
            NatchezAkkaHttp.server(IO.delay(helloRoute))
          }
        }
      }

      val tracedRoutes: Route                 = entryPoint.liftT(liftedRoutes)
      // wrap in IO
      val akkaServer: IO[Http.HttpTerminated] =
        IO.fromFuture(IO(Http().newServerAt("localhost", 8080).bind(tracedRoutes).flatMap(_.whenTerminated)))
      akkaServer.void
  }
}
