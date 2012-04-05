package com.vedaadata.web.binding

import javax.servlet.http.HttpServletRequest

class ParamBinder(request: HttpServletRequest)
{
  def exists(name: String) =
    request.getParameter(name) != null

  def getInt(name: String): Int =
    try {
      request.getParameter(name) toInt
    }
    catch {
      case ex: NumberFormatException => 0
    }

  def getIntOption(name: String): Option[Int] =
    request.getParameter(name) match {
      case null => None
      case i =>
        try { Some(i.toInt) }
        catch { case ex: NumberFormatException => None }
    }

  def getBoolean(name: String): Boolean =
    request.getParameter(name) match {
      case null => false
      case "true" | "1" | "yes" => true
      case _ => false
    }

  def getString(name: String): String =
    request.getParameter(name) match {
      case null => ""
      case param => param
    }

  def getStrings(name: String): List[String] =
    request.getParameterValues(name) match {
      case null => Nil
      case values => values toList
    }

  def getDate(name: String): Option[java.util.Date] =
    request.getParameter(name) match {
      case null => None
      case dateString => parseDate(dateString)
    }
}

