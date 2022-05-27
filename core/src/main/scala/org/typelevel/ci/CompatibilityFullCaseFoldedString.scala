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

import java.text.Normalizer
import scala.annotation.tailrec

final case class CompatibilityFullCaseFoldedString private (override val toString: String)
    extends AnyVal

object CompatibilityFullCaseFoldedString {

  def apply(value: String): CompatibilityFullCaseFoldedString = {
    val nfdNormal: String =
      if (Normalizer.isNormalized(value, Normalizer.Form.NFD)) {
        value
      } else {
        Normalizer.normalize(value, Normalizer.Form.NFD)
      }

    val caseFold0: String =
      CaseFolding.fullCaseFoldString(nfdNormal)

    val nfkdNormal0: String =
      Normalizer.normalize(caseFold0, Normalizer.Form.NFKD)

    val caseFold1: String =
      CaseFolding.fullCaseFoldString(nfkdNormal0)

    val nfkdNormal1: String =
      Normalizer.normalize(caseFold1, Normalizer.Form.NFKD)

    // scalafmt:off
    //
    // Yes, you read that right. Round and round we go.
    //
    // > "Yes, that’s it," said the Hatter with a sigh:
    // > "it’s always tea-time, and we’ve no time to wash the things between whiles."
    // >
    // > "Then you keep moving round, I suppose?" said Alice.
    // >
    // > "Exactly so," said the Hatter: "as the things get used up."
    // >
    // > "But what happens when you come to the beginning again?" Alice ventured to ask.
    // >
    // > "Suppose we change the subject," the March Hare interrupted, yawning
    //
    // - Alice's Adventures In Wonderland, Chapter VII, by Lewis Carroll
    //
    // scalafmt:on
    new CompatibilityFullCaseFoldedString(nfkdNormal1)
  }

  val empty: CompatibilityFullCaseFoldedString =
    apply("")
}
