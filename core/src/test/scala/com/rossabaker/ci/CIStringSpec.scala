package com.rossabaker.ci

import com.rossabaker.ci.arbitraries._
import org.specs2.mutable.Specification
import org.specs2.ScalaCheck
import org.scalacheck.Prop._
import scala.math.signum

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

  "compare" >> {
    "is case insensitive" >> {
      assert("case-insensitive" > "CI")
      CIString("case-insensitive") < CIString("CI")
    }

    "is reflexive" >> forAll { (x: CIString) =>
      x.compare(x) == 0
    }

    "is symmetric" >> forAll { (x: CIString, y: CIString) =>
      signum(x.compare(y)) == -signum(y.compare(x))
    }

    "is transitive" >> forAll { (x: CIString, y: CIString, z: CIString) =>
      (x.compare(y) <= 0) || (y.compare(z) <= 0) || (x.compare(z) > 0)
    }

    "is substitutable" >> forAll { (x: CIString, y: CIString, z: CIString) =>
      (x.compare(y) != 0) || (signum(x.compare(z)) == signum((y.compare(z))))
    }

    "is consistent with ==" >> forAll { (x: CIString, y: CIString) =>
      (x == y) == (x.compare(y) == 0)
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
