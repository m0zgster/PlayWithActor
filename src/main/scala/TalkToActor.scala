import akka.actor.{ ActorRef, ActorSystem, Props, Actor }
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout

case class User(username: String, email: String)

object Recorder {
  case class NewUser(user: User)

  def props(checker: ActorRef, storage: ActorRef) = Props(new Recorder(checker, storage))
}

object Checker {
  case class CheckUser(user: User)

  case class WhiteUser(user: User)

  case class BlackUser(user: User)
}

object Storage {
  case class AddUser(user: User)
}

class Storage extends Actor {
  import Storage._

  var users = List.empty[User]

  override def receive: Receive = {
    case AddUser(user) =>
      println(s"Storage: $user stored")
      users = user :: users
  }

}

class Checker extends Actor {
  import Checker._

  val blackList = List(User("Max", "m0zgster@gmail.com"))

  override def receive: Receive = {
    case CheckUser(user) =>
      if (blackList.contains(user)) {
        println(s"Checker: $user is in blackList.")
        sender ! BlackUser(user)
      } else {
        println(s"Checker: $user isn't in blackList.")
        sender ! WhiteUser(user)
      }
  }

}

class Recorder(checker: ActorRef, storage: ActorRef) extends Actor {
  import Recorder._
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout(5 seconds)

  override def receive: Receive = {
    case NewUser(user) =>
      println(s"Recorder receives NewUser for $user")
      checker ? Checker.CheckUser(user) map {
        case Checker.WhiteUser(user) =>
          storage ! Storage.AddUser(user)
        case Checker.BlackUser(user) =>
          println(s"Recorder: $user is a black user.")
      }
  }

}

object TalkToActor extends App {

  val system = ActorSystem("talk-to-actor")
  val checker = system.actorOf(Props[Checker], "checker")
  val storage = system.actorOf(Props[Storage], "storage")
  val recorder = system.actorOf(Recorder.props(checker, storage), "recorder")
  recorder ! Recorder.NewUser(User("John Smith", "john@smith.com"))
  //recorder ! Recorder.NewUser(User("Max", "m0zgster@gmail.com"))
  system.terminate()

}