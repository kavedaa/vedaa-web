package com.vedaadata.web.text

object TextFormatter
{
  private var markdownProcessor: String => scala.xml.NodeSeq = {
    s => throw new Exception("TextFormatter.markdownProcessor not initialized.")
  }

  def setMarkdownProcessor(f: String => scala.xml.NodeSeq) { markdownProcessor = f }

  object Format extends Enumeration {
    val plainText = Value("Plain")
    val plainWithParas = Value("Plain with paragraphs")
    val markdown = Value("Markdown")
    val xhtml = Value("XHTML")
  }

  def formatText(text: String, format: Format.Value): scala.xml.NodeSeq = format match {
    case Format.plainText =>
      <p>{ text }</p>
    case Format.plainWithParas =>
      scala.xml.NodeSeq.fromSeq(text split "\r\n\r\n" map { p => <p>{ p }</p> } )
    case Format.markdown =>
      markdownProcessor(text)
    case Format.xhtml =>
      throw new Exception("format not yet supported")
    case _ =>
      throw new Exception("unsupported text format")
  }

}
