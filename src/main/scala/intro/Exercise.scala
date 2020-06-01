package intro

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor

import scala.collection.mutable

object Exercise extends App {

  case class Vote(citizenID: String, candidate: String)

  class VotingStation extends PersistentActor with ActorLogging {
    val citizens: mutable.Set[String] = new mutable.HashSet[String]()
    val poll: mutable.Map[String, Int] = new mutable.HashMap[String, Int]()

    override def persistenceId: String = "simple-voting-station"

    override def receiveCommand: Receive = {
      case vote@Vote(citizenID, candidate) =>
        if (!citizens.contains(vote.citizenID)) {
          persist(vote) { _ => // COMMAND sourcing
            log.info(s"persisted $vote")
            handleInternalStateChange(citizenID, candidate)
          }
        }
    }

    def handleInternalStateChange(citizenID: String, candidate: String): Unit = {
      citizens.add(citizenID)
      val votes = poll.getOrElse(candidate, 0)
      poll.put(candidate, votes + 1)
    }

    override def receiveRecover: Receive = {
      case vote@Vote(citizenID, candidate) =>
        log.info(s"recovered $vote")
        handleInternalStateChange(citizenID, candidate)
    }
  }

  val system = ActorSystem("system")
  val votingStation = system.actorOf(Props[VotingStation], "station")
  val votesMap = Map[String, String](
    "Alice" -> "Martin",
    "Hanna" -> "Martin",
    "Bob" -> "Roland",
    "Daniel" -> "Martin"
  )
  votesMap.keys.foreach { citizen =>
    votingStation ! Vote(citizen, votesMap(citizen))
  }

}
