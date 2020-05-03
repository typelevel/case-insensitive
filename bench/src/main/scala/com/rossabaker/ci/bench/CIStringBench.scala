package com.rossabaker.ci
package bench

import org.openjdk.jmh.annotations._

class CIStringBench {
  import CIStringBench._

  @Benchmark
  def hash: Int = ci.hashCode
}

object CIStringBench {
  val ci = CIString("A Case-Insensitive String")
}
