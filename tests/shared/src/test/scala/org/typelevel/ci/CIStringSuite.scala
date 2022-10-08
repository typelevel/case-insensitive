/*
 * Copyright 2020 Typelevel
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

package org.typelevel.ci

import cats.implicits._
import cats.kernel.laws.discipline._
import munit.DisciplineSuite
import org.scalacheck.Prop._
import org.typelevel.ci.testing.arbitraries._
import scala.math.signum

class CIStringSuite extends DisciplineSuite {
  override def scalaCheckTestParameters =
    if (System.getProperty("java.vm.name") == "Scala Native")
      super.scalaCheckTestParameters.withMinSuccessfulTests(10)
    else super.scalaCheckTestParameters

  property("case insensitive equality") {
    forAll { (x: CIString) =>
      val y = CIString(new String(x.toString.toArray.map(_.toUpper)))
      val z = CIString(new String(x.toString.toArray.map(_.toLower)))
      assertEquals(y, z)
    }
  }

  test("character based equality") {
    assert(CIString("ß") != CIString("SS"))
  }

  property("reflexive equality") {
    forAll { (x: CIString) =>
      assertEquals(x, x)
    }
  }

  property("symmetrical equality") {
    forAll { (x: CIString, y: CIString) =>
      assertEquals(x == y, y == x)
    }
  }

  property("transitive equality") {
    forAll { (x: CIString, y: CIString, z: CIString) =>
      assert((x != y) || (y != z) || (x == z))
    }
  }

  test("case insensitive comparison") {
    assert(CIString("case-insensitive") < CIString("CI"))
  }

  property("reflexive comparison") {
    forAll { (x: CIString) =>
      assertEquals(x.compare(x), 0)
    }
  }

  property("symmetric comparison") {
    forAll { (x: CIString, y: CIString) =>
      assertEquals(signum(x.compare(y)), -signum(y.compare(x)))
    }
  }

  property("transitive comparison") {
    forAll { (x: CIString, y: CIString, z: CIString) =>
      assert((x.compare(y) <= 0) || (y.compare(z) <= 0) || (x.compare(z) > 0))
    }
  }

  property("substitutable comparison") {
    forAll { (x: CIString, y: CIString, z: CIString) =>
      assert((x.compare(y) != 0) || (signum(x.compare(z)) == signum(y.compare(z))))
    }
  }

  property("equality consistent with comparison") {
    forAll { (x: CIString, y: CIString) =>
      assertEquals(x == y, x.compare(y) == 0)
    }
  }

  property("hashCode consistent with equality") {
    forAll { (x: CIString, y: CIString) =>
      assert((x != y) || (x.hashCode == y.hashCode))
    }
  }
  property("toString is inverse of CI.apply") {
    forAll { (x: String) =>
      assert(x == CIString(x).toString)
    }
  }

  test("isEmpty is true given an empty string") {
    assert(CIString("").isEmpty)
  }

  test("isEmpty is false given a non-empty string") {
    assert(!CIString("non-empty string").isEmpty)
  }

  property("is never equal to .nonEmpty for any given string") {
    forAll { (ci: CIString) =>
      assert(ci.isEmpty != ci.nonEmpty)
    }
  }

  test("nonEmpty is true given a non-empty string") {
    assert(CIString("non-empty string").nonEmpty)
  }

  test("nonEmpty is false given an empty string") {
    assert(!CIString("").nonEmpty)
  }

  test("trim removes leading whitespace") {
    assert(CIString("  text").trim == CIString("text"))
  }

  test("removes trailing whitespace") {
    assert(CIString("text   ").trim == CIString("text"))
  }

  test("removes leading and trailing whitespace") {
    assert(CIString("  text   ").trim == CIString("text"))
  }

  property("ci interpolator is consistent with apply") {
    forAll { (s: String) =>
      assertEquals(ci"$s", CIString(s))
    }
  }

  property("ci interpolator handles expressions") {
    forAll { (x: Int, y: Int) =>
      assertEquals(ci"${x + y}", CIString((x + y).toString))
    }
  }

  property("ci interpolator handles multiple parts") {
    forAll { (a: String, b: String, c: String) =>
      assertEquals(ci"$a:$b:$c", CIString(s"$a:$b:$c"))
    }
  }

  property("ci interpolator extractor is case-insensitive") {
    forAll { (s: String) =>
      assert(CIString(new String(s.toString.toArray.map(_.toUpper))) match {
        case ci"${t}" => t == CIString(s)
        case _ => false
      })

      assert(CIString(new String(s.toString.toArray.map(_.toLower))) match {
        case ci"${t}" => t == CIString(s)
        case _ => false
      })
    }
  }

  test("ci interpolator extracts multiple parts") {
    assert(CIString("Hello, Aretha") match {
      case ci"${greeting}, ${name}" => greeting == ci"Hello" && name == ci"Aretha"
    })
  }

  test("ci interpolator matches literals") {
    assert(CIString("literally") match {
      case ci"LiTeRaLlY" => true
      case _ => false
    })
  }

  test("contains lowercase to lowercase is true if matching") {
    assert(CIString("hello there").contains(CIString("hello there")))
    assert(CIString("hello there").contains(CIString("hello")))
  }

  test("contains lowercase to lowercase is false if not matching") {
    assertEquals(CIString("hello there").contains(CIString("guten arben")), false)
  }

  test("contains lowercase to mixed case is false if not matching") {
    assertEquals(CIString("hello there").contains(CIString("GUTEN ARBEN")), false)
  }

  test("contains mixed to mixed case is true if matching") {
    assert(CIString("HELLO there").contains(CIString("hellO ThErE")))
  }

  test("contains uppercase to mixed case is true if matching") {
    assert(CIString("HELLO THERE").contains(CIString("hellO ThErE")))
  }

  test("contains uppercase to mixed case is false if not matching") {
    assertEquals(CIString("HELLO THERE").contains(CIString("GUTEN arben")), false)
  }

  checkAll("Order[CIString]", OrderTests[CIString].order)
  checkAll("Hash[CIString]", HashTests[CIString].hash)
  checkAll("LowerBounded[CIString]", LowerBoundedTests[CIString].lowerBounded)
  checkAll("Monoid[CIString]", MonoidTests[CIString].monoid)

  checkAll(
    "CIString instances",
    SerializableTests.serializable(CIString.catsInstancesForOrgTypelevelCIString))
}
