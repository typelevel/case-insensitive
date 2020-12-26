/*
 * Copyright 2020 Typelevel
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.typelevel.ci

import cats.implicits._
import java.io._
import munit.ScalaCheckSuite
import org.typelevel.ci.testing.arbitraries._
import org.scalacheck.Prop._

class CIStringJVMSuite extends ScalaCheckSuite {
  property("serialization round trips") {
    def roundTrip[A](x: A): A = {
      val baos = new ByteArrayOutputStream
      val oos = new ObjectOutputStream(baos)
      oos.writeObject(x)
      oos.close()
      val bais = new ByteArrayInputStream(baos.toByteArray)
      val ois = new ObjectInputStream(bais)
      ois.readObject().asInstanceOf[A]
    }

    forAll { (x: CIString) =>
      x.eqv(roundTrip(x))
    }
  }
}
