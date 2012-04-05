package com.vedaadata.web.binding

import javax.servlet.http.HttpServletRequest

class PathInfoBinder(request: HttpServletRequest)
{
  val params = request.getPathInfo match {
    case null => Nil
    case pathInfo => pathInfo.split("/").toList.drop(1)
  }

  def getString(paramNo: Int) =
    try { Some(params(paramNo)) }
    catch { case ex: IndexOutOfBoundsException => None }

  def getInt(paramNo: Int) =
    try { getString(paramNo) map { _.toInt } }
    catch { case ex: NumberFormatException => None }

}
