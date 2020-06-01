package intro

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}

import scala.collection.mutable

object Snapshots extends App {

  // command
  case class ReceivedMessage(contents: String) // from
  case class SentMessage(contents: String) // to contact

  //events
  case class ReceivedMessageRecord(id: Int, contents: String)

  case class SentMessageRecord(id: Int, contents: String)

  object Chat {
    def props(owner: String, contact: String): Props = Props(new Chat(owner, contact))
  }

  class Chat(owner: String, contact: String) extends PersistentActor with ActorLogging {
    val MAX_MESSAGES = 10

    var commandWithoutCheckpoint = 0
    var currentMessageId = 0
    val lastMessages = new mutable.Queue[(String, String)]()

    override def persistenceId: String = s"$owner-$contact-chat"

    override def receiveCommand: Receive = {
      case ReceivedMessage(contents) =>
        persist(ReceivedMessageRecord(currentMessageId, contents)) { e =>
          log.info(s"received: $contents")
          maybeReplaceMessage(contact, contents)

          currentMessageId += 1
          maybeCheckpoint()
        }
      case SentMessage(contents) =>
        persist(SentMessageRecord(currentMessageId, contents)) { e =>
          log.info(s"sent: $contents")
          maybeReplaceMessage(owner, contents)

          currentMessageId += 1
          maybeCheckpoint()
        }
    }

    override def receiveRecover: Receive = {
      case ReceivedMessageRecord(id, contents) =>
        log.info(s"recovered $id: $contents")
        maybeReplaceMessage(contact, contents)
        currentMessageId += id
      case SentMessageRecord(id, contents) =>
        log.info(s"recovered sent msg $id: $contents")
        maybeReplaceMessage(owner, contents)
        currentMessageId += id
      case SnapshotOffer(metadata, contents) =>
        contents.asInstanceOf[mutable.Queue[(String, String)]].foreach(lastMessages.enqueue(_))
    }

    def maybeReplaceMessage(sender: String, contents: String): Unit = {
      if (lastMessages.size == MAX_MESSAGES) {
        lastMessages.dequeue()
      }
      lastMessages.enqueue((sender, contents))
    }

    def maybeCheckpoint(): Unit = {
      commandWithoutCheckpoint += 1
      if (commandWithoutCheckpoint >= MAX_MESSAGES) {
        saveSnapshot(lastMessages)
        commandWithoutCheckpoint = 0
      }
    }
  }

  val system = ActorSystem("snapshots")
  val shat = system.actorOf(Chat.props("anuki23", "martin843"))

}
