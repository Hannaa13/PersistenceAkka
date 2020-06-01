name := "PersistenceAkka"

version := "0.1"

scalaVersion := "2.13.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence" % "2.5.24",

  // local levelDB stores
  "org.iq80.leveldb" % "leveldb" % "0.12",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",

  // postgreSQL
  "org.postgresql" % "postgresql" % "42.2.12",
 "com.github.dnvriend" %% "akka-persistence-jdbc" % "3.5.3",

  "com.typesafe.akka" %% "akka-persistence-cassandra" % "1.0.0",
  "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % "1.0.0" % Test


)
