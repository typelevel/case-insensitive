/*
 * Copyright 2020 Typelevel
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.typelevel.ci

import scala.annotation.{Annotation, StaticAnnotation}

private[ci] object compat {
  class suppressUnusedImportWarningForCompat extends Annotation with StaticAnnotation

  type IterableOnce[+A] = TraversableOnce[A]

  implicit class traversableOnceSyntax[A](private val to: TraversableOnce[A]) extends AnyVal {
    def iterator: Iterator[A] = to.toIterator
  }

  def unsafeWrapArray[A](arr: Array[A]): Seq[A] =
    arr.toSeq
}
