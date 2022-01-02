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
