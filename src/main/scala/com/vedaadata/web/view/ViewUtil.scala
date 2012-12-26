package com.vedaadata.web.view

trait ViewUtil {

  def URL(path: List[Any], params: (String, Any)*) = {
    val pathElement = Some(path mkString "/")
    val paramsString = params.toList map {
      case (param, value) =>
        List(param, "=", value).mkString
    } mkString "&"
    val paramsElement = paramsString match {
      case x if x.nonEmpty => Some(x)
      case _ => None
    }
    List(pathElement, paramsElement).flatten mkString "?"
  }
}
