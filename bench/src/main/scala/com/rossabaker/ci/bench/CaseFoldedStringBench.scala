package org.typelevel.ci
package bench

import org.scalacheck._
import org.typelevel.ci.testing.arbitraries._
import cats._
import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput, Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class CaseFoldedStringBench {

  var currentSeed: Long = Long.MinValue

  def nextSeed: Long = {
    val seed = currentSeed
    currentSeed += 1L
    seed
  }

  def nextString: String =
    Arbitrary.arbitrary[String].apply(Gen.Parameters.default, rng.Seed(nextSeed)).getOrElse(throw new AssertionError("Failed to generate String."))

  def nextListOfString: List[String] =
    Gen.listOf(Arbitrary.arbitrary[String])(Gen.Parameters.default, rng.Seed(nextSeed)).getOrElse(throw new AssertionError("Failed to generate String."))

  @Benchmark
  def caseFoldedStringHash: Int =
    CaseFoldedString(nextString).hashCode

  @Benchmark
  def caseFoldedStringFoldMap: CaseFoldedString =
    Foldable[List].foldMap(nextListOfString)(CaseFoldedString.apply)

  @Benchmark
  def stringHash: Int =
    nextString.hashCode

  @Benchmark
  def stringFoldMap: String =
    Foldable[List].foldMap(nextListOfString)(identity)
}
