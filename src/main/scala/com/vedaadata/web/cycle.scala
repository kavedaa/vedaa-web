package com.vedaadata.web

abstract class Request[R] {
  
  val r: R
  
  def getContextPath: String
}


abstract class Response[R] {

  val r: R
}

abstract class Cycle {
  type Q
  type P
  val request: Request[Q]
  val response: Response[P]
}

