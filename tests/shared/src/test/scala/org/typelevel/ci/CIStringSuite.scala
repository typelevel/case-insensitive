/*
 * Copyright 2020 Typelevel
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.typelevel.ci

import cats.implicits._
import cats.kernel.laws.discipline._
import munit.DisciplineSuite
import org.scalacheck.Prop._
import org.typelevel.ci.testing.arbitraries._
import scala.math.signum

class CIStringSuite extends DisciplineSuite {
  property("case insensitive equality") {
    forAll { (x: CIString) =>
      val y = CIString(new String(x.toString.toArray.map(_.toUpper)))
      val z = CIString(new String(x.toString.toArray.map(_.toLower)))
      assertEquals(y, z)
    }
  }

  property("character based equality") {
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
      assert((x.compare(y) != 0) || (signum(x.compare(z)) == signum((y.compare(z)))))
    }
  }

  property("equality consistent with comparison") {
    forAll { (x: CIString, y: CIString) =>
      assertEquals((x == y), (x.compare(y) == 0))
    }
  }

  property("hashCode consistent with equality") {
    forAll { (x: CIString, y: CIString) =>
      assert((x != y) || (x.hashCode == y.hashCode))
    }
  }
  property("toString is inverse of CI.apply") {
    forAll { (x: String) =>
      x == CIString(x).toString
    }
  }

  test("isEmpty is true given an empty string") {
    CIString("").isEmpty
  }

  test("isEmpty is false given a non-empty string") {
    !CIString("non-empty string").isEmpty
  }

  property("is never equal to .nonEmpty for any given string") {
    forAll { (ci: CIString) =>
      ci.isEmpty != ci.nonEmpty
    }
  }

  test("nonEmpty is true given a non-empty string") {
    CIString("non-empty string").nonEmpty
  }

  test("is false given an empty string") {
    !CIString("").nonEmpty
  }

  test("trim removes leading whitespace") {
    CIString("  text").trim == CIString("text")
  }

  test("removes trailing whitespace") {
    CIString("text   ").trim == CIString("text")
  }

  test("removes leading and trailing whitespace") {
    CIString("  text   ").trim == CIString("text")
  }

  property("ci interpolator is consistent with apply") {
    forAll { (s: String) =>
      assertEquals(ci"${s}", CIString(s))
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

  checkAll("Order[CIString]", OrderTests[CIString].order)
  checkAll("Hash[CIString]", HashTests[CIString].hash)
  checkAll("LowerBounded[CIString]", LowerBoundedTests[CIString].lowerBounded)
  checkAll("Monoid[CIString]", MonoidTests[CIString].monoid)

  checkAll(
    "CIString instances",
    SerializableTests.serializable(CIString.catsInstancesForOrgTypelevelCIString))
}
