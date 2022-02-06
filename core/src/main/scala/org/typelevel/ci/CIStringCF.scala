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

final class CIStringCF private (
    override val toString: String,
    val asCanonicalFullCaseFoldedString: CanonicalFullCaseFoldedString)
    extends Serializable {
  override def equals(that: Any): Boolean =
    that match {
      case that: CIStringCF =>
        asCanonicalFullCaseFoldedString == that.asCanonicalFullCaseFoldedString
      case _ =>
        false
    }

  override def hashCode(): Int =
    asCanonicalFullCaseFoldedString.hashCode
}

object CIStringCF {
  def apply(value: String): CIStringCF =
    new CIStringCF(
      value,
      CanonicalFullCaseFoldedString(value)
    )
}
