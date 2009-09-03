package com.digitaltumbleweed.http

import scala.io.Source

import java.io._
import java.text.SimpleDateFormat
import java.util.Date

class Response() {
  private var headers: List[String] = List()
  private var content = new String

  this addHeader "Server: Beaker"
  this addHeader "Accept-Ranges: bytes"
  this addHeader "Connection: close"
  this addHeader "Content-Type: text/html"
  this addHeader "Date: " + new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date())

  def addHeader(header: String) {
    this.headers += header //deprecated but :: doesn't appear to be working...
  }

  def getHeaders(): String = {
    ("|" + this.headers.mkString("\n|")+"\n|\n|").stripMargin
  }

  def setContentLengthHeader() {
    this.addHeader("Content-Length: " + this.content.length)
  }

  def appendContent(line: String) {
    this.content += line
  }

  def addContent(content: String) {
    this.content = content
  }

  def getContent(): String = {
    this.content
  }
  
  def getResponseCodeHeader(): String = { //500
    "HTTP/1.1 500 Internal Server Error\n"
  }

  def generateResponse(): String = {
    this.content = this.getContent()
    this.setContentLengthHeader()
    this.getResponseCodeHeader() + this.getHeaders() + this.content
  }
}

sealed abstract class ResponseCode extends Response

case class OK(path: String, file: File) extends ResponseCode {
  override def getResponseCodeHeader(): String = { //200
    "HTTP/1.1 200 OK\n"
  }

  override def getContent(): String = {
    Source.fromFile(file).getLines.foreach(this.appendContent)
    super.getContent()
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

  override def getContent(): String = {
    "The path you requested: " + path + " does not exist."
  }
}
case class InternalServerError() extends ResponseCode { //404
  override def getResponseCodeHeader(): String = {
    "HTTP/1.1 500 Internal Server Error\n"
  }

  override def getContent(): String = {
    "There was an internal server error. Please contact your administrator and inform him/her of it."
  }
}
