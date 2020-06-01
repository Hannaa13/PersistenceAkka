package patterns

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor
import com.typesafe.config.ConfigFactory
import stores.SimplePersistent

import scala.collection.mutable

object EventAdapters extends App {

  // store
  // data structures
  case class Guitar(id: String, model: String, make: String)

  //command
  case class AddGuitar(guitar: Guitar, quantity: Int)

  // event
  case class GuitarAdded(guitarId: String, guitarModel: String, guitarMake: String, guitarQuantity: Int)

  class InventoryManager extends PersistentActor with ActorLogging {

    val inventory: mutable.Map[Guitar, Int] = new mutable.HashMap[Guitar, Int]()

    override def receiveCommand: Receive = {
      case AddGuitar(guitar@Guitar(id, model, make), quantity) =>
        persist(GuitarAdded(id, model, make, quantity)) { _ =>
          addGuitar(guitar, quantity)
          log.info(s"Added $quantity $guitar to inventory")
        }
    }

    override def receiveRecover: Receive = {
      case GuitarAdded(id, model, make, quantity) =>
        val guitar = Guitar(id, model, make)
        addGuitar(guitar, quantity)
    }

    def addGuitar(guitar: Guitar, quantity: Int): Unit = {
      val existingQuantity = inventory.getOrElse(guitar, 0)
      inventory.put(guitar, existingQuantity + quantity)
    }

    override def persistenceId: String = "guitar-manager"
  }

  val system  = ActorSystem("system", ConfigFactory.load().getConfig("eventAdapters"))
  val actor = system.actorOf(Props[SimplePersistent], "manager")


}
