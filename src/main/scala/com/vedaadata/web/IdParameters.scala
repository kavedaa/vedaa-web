package com.vedaadata.web

import scala.util.Try

case class IdParameter(
  id: String,
  name: String,
  value: String)

class IdParameters(parameters: AbstractParameters) {
  
  //  Assumes parameters on the form "name_id"
  val NameId = """([A-Za-z0-9-]+)_([A-Za-z0-9-]+)""".r

  def idParameters =
    parameters flatMap { case (paramNameWithId, value) =>
      val idParameter = Try {
        val NameId(name, id) = paramNameWithId
        IdParameter(id, name, value)
      } 
      idParameter.toOption
    }

  //  returns Map(id -> Map(name -> value))
  def byId: Map[String, Map[String, String]] =
    idParameters groupBy(_.id) map { idParamMap => 
      idParamMap._1 -> idParamMap._2.map(idParam => idParam.name -> idParam.value).toMap
    }

  //  returns Map(name -> Map(id -> value))
  def byName: Map[String, Map[String, String]] =
    idParameters groupBy(_.name) map { idParamMap =>
      idParamMap._1 -> idParamMap._2.map(idParam => idParam.id -> idParam.value).toMap
    }
}
