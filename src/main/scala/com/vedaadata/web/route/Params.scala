package com.vedaadata.web.route

import java.text.DateFormat
import java.text.ParseException

class Params(val self: Map[String, Array[String]]) {
  
  //	Basic
  
  def exists(name: String) = self isDefinedAt name
  def apply(name: String) = value(name)
  def value(name: String) = self get name map { _.apply(0) }
  def values(name: String) = self get name match {
    case Some(arr) if (arr.length > 0) => arr.toList
    case _ => Nil  
  }

  //	Typed simple values
  
  def int(name: String): Option[Int] =
    try { apply(name) map { _.toInt } } catch { case ex: NumberFormatException => None }

  def string(name: String) = value(name)
    
  def date(name: String)(implicit df: DateFormat): Option[java.util.Date] =
    try { apply(name) map df.parse } catch { case ex: ParseException => None }

  def boolean(name: String): Option[Boolean] =
    apply(name) map { _ match {
      case "true" | "1" | "yes" | "on" => true
      case _ => false
    } }

  //	Typed array values
  
  def strings(name: String) = values(name)
  
  //  gir alle x som Int for eksisterende parametre på formen "name_x"
  def intIdsForName(name: String): List[Int] = {
    val regex = """([A-Za-z0-9-]+)_([0-9]+)""".r
    self.toList map { param =>
      try {
        val regex(paramName, id) = param._1
        if (paramName == name) Some(id.toInt) else None
      }
      catch { case _ => None}
    }
  } flatten

  //  gir parameterverdi for parameter "name_id"
  def apply(name: String, id: String): Option[String] = apply(name + "_" + id)
  
  override def toString = self.toString
}

abstract class Param(name: String) {
  def default: String
  def apply(f: this.type => String) = name -> f(this)
  def unapply(params: Params) = params(name) orElse Some(default)
}