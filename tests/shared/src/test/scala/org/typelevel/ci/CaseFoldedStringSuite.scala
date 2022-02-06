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

final class CaseFoldedStringSuite extends DisciplineSuite {
  property("case insensitive equality") {
    forAll { (x: CaseFoldedString) =>
      if (x.toString.contains('\u0131')) {
        // '\u0131' is LATIN SMALL LETTER DOTLESS I The .toUpper on this
        // character will yield a 'I', but the Unicode standard for case
        // folding states \u0131 is only case insensitively equivalent to 'I'
        // for Turkic languages and by default this mapping should not be
        // used.
        val y = CaseFoldedString(x.toString.toLowerCase())
        val z = CaseFoldedString(x.toString.toUpperCase())
        assertNotEquals(y, z)
      } else {
        val y = CaseFoldedString(x.toString.toLowerCase())
        val z = CaseFoldedString(x.toString.toUpperCase())
        val t = CaseFoldedString(CaseFoldedStringSuite.toTitleCase(x.toString))
        assertEquals(y, z)
        assertEquals(y, t)
        assertEquals(t, z)
      }
    }
  }

  test("case insensitive comparison") {
    assert(CaseFoldedString("case-insensitive") < CaseFoldedString("CI"))
  }

  property("reflexive comparison") {
    forAll { (x: CaseFoldedString) =>
      assertEquals(x.compare(x), 0)
    }
  }

  property("equality consistent with comparison") {
    forAll { (x: CaseFoldedString, y: CaseFoldedString) =>
      assertEquals((x == y), (x.compare(y) == 0))
    }
  }

  property("hashCode consistent with equality") {
    forAll { (x: CaseFoldedString, y: CaseFoldedString) =>
      assert((x != y) || (x.hashCode == y.hashCode))
    }
  }

  test("isEmpty is true given an empty string") {
    assert(CaseFoldedString("").isEmpty)
  }

  test("isEmpty is false given a non-empty string") {
    assert(!CaseFoldedString("non-empty string").isEmpty)
  }

  property("is never equal to .nonEmpty for any given string") {
    forAll { (ci: CaseFoldedString) =>
      assert(ci.isEmpty != ci.nonEmpty)
    }
  }

  test("nonEmpty is true given a non-empty string") {
    assert(CaseFoldedString("non-empty string").nonEmpty)
  }

  test("nonEmpty is false given an empty string") {
    assert(!CaseFoldedString("").nonEmpty)
  }

  test("trim removes leading whitespace") {
    assert(CaseFoldedString("  text").trim == CaseFoldedString("text"))
  }

  test("removes trailing whitespace") {
    assert(CaseFoldedString("text   ").trim == CaseFoldedString("text"))
  }

  test("removes leading and trailing whitespace") {
    assert(CaseFoldedString("  text   ").trim == CaseFoldedString("text"))
  }

  // property("ci interpolator is consistent with apply") {
  //   forAll { (s: String) =>
  //     assertEquals(ci"$s", CaseFoldedString(s))
  //   }
  // }

  // property("ci interpolator handles expressions") {
  //   forAll { (x: Int, y: Int) =>
  //     assertEquals(ci"${x + y}", CaseFoldedString((x + y).toString))
  //   }
  // }

  // property("ci interpolator handles multiple parts") {
  //   forAll { (a: String, b: String, c: String) =>
  //     assertEquals(ci"$a:$b:$c", CaseFoldedString(s"$a:$b:$c"))
  //   }
  // }

  // property("ci interpolator extractor is case-insensitive") {
  //   forAll { (s: String) =>
  //     assert(CaseFoldedString(new String(s.toString.toArray.map(_.toUpper))) match {
  //       case ci"${t}" => t == CaseFoldedString(s)
  //       case _ => false
  //     })

  //     assert(CaseFoldedString(new String(s.toString.toArray.map(_.toLower))) match {
  //       case ci"${t}" => t == CaseFoldedString(s)
  //       case _ => false
  //     })
  //   }
  // }

  // test("ci interpolator extracts multiple parts") {
  //   assert(CaseFoldedString("Hello, Aretha") match {
  //     case ci"${greeting}, ${name}" => greeting == ci"Hello" && name == ci"Aretha"
  //   })
  // }

  // test("ci interpolator matches literals") {
  //   assert(CaseFoldedString("literally") match {
  //     case ci"LiTeRaLlY" => true
  //     case _ => false
  //   })
  // }

  // Test name copied from java.lang.Character.getName(), I know it's long...
  test("GREEK SMALL LETTER ETA WITH DASIA AND OXIA AND YPOGEGRAMMENI should compare equal with upper and loser case invocations"){
    val codePoint: Int = 8085 // Unicode codepoint of lower case value
    val lower: String = (new String(Character.toChars(codePoint))).toLowerCase
    val upper: String = lower.toUpperCase
    val title: String = lower.map(c => Character.toTitleCase(c)).mkString
    assertEquals(CaseFoldedString(lower), CaseFoldedString(upper))
    assertEquals(CaseFoldedString(lower), CaseFoldedString(title))
    assertEquals(CaseFoldedString(title), CaseFoldedString(upper))
  }

  checkAll("Order[CaseFoldedString]", OrderTests[CaseFoldedString].order)
  checkAll("Hash[CaseFoldedString]", HashTests[CaseFoldedString].hash)
  checkAll("LowerBounded[CaseFoldedString]", LowerBoundedTests[CaseFoldedString].lowerBounded)
  checkAll("Monoid[CaseFoldedString]", MonoidTests[CaseFoldedString].monoid)
}

object CaseFoldedStringSuite {
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
