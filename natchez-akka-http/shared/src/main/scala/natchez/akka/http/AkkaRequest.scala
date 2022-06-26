package natchez.akka.http

import akka.http.scaladsl.model.HttpRequest
import natchez.Kernel

object AkkaRequest {
  private[http] def toKernel(request: HttpRequest): Kernel = {
    val headers           = request.headers
    val traceId           = "X-Natchez-Trace-Id"
    val parentSpanId      = "X-Natchez-Parent-Span-Id"
    val maybeTraceId      =
      headers.find(_.lowercaseName() == traceId.toLowerCase).map(header => (traceId, header.value()))
    val maybeParentSpanId =
      headers.find(_.lowercaseName() == parentSpanId.toLowerCase).map(header => (parentSpanId, header.value()))
    Kernel((maybeTraceId.toList ++ maybeParentSpanId.toList).toMap)
  }
}
