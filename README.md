# Natchez Akka Http

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.massimosiani/natchez-akka-http_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.massimosiani/natchez-akka-http_2.13)

A tiny integration library for Natchez and Akka Http.

Only a server middleware has been implemented so far.

## Code teaser

If you only have the routes, then the following code will add a single span for the Http layer only
```scala
import natchez.akka.http.NatchezAkkaHttp

val entryPoint: EntryPoint[IO] = ???
val myRoutes: Route = ???

val tracedRoutes: Route = NatchezAkkaHttp.legacyServer(entryPoint)(legacyRoutes)
```

If you can build your services using `IO` or tagless final style
```scala
import akka.http.scaladsl.server.Route
import cats.data.Kleisli
import cats.effect.IO
import natchez.akka.http.HasFuture.ioHasFuture
import natchez.akka.http.NatchezAkkaHttp
import natchez.akka.http.entrypoint.toEntryPointOps
import natchez.{EntryPoint, Span, Trace}

val entryPoint: EntryPoint[IO] = ???
val myRoutes: Route = ???

// lift the Route in a Kleisli to pass the span around implicitly
// use the Trace constraint on the services
val liftedRoutes: Kleisli[IO, Span[IO], Route] = Kleisli { span =>
  Trace.ioTrace(span).flatMap(implicit t => NatchezAkkaHttp.server(IO.delay(myRoutes)))
}

val tracedRoutes: Route = entryPoint.liftT(liftedRoutes)
```
