import scala.actors.Actor
import scala.actors.Actor._
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

  def addContent(content: String) {
    this.content = content
    this.addHeader("Content-Length: " + content.length)
  }

  def getContent(): String = {
    this.content
  }
}

case class Connection(socket: Socket)

class Handler extends Actor {
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

  def read(reader: LineNumberReader, writer: Writer, res: Response): Unit = {
    val line = reader.readLine()
    if (line != null) {
      val trimmed = line.trim
      println("Got line: \"" + trimmed + "\"")
      
      trimmed match {
        case Get(s) =>
	  //move to static
	  res addContent "<html><body><h1>It works in Scala! Hooray.</h1></body></html>"
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
