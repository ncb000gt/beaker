package com.digitaltumbleweed.server

import scala.Null
import scala.actors.Actor
import scala.actors.Actor._

import com.digitaltumbleweed.http._

import java.net._
import java.io._

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
    val os = socket.getOutputStream
    val writer = new OutputStreamWriter(os)
    val is = socket.getInputStream
    val reader = new LineNumberReader(new InputStreamReader(is))
    read(reader, writer)
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

    (path + uri + postfix)
  }

  def read(reader: LineNumberReader, writer: Writer): Unit = {
    val line = reader.readLine()
    if (line != null) {
      val trimmed = line.trim
      var res = new Response()
      val path = this.getFullPath(trimmed)
      val file = new File(path)
      if (!file.exists()) {
	res = new NotFound(path)
      } else {
	res = new OK(path, file)
      }
      
      if (!res.isInstanceOf[Response]) {
	res = new InternalServerError()
      }

      trimmed match {
        case Get(s) =>
	  writer.write(res.generateResponse())
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
