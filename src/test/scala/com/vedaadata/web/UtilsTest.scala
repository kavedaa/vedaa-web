package com.vedaadata.web

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class UtilsTest extends FunSuite with ShouldMatchers {

  import WebUtils._

  test("URL") {

    URL(List("foo")) should equal("foo")

    URL(List("foo", "bar")) should equal("foo/bar")

    URL(List("foo", "bar"), "one" -> 1) should equal("foo/bar?one=1")

    URL(List("foo", "bar"), "one" -> 1, "two" -> 2) should equal("foo/bar?one=1&two=2")
  }
}