package com.vedaadata.web.io

import java.io._
import javax.servlet.http.HttpServletResponse

trait FileStreamer
{
  def contentType(fileName: String): String

  def streamOriginal(fileName: String, path: String, response: HttpServletResponse) {
    streamFile(new File(path, fileName), response) {
      (inputStream, outputStream) => println("Read " + stream(inputStream, outputStream) + " bytes.")
    }
  }

  def streamOriginal(file: File, response: HttpServletResponse) {
    streamFile(file, response) {
      (inputStream, outputStream) => println("Read " + stream(inputStream, outputStream) + " bytes.")
    }
  }

  def streamFile(file: File, response: HttpServletResponse)(method: (InputStream, OutputStream) => Unit)
  {
    try {
      val inputStream = new FileInputStream(file)
      if (inputStream != null) {
        val outputStream = response.getOutputStream
        response setContentType contentType(file.getName)
        try { method(inputStream, outputStream) }
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

  def stream(inputStream: InputStream, outputStream: OutputStream) =
  {
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
