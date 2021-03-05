/*
 * Copyright 2020 Typelevel
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.typelevel.ci

import scala.annotation.{Annotation, StaticAnnotation}
import scala.collection.immutable.ArraySeq

private[ci] object compat {
  class suppressUnusedImportWarningForCompat extends Annotation with StaticAnnotation

  def unsafeWrapArray[A](arr: Array[A]): Seq[A] =
    ArraySeq.unsafeWrapArray(arr)
}
