package stores

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, RecoveryCompleted, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}

class SimplePersistent extends PersistentActor with ActorLogging {
    override def persistenceId: String = "simple"

    var nMsg = 0
    override def receiveCommand: Receive = {
      case "print" =>
        log.info(s"persisted $nMsg")
      case "snap" =>
        saveSnapshot(nMsg)
      case SaveSnapshotSuccess(metadata) =>
        log.info(s"successful $metadata")
      case SaveSnapshotFailure(_, cause) =>
        log.info(s"$cause")
      case msg => persist(msg) { _ =>
        log.info(s"persisting $msg")
        nMsg += 1
      }
    }

    override def receiveRecover: Receive = {
      case RecoveryCompleted =>
        log.info("done")
      case SnapshotOffer(_, payload: Int) =>
        log.info(s"recovered $payload")
        nMsg = payload
      case msg =>
        log.info(s"recovered $msg")
        nMsg += 1
    }
  }