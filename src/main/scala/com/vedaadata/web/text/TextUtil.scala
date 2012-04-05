package com.vedaadata.web.text

object TextUtil
{
  def stripToDashes(text: String) =
    text map { char =>
      if ((char >= '0' && char <= '9') ||
          (char >= 'a' && char <= 'z') ||
          (char >= 'A' && char <= 'Z')) char
      else '-'
  }

  def urlDot(text: String, ext: String) = stripToDashes(text) + "." + ext

  def urlDotHtml(text: String) = urlDot(text, "html")

}
