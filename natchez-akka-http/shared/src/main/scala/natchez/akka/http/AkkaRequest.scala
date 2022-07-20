/*
 * Copyright 2022 Massimo Siani
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
