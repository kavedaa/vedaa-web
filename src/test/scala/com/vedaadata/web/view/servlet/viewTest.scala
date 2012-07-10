package com.vedaadata.web.view.servlet

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class ViewUtilTest extends FunSuite with ShouldMatchers {

  object ViewUtil extends ViewUtil
  
  test("URL") {
    
    ViewUtil URL(List("foo")) should equal("foo")

    ViewUtil URL(List("foo", "bar")) should equal("foo/bar")
    
    ViewUtil URL(List("foo", "bar"), "one" -> 1) should equal("foo/bar?one=1")
    
    ViewUtil URL(List("foo", "bar"), "one" -> 1, "two" -> 2) should equal("foo/bar?one=1&two=2")
  }
}