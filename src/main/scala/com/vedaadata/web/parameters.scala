package com.vedaadata.web

import scala.collection.JavaConversions
import scala.util.Try
import java.text.DateFormat
import org.apache.commons.fileupload.FileItem

/**
 * Wraps all request parameters and values in a scala.immutable.Map[String, String].
 * Has utility methods to read out single parameter values as specific datatypes.
 */
abstract class AbstractParameters extends Map[String, String] {

  protected val self: Map[String, Seq[String]]

  def paramsCompanion: ParametersCompanion

  private lazy val single = self collect { case (k, v) if v.nonEmpty => (k, v.head) }

  /**
   * Gives access to multiple values of parameters.
   */
  val multi: AbstractMultiParameters

  lazy val idParameters = new IdParameters(this)
  
  //	Map implementation methods

  def get(key: String) = single get key

  def iterator = single.iterator

  //	These two are not really that useful but are required on Map

  def +[B1 >: String](kv: (String, B1)) = single + kv

  def -(key: String) = single - key

  //	Parameter value methods

  def int(name: String): Option[Int] = get(name) flatMap paramsCompanion.int

  def boolean(name: String): Boolean = contains(name)

  def date(name: String)(implicit df: DateFormat): Option[java.util.Date] = get(name) flatMap paramsCompanion.date

  /**
   * Gives parameter value for parameter named like "name_id"
   */
  def withId(name: String, id: String): Option[String] = get(s"${name}_${id}")
}

/**
 * Wraps all request parameters and values in a scala.immutable.Map[String, Seq[String]].
 * Has utility methods to read out multiple parameter values as specific datatypes.
 */
abstract class AbstractMultiParameters extends Map[String, Seq[String]] {

  protected val self: Map[String, Seq[String]]

  def paramsCompanion: ParametersCompanion

  //	Map implementation methods

  def get(key: String) = self get key

  def iterator = self.iterator

  override def default(key: String) = Nil

  //	These two are not really that useful but are required on Map

  def +[B1 >: Seq[String]](kv: (String, B1)) = self + kv

  def -(key: String) = self - key

  //	Parameter value methods

  def int(name: String): Seq[Int] = apply(name) flatMap paramsCompanion.int

//  def boolean(name: String): Seq[Boolean] = apply(name) map paramsCompanion.boolean

  def date(name: String)(implicit df: DateFormat): Seq[java.util.Date] = apply(name) flatMap paramsCompanion.date

  /**
   * Gives parameter value for parameter named like "name_id".
   */
  def withId(name: String, id: String): Seq[String] = apply(s"${name}_${id}")

  //  gir alle x som Int for eksisterende parametre på formen "name_x"
  def intIdsForName(name: String): Seq[Int] = {
    val regex = """([A-Za-z0-9-]+)_([0-9]+)""".r
    self.toSeq flatMap { param =>
      Try {
        val regex(paramName, id) = param._1
        if (paramName == name) Some(id.toInt) else None
      } toOption
    }
  } flatten

}

abstract class AbstractMultipartFormdataParameters extends AbstractParameters {

  protected val fileItems: Seq[FileItem]

  def file(name: String) = fileItems find(_.getFieldName == name)
}

class ParametersCompanion {

  private[web] def int(value: String) = Try(value.toInt).toOption

//  private[web] def boolean(value: String) = value match {
//    case "true" | "1" | "yes" | "on" => true
//    case _ => false
//  }

  private[web] def date(value: String)(implicit df: DateFormat) = Try(df parse value).toOption
}

//	Utility classes

class Parameter(val name: String) {
  def apply(f: this.type => String) = name -> f(this)
  def unapply(params: AbstractParameters) = Some(params get name)
}

abstract class DefaultParameter(val name: String) {
  def default: String
  def apply(f: this.type => String) = name -> f(this)
  def unapply(params: AbstractParameters) = Some(params get name getOrElse default) 
}