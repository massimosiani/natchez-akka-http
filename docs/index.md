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

A full, working example can be found in the [tapir example](https://github.com/massimosiani/natchez-akka-http/tree/main/examples/tapir) module. Run `sbt tapirExampleJVM/run`, go to `localhost:8080`, then your console will look like the following

```json
{
  "name" : "/",
  "service" : "example-service",
  "timestamp" : "2022-07-03T18:09:23.905889985Z",
  "duration_ms" : 209,
  "trace.span_id" : "541af16a-d167-483e-a817-cf774337fe27",
  "trace.parent_id" : null,
  "trace.trace_id" : "26406382-44de-4c80-b1c9-ac5672164974",
  "exit.case" : "succeeded",
  "http.method" : "GET",
  "http.url" : "http://localhost:8080/",
  "http.status_code" : "200",
  "children" : [
    {
      "name" : "hello",
      "service" : "example-service",
      "timestamp" : "2022-07-03T18:09:24.016872621Z",
      "duration_ms" : 2,
      "trace.span_id" : "ec57eca7-52db-4edb-94c0-9f1b10d62ac9",
      "trace.parent_id" : "26406382-44de-4c80-b1c9-ac5672164974",
      "trace.trace_id" : "26406382-44de-4c80-b1c9-ac5672164974",
      "exit.case" : "succeeded",
      "children" : [
      ]
    }
  ]
}
```
