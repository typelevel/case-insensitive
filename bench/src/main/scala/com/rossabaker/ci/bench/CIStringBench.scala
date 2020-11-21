/*
 * Copyright 2020 Typelevel
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.typelevel.ci
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
