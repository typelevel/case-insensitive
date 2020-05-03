/*
 * Copyright 2020 Ross A. Baker
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

package com.rossabaker.ci
package bench

import cats.Monoid
import org.openjdk.jmh.annotations._

class CIStringBench {
  import CIStringBench._

  @Benchmark
  def hash: Int = ci.hashCode

  @Benchmark
  def combineAll: CIString = Monoid[CIString].combineAll(ciList)
}

object CIStringBench {
  val ci = CIString("A Case-Insensitive String")

  val ciList = List(
    CIString("A"),
    CIString("B"),
    CIString("C"),
    CIString("D"),
    CIString("E"),
    CIString("F"),
    CIString("G"),
    CIString("H"),
    CIString("I")
  )
}
