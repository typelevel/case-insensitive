package com.rossabaker.ci

import com.rossabaker.ci.arbitraries._
import org.specs2.mutable.Specification
import org.specs2.ScalaCheck
import org.scalacheck.Prop._

class CIStringSpec extends Specification with ScalaCheck {
  "==" >> {
    "is case insensitive" >> forAll { (x: CIString) =>
      val y = CIString(new String(x.toString.toArray.map(_.toUpper)))
      val z = CIString(new String(x.toString.toArray.map(_.toLower)))
      y == z
    }

    "is character based" >> {
      assert("ß".toUpperCase == "SS")
      CIString("ß") != CIString("SS")
    }

    "is reflexive" >> forAll { (x: CIString) =>
      x == x
    }

    "is symmetric" >> forAll { (x: CIString, y: CIString) =>
      (x == y) == (y == x)
    }

    "is transitive" >> forAll { (x: CIString, y: CIString, z: CIString) =>
      (x != y) || (y != z) || (x == z)
    }
  }

  "hashCode" >> {
    "is consistent with ==" >> forAll { (x: CIString, y: CIString) =>
      (x != y) || (x.hashCode == y.hashCode)
    }
  }

  "toString" >> {
    "is inverse of CI.apply" >> forAll { (x: String) =>
      x == CIString(x).toString
    }
  }
}
