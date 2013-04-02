package com.vedaadata.web

object Utils {
  
  def stripToDashes(text: String) =
    text map { char =>
      if ((char >= '0' && char <= '9') ||
          (char >= 'a' && char <= 'z') ||
          (char >= 'A' && char <= 'Z')) char
      else '-'
  }

  def urlDot(text: String, ext: String) = stripToDashes(text) + "." + ext

  def urlDotHtml(text: String) = urlDot(text, "html")

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
