package intro

import java.util.Date

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor


object PersistentActor extends App {

  // COMMANDS
  case class Invoice(recipient: String, date: Date, amount: Int)
  case class InvoiceBulk(invoices: List[Invoice])

  // EVENTS
  case class InvoiceRecorded(id: Int, recipient: String, date: Date, amount: Int)

  class Accountant extends PersistentActor with ActorLogging {

    var latestInvoiceId = 0
    var totalAmount = 0

    override def persistenceId: String = "simple" // unique

    override def receiveCommand: Receive = { // normal receive
      case Invoice(recipient, date, amount) => // command
        log.info(s"Receive invoice for amount $amount")
        val event = InvoiceRecorded(latestInvoiceId, recipient, date, amount) // send to the journal
        persist(event) /*промежуток времени: messages are stashed*/ { e => //callback
          // update state
          // SAFE TO ACCESS MUTABLE STATE
          latestInvoiceId += 1
          totalAmount += amount
          log.info(s"persisted $e as invoice #${e.id}, for total amount $totalAmount")
    }
      /**
       * Persisting MULTIPLE events
       * persistAll
       */
      case InvoiceBulk(invoices) =>
        //1. create events
        val invoiceId = latestInvoiceId to (latestInvoiceId + invoices.size)
        val events = invoices.zip(invoiceId).map { pair =>
          val id = pair._2
          val invoice = pair._1

          InvoiceRecorded(id, invoice.recipient, invoice.date, invoice.amount) // list of invoices
        }
          // persist all the events
          persistAll(events) { e => // для каждого
            // update the actor state
            latestInvoiceId +=1
            totalAmount += e.amount
            log.info(s"persisted SINGLE $e as invoice #${e.id}, for total amount $totalAmount")
          }
    }

    override def receiveRecover: Receive = { // called on recovery
      case InvoiceRecorded(id, _, _, amount) => // follow the logic in the persist steps of receiveCommand
        log.info(s"recovered invoice #$id  for  amount $amount, total $totalAmount")

        latestInvoiceId += id
        totalAmount += amount
    }
    /**
     * FAILURES
     */
    // 1. called if persisting failed. The actor will be STOPPED.
    override def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit = {
      log.error(s"error $event because $cause")
      super.onPersistFailure(cause, event, seqNr)
    }
    //2. called if journal fails. The actor is RESUMED.
    override def onPersistRejected(cause: Throwable, event: Any, seqNr: Long): Unit = {
      log.error(s"Persist Rejected $event because $cause")
      super.onPersistRejected(cause, event, seqNr)
    }
  }

  val system = ActorSystem("persistence")
  val accountant = system.actorOf(Props[Accountant], "simpleAcc")
//
//  for (i <- 1 to 10) {
//    accountant ! Invoice("Company", new Date, i * 1000)
//  }

 val newInvoices = for (i <- 1 to 5) yield Invoice("someInvoice", new Date, i * 30)
  accountant ! InvoiceBulk(newInvoices.toList)

}
