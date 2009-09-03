package com.digitaltumbleweed.http

import scala.io.Source

import java.io._
import java.text.SimpleDateFormat
import java.util.Date

import eu.medsea.mimeutil._
import eu.medsea.mimeutil.detector._

class Response() {
  private var headers: List[String] = List()
  private var content: Array[Byte] = Array()

  this addHeader "Server: Beaker"
  this addHeader "Accept-Ranges: bytes"
  this addHeader "Connection: close"
  this addHeader "Date: " + new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date())

  def addHeader(header: String) {
    this.headers += header //deprecated but :: doesn't appear to be working...
  }

  def getHeaders(): String = {
    ("|" + this.headers.mkString("\n|")+"\n|\n|").stripMargin
  }

  def setContentLengthHeader() {
    this setContentLengthHeader this.content.length
  }

  def setContentLengthHeader(len: Int) {
    this.addHeader("Content-Length: " + len)
  }
  
  def getResponseCodeHeader(): String = { //500
    "HTTP/1.1 500 Internal Server Error\n"
  }

  def getContentType(): String = {
    "text/html"
  }

  def getContent(): Array[Byte] = {
    "" getBytes
  }

  def getContentTypeHeader(): String = {
    "Content-Type: " + this.getContentType() + "\n"
  }

  def write(os: OutputStream) {
    this.setContentLengthHeader()
    this.writeHeaders(os)
    
    val ba = this.getContent()
    os.write(ba, 0, ba.length)
  }
  
  def writeHeaders(os: OutputStream) {
    os.write(
      (this.getResponseCodeHeader() + 
       this.getContentTypeHeader() + 
       this.getHeaders()) getBytes
    )
  }
}

sealed abstract class ResponseCode extends Response

case class OK(path: String, file: File) extends ResponseCode {
  override def getResponseCodeHeader(): String = { //200
    "HTTP/1.1 200 OK\n"
  }

  override def write(os: OutputStream) {
    val inputStream = new FileInputStream(file)
    this.setContentLengthHeader(inputStream.available())
    this.writeHeaders(os)

    val ba = new Array[Byte](8192)
    def writeFile() {
      inputStream.read(ba) match {
        case x if x < 0 =>
        case 0 => writeFile
        case x => os.write(ba, 0, x); writeFile
      } 
    }
 
    writeFile
    inputStream.close
  }

  override def getContentType(): String = {
    MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector")
    MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector")
   
    var contenttype = MimeUtil.getMostSpecificMimeType(MimeUtil.getMimeTypes(file)).toString()
    
    if (contenttype == "") {
      contenttype = "text/html" //default as we are trying to be a web server.
    }

    MimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector")
    MimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector")
    contenttype
  }
}
case class MovedPermanently(path: String) extends ResponseCode //301
case class Found(path: String) extends ResponseCode //302
case class MovedTemporarily(path: String) extends ResponseCode //303
case class Unuthorized(path: String) extends ResponseCode //401
case class Forbidden(path: String) extends ResponseCode //403
case class NotFound(path: String) extends ResponseCode { //404
  override def getResponseCodeHeader(): String = { 
    "HTTP/1.1 404 Not Found\n"
  }

  override def getContent(): Array[Byte] = {
    "The path you requested: " + path + " does not exist." getBytes
  }
}
case class InternalServerError() extends ResponseCode { //404
  override def getResponseCodeHeader(): String = {
    "HTTP/1.1 500 Internal Server Error\n"
  }

  override def getContent(): Array[Byte] = {
    "There was an internal server error. Please contact your administrator and inform him/her of it." getBytes
  }
}
