/*
 * Copyright 2020 Typelevel Contributors
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.typelevel.ci

import cats.implicits._
import cats.kernel.laws.discipline._
import java.io._
import org.typelevel.ci.testing.arbitraries._
import org.specs2.mutable.Specification
import org.scalacheck.Prop._
import org.typelevel.discipline.specs2.mutable.Discipline
import scala.math.signum

class CIStringSpec extends Specification with Discipline {
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

  "serialization" >> {
    def roundTrip[A](x: A): A = {
      val baos = new ByteArrayOutputStream
      val oos = new ObjectOutputStream(baos)
      oos.writeObject(x)
      oos.close()
      val bais = new ByteArrayInputStream(baos.toByteArray)
      val ois = new ObjectInputStream(bais)
      ois.readObject().asInstanceOf[A]
    }

    "round trips" >> forAll { (x: CIString) =>
      x.eqv(roundTrip(x))
    }
  }

  checkAll("Order[CIString]", OrderTests[CIString].order)
  checkAll("Hash[CIString]", HashTests[CIString].hash)
  checkAll("LowerBounded[CIString]", LowerBoundedTests[CIString].lowerBounded)
  checkAll("Monoid[CIString]", MonoidTests[CIString].monoid)

  checkAll(
    "CIString instances",
    SerializableTests.serializable(CIString.catsInstancesForComRossbakerCIString))
}
