package stores

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Cassandra extends App {

  val cassandraSystem = ActorSystem("local", ConfigFactory.load().getConfig("cassandraDemo"))
  val actor = cassandraSystem.actorOf(Props[SimplePersistent], "simple")

  for (i <- 1 to 10)  actor ! s"I love Akka $i"

  actor ! "print"
  actor ! "snap"

  for (i <- 11 to 20)  actor ! s"I love Akka $i"



}
