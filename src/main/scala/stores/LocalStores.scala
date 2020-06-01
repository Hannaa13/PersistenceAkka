package stores

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object LocalStores extends App {

  val localSystem = ActorSystem("local", ConfigFactory.load().getConfig("localStores"))
  val actor = localSystem.actorOf(Props[SimplePersistent], "simple")

  for (i <- 1 to 10)  actor ! s"I love Akka $i"

  actor ! "print"
  actor ! "snap"

  for (i <- 11 to 20)  actor ! s"I love Akka $i"


  }
