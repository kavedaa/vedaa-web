package com.vedaadata.web.servlet

import java.io._
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServlet

class FileStreamer(file: File, servlet: HttpServlet) extends Servicer {

  def this(filePath: String, servlet: HttpServlet) = this(new File(filePath), servlet)

  def this(fileName: String, path: String, servlet: HttpServlet) = this(new File(path, fileName), servlet)

  def contentType(fileName: String) =
    servlet.getServletContext.getMimeType(fileName.toLowerCase) match {
      case null => "application/octet-stream"
      case contentType => contentType
    }

  def service(implicit c: ServletCycle) {
    c.response setContentType (contentType(file.getName))
    println(file.getName)
    println(contentType(file.getName))
    streamFile(file, c.response)
  }

  def streamFile(file: File, response: HttpServletResponse) {
    try {
      val inputStream = new FileInputStream(file)
      if (inputStream != null) {
        val outputStream = response.getOutputStream
        response setContentType contentType(file.getName)
        try { stream(inputStream, outputStream) }
        catch {
          case ex: IOException =>
            println("Error reading file " + ex)
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        inputStream.close()
        outputStream.flush()
        outputStream.close()
      }
      else {
        println("Error opening file: " + file.getAbsolutePath)
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
    }
    catch {
      case ex: IOException =>
        println("Error opening file: " + ex)
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  def stream(inputStream: InputStream, outputStream: OutputStream) = {

    val buffer = new Array[Byte](16384)

    def doStream(total: Int = 0): Int = {
      val n = inputStream.read(buffer)
      if (n == -1)
        total
      else {
        outputStream.write(buffer, 0, n)
        doStream(total + n)
      }
    }

    doStream()
  }

}

