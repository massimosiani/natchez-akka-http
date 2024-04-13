/*
 * Copyright 2024 Massimo Siani
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

package natchez.pekko.http

import cats.syntax.eq.*
import natchez.Kernel
import org.apache.pekko.http.scaladsl.model.HttpRequest
import org.apache.pekko.http.scaladsl.model.headers.RawHeader
import org.typelevel.ci.*

object PekkoRequest {
  private[http] def withKernelHeaders(request: HttpRequest, kernel: Kernel): HttpRequest = request.withHeaders(
    kernel.toHeaders.map { case (k, v) => RawHeader(k.toString, v) }.toSeq ++ request.headers
  ) // prioritize request headers over kernel ones

  private[http] def toKernel(request: HttpRequest): Kernel = {
    val headers           = request.headers
    val traceId           = ci"X-Natchez-Trace-Id"
    val parentSpanId      = ci"X-Natchez-Parent-Span-Id"
    val maybeTraceId      =
      headers.find(h => CIString(h.lowercaseName()) === traceId).map(header => (traceId, header.value()))
    val maybeParentSpanId =
      headers.find(h => CIString(h.lowercaseName()) === parentSpanId).map(header => (parentSpanId, header.value()))
    Kernel((maybeTraceId.toList ++ maybeParentSpanId.toList).toMap)
  }
}
