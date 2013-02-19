package com.vedaadata.web

import java.io.PrintWriter

abstract class Request[+R] {
  
  val raw: R
  
  def getContextPath: String
  def getParameterMap: java.util.Map[String, Array[String]]
}


abstract class Response[+R] {

  val raw: R
  
  def setContentType(contentType: String)
  def getWriter: PrintWriter
}

abstract class Cycle {
  type Req
  type Resp
  val request: Request[Req]
  val response: Response[Resp]
}

