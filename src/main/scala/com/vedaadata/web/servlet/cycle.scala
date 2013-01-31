package com.vedaadata.web.servlet

import com.vedaadata.web._
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ServletRequest(val r: HttpServletRequest) extends Request[HttpServletRequest] {

  def getContextPath = r.getContextPath
}

class ServletResponse(val r: HttpServletResponse) extends Response[HttpServletResponse] {
 
}

case class ServletCycle(request: ServletRequest, response: ServletResponse)
  extends Cycle {
  type Q = HttpServletRequest
  type P = HttpServletResponse
}