import akka.actor.{Actor, ActorSystem, Props}

object Apollo {

  case object Play
  case object Stop

  def props = Props[Apollo]

}

class Apollo extends Actor {
  import Apollo._

  override def receive: Receive = {
    case Play =>
      println("Music started...")
    case Stop =>
      println("Music stopped...")
  }

}

object Zeus {
  case object StartMusic
  case object StopMusic
}

class Zeus extends Actor {
  import Zeus._

  override def receive: Receive = {
    case StartMusic =>
      val apollo = context.actorOf(Apollo.props)
      apollo ! Apollo.Play
    case StopMusic =>
      println("I don't want to stop music.")
  }

}

object Creation extends App {
  val system = ActorSystem("creation")
  val zeus = system.actorOf(Props[Zeus], "zeus")
  zeus ! Zeus.StartMusic
  zeus ! Zeus.StopMusic
  system.terminate()
}
