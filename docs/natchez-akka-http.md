# natchez-akka-http

[![Maven Central](https://img.shields.io/maven-central/v/io.github.massimosiani/natchez-akka-http_2.13.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.massimosiani%22%20AND%20a:%22natchez-akka-http_2.13%22)

An integration library for Natchez and Akka Http.

Inspired by [natchez-http4s](https://github.com/tpolecat/natchez-http4s).

## Installation

Add the following to your `build.sbt`
```scala
libraryDependencies ++= Seq("io.github.massimosiani" %% "natchez-akka-http" % <version>)
```

## Server side

### Plain Akka Http
If you only have the routes, then follow the example in the
[VanillaAkkaHttpRoute class](https://github.com/massimosiani/natchez-akka-http/tree/main/examples/vanilla-akka).

If the request contains a kernel, the entry point will create a continuation,
otherwise a root span will be created.

Run `sbt "exampleVanillaAkkaJVM/runMain natchez.akka.http.examples.vanilla.VanillaAkkaHttpRoute"`,
open a browser and go to `localhost:8080/hello`.
You should see something similar in your console.
Notice that the `children` section will always be empty.

```json
{
  "name" : "/hello",
  "service" : "example-service",
  "timestamp" : "2022-07-03T18:38:43.947920703Z",
  "duration_ms" : 7,
  "trace.span_id" : "2a94d0b0-1505-4677-aa45-02220f4b2499",
  "trace.parent_id" : null,
  "trace.trace_id" : "7942bd6f-cbdd-45f7-9a38-e57a0274d070",
  "exit.case" : "succeeded",
  "http.method" : "GET",
  "http.url" : "http://localhost:8080/hello",
  "http.status_code" : "200",
  "children" : [
  ]
}
```

### Services built with cats effect IO or tagless final
If you can build your services using cats effect `IO` or tagless final style,
e.g. using [tapir](https://tapir.softwaremill.com/en/latest/),
build the corresponding services with a `Trace` constraint in scope.

In this case, the `children` section will be filled.

#### Example: tapir
The main idea is writing all services forgetting about `Future`, and mapping the `IO` to `Future` at the
very end.

A full, working example can be found in the [tapir example](https://github.com/massimosiani/natchez-akka-http/tree/main/examples/tapir) module. Run `sbt exampleTapirJVM/run`, go to `localhost:8080`, then your console will look like the following

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

## Client side

For an example, see [HttpClientSingleRequest class](https://github.com/massimosiani/natchez-akka-http/tree/main/examples/vanilla-akka).
If you run `sbt "exampleVanillaAkkaJVM/runMain natchez.akka.http.examples.vanilla.HttpClientSingleRequest"`,
you should see something similar in your console:

```json
{
  "name" : "example-http-client-single-request-root-span",
  "service" : "example-http-client-single-request",
  "timestamp" : "2022-07-24T17:07:51.490422799Z",
  "duration_ms" : 6,
  "trace.span_id" : "ce1973e4-eed5-4e38-9201-caae09ea7b1d",
  "trace.parent_id" : null,
  "trace.trace_id" : "b10a567d-0d80-4ddd-b70d-4e7f00146256",
  "exit.case" : "succeeded",
  "children" : [
  ]
}
```
