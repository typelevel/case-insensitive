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
import scala.annotation.tailrec

class CIStringSuite extends DisciplineSuite {
  property("case insensitive equality") {
    forAll { (x: CIString) =>
      if (x.toString.contains('\u0131')) {
        // '\u0131' is LATIN SMALL LETTER DOTLESS I The .toUpper on this
        // character will yield a 'I', but the Unicode standard for case
        // folding states \u0131 is only case insensitively equivalent to 'I'
        // for Turkic languages and by default this mapping should not be
        // used.
        val y = CIString(x.toString.toLowerCase())
        val z = CIString(x.toString.toUpperCase())
        assertNotEquals(y, z)
      } else {
        val y = CIString(x.toString.toLowerCase())
        val z = CIString(x.toString.toUpperCase())
        val t = CIString(CIStringSuite.toTitleCase(x.toString))
        assertEquals(y, z)
        assertEquals(y, t)
        assertEquals(t, z)
      }
    }
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

  // Test name copied from java.lang.Character.getName(), I know it's long...
  test("GREEK SMALL LETTER ETA WITH DASIA AND OXIA AND YPOGEGRAMMENI should compare equal with upper and loser case invocations"){
    val codePoint: Int = 8085 // Unicode codepoint of lower case value
    val lower: String = (new String(Character.toChars(codePoint))).toLowerCase
    val upper: String = lower.toUpperCase
    val title: String = lower.map(c => Character.toTitleCase(c)).mkString
    assertEquals(CIString(lower), CIString(upper))
    assertEquals(CIString(lower), CIString(title))
    assertEquals(CIString(title), CIString(upper))
  }

  checkAll("Order[CIString]", OrderTests[CIString].order)
  checkAll("Hash[CIString]", HashTests[CIString].hash)
  checkAll("LowerBounded[CIString]", LowerBoundedTests[CIString].lowerBounded)
  checkAll("Monoid[CIString]", MonoidTests[CIString].monoid)

  checkAll(
    "CIString instances",
    SerializableTests.serializable(CIString.catsInstancesForOrgTypelevelCIString))
}

object CIStringSuite {
  def mapStringByCodepoint(f: Int => Int)(s: String): String = {
    // Scala's wrapper class doesn't support appendCodePoint, so we need to
    // explicitly use the java.lang.StringBuilder
    val builder: java.lang.StringBuilder = new java.lang.StringBuilder(s.length)

    @tailrec
    def loop(index: Int): String =
      if (index >= s.length) {
        builder.toString
      } else {
        val codePoint: Int = s.codePointAt(index)
        builder.appendCodePoint(f(codePoint))
        val inc: Int = Character.charCount(codePoint)
        loop(index + inc)
      }

    loop(0)
  }

  def toTitleCase(s: String): String =
    mapStringByCodepoint(Character.toTitleCase)(s)
}
