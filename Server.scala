import scala.actors.Actor
import scala.actors.Actor._
import java.net._
import java.io._

class Handler extends Actor {
  def act() {
    loop {
      react {
	case msg =>
	  println("recieved message: " + msg)
      }
    }
  }
}

class Server {

  def run() {
    val socket = new ServerSocket(8080)
    val handler = new Handler()
    
//    var i = 0
    handler start

    while(true) {
      println("Waiting")
      val conn = socket.accept()
      handler ! "connected!!"
    }
  }
}


object server {
  def main(args: Array[String]) {
    new Server().run()
  }
}
