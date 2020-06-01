package intro

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.persistence.PersistentActor

object PersistAsyncDemo extends App {

  case class Command(contents: String)
  case class Event(contents: String)

  object CriticalStreamProcessor {
    def props(eventAggregator: ActorRef) = Props(new CriticalStreamProcessor(eventAggregator))
  }

  class CriticalStreamProcessor(eventAggregator: ActorRef) extends PersistentActor with ActorLogging {
    override def persistenceId: String = "stream"

    override def receiveCommand: Receive = {
      case Command(contents)     =>
        eventAggregator ! s"processing $contents"
        persistAsync(Event(contents)) { e =>
          eventAggregator ! e
        }

        val processContents = contents + "_process"
        persistAsync(Event(processContents)) { e =>
          eventAggregator ! e
        }
    }

    override def receiveRecover: Receive = {
      case msg => log.info(s"recover $msg")
    }
  }

  class EventAggregator extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(s"aggregating $msg")
    }
  }

  val actorSystem = ActorSystem("demo")
  val eventAggregator = actorSystem.actorOf(Props[EventAggregator], "event1")
  val streamProcessor = actorSystem.actorOf(CriticalStreamProcessor.props(eventAggregator), "stream")

  streamProcessor ! Command("command1")
  streamProcessor ! Command("command2")

}
