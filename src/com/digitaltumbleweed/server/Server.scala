package com.digitaltumbleweed.server

import scala.actors.Actor
import scala.actors.Actor._
import scala.io.Source

import java.net._
import java.io._
import java.text.SimpleDateFormat
import java.util.Date

class Response() {
  private var headers: List[String] = List()
  private var content = new String

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
}

case class Connection(socket: Socket)

class Handler extends Actor {
  val path = "/var/www/"

  def act() {
    loop {
      react {
	case Connection(socket) =>
	  println("connection made: " + socket.getInetAddress().getHostAddress())
	  handle(socket)
	  socket.close()
	case msg =>
	  println("recieved message: " + msg)
      }
    }
  }

  def handle(socket: Socket) {
    val res = new Response()
    res addHeader "HTTP/1.1 200 OK"
    res addHeader "Server: Beaker"
    res addHeader "Accept-Ranges: bytes"
    res addHeader "Connection: close"
    res addHeader "Content-Type: text/html"
    res addHeader "Date: " + new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date())
    
    val os = socket.getOutputStream
    val writer = new OutputStreamWriter(os)
    val is = socket.getInputStream
    val reader = new LineNumberReader(new InputStreamReader(is))
    read(reader, writer, res)
  }

  private val Get = "^(GET /.*)$".r
  private val Head = "^(HEAD /.*)$".r

  def getFullPath(req_line: String): String = {
    var postfix = ""
    var uri = req_line.split(" ")(1)
    if (uri.endsWith("/")) {
      postfix = "index.html"
    }
    if (uri.startsWith("/")) {
      uri = uri substring 1
    }
    println(uri)
    println((path + uri + postfix))
    (path + uri + postfix)
  }

  def read(reader: LineNumberReader, writer: Writer, res: Response): Unit = {
    val line = reader.readLine()
    if (line != null) {
      val trimmed = line.trim
      println("Got line: \"" + trimmed + "\"")
      
      trimmed match {
        case Get(s) =>
	  Source.fromFile(this.getFullPath(trimmed)).getLines.foreach(res.appendContent)
	  res.setContentLengthHeader()
          writer.write(res.getHeaders())
          writer.write(res.getContent())
          writer.flush()
        case Head(s) =>
          writer.write(res.getHeaders())
	  writer.flush()
        case _ =>
	  println("Got unknown command: \"" + trimmed + "\"")
      }
    }
  }
}

class Server {

  def run() {
    val socket = new ServerSocket(8080)
    val handler = new Handler()
    
    handler start

    while(true) {
      val conn = socket.accept()
      handler ! Connection(conn)
    }
  }
}


object server {
  def main(args: Array[String]) {
    new Server().run()
  }
}
