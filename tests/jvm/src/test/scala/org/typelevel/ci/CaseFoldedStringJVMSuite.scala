package org.typelevel.ci

import cats.implicits._
import java.io._
import munit.ScalaCheckSuite
import org.typelevel.ci.testing.arbitraries._
import org.scalacheck.Prop._

final class CaseFoldedStringJVMSuite extends ScalaCheckSuite {
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

    forAll { (x: CaseFoldedString) =>
      x.eqv(roundTrip(x))
    }
  }
}
