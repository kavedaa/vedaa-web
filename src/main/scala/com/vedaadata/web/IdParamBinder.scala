package com.vedaadata.web

import scala.util.Try

case class IdParam(
  id: String,
  name: String,
  value: String)

object IdParamBinder {
  
  //  Assumes parameters on the form "name_id"
  val NameId = """([A-Za-z0-9-]+)_([A-Za-z0-9-]+)""".r

  def idParams(parameters: AbstractParameters) =
    parameters map { case (paramNameWithId, value) =>
      Try {
        val NameId(name, id) = paramNameWithId
        IdParam(id, name, value)
      } toOption
    } flatten

  //  returns Map(id -> Map(name -> value))
  def bindPerId(parameters: AbstractParameters): Map[String, Map[String, String]] =
    idParams(parameters) groupBy(_.id) map { idParamMap => 
      idParamMap._1 -> (idParamMap._2 map { idParam => idParam.name -> idParam.value } toMap)
    }

  //  returns Map(name -> Map(id -> value))
  def bindPerName(parameters: AbstractParameters): Map[String, Map[String, String]] =
    idParams(parameters) groupBy(_.name) map { idParamMap =>
      idParamMap._1 -> (idParamMap._2 map { idParam => idParam.id -> idParam.value } toMap)
    }
}
