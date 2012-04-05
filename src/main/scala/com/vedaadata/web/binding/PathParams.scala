package com.vedaadata.web.binding

case class PathParams(path: String)
{
  val all = path match {
    case null => Nil
    case pathInfo => pathInfo.split("/").toList.drop(1)
  }

  def getString(paramNo: Int) =
    try { Some(all(paramNo)) }
    catch { case ex: IndexOutOfBoundsException => None }

  def getInt(paramNo: Int) =
    try { getString(paramNo) map { _.toInt } }
    catch { case ex: NumberFormatException => None }
}
