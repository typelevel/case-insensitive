/*
 * Copyright 2020 Typelevel
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.typelevel.ci

import cats.implicits._
import java.io._
import org.typelevel.ci.testing.arbitraries._
import org.specs2.mutable.Specification
import org.scalacheck.Prop._
import org.typelevel.discipline.specs2.mutable.Discipline

class CIStringJVMSpec extends Specification with Discipline {
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
}
