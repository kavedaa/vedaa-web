package com.vedaadata.web.servlet

import com.vedaadata.web._
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HttpServletRequestProxy(val raw: HttpServletRequest) extends Request[HttpServletRequest] {
  def getContextPath = raw.getContextPath
  def getParameterMap = raw.getParameterMap.asInstanceOf[java.util.Map[String, Array[String]]]
}

class HttpServletResponseProxy(val raw: HttpServletResponse) extends Response[HttpServletResponse] {
  def setContentType(contentType: String) = raw setContentType contentType
  def getWriter = raw.getWriter
}

class ServletCycle(val request: HttpServletRequestProxy, val response: HttpServletResponseProxy)
  extends Cycle {
  type Req = HttpServletRequest
  type Resp = HttpServletResponse
}