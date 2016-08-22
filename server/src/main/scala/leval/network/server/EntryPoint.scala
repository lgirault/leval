package leval
package network
package server

import protocol._

import akka.actor.{Actor, Props}
import leval.core.PlayerId


object EntryPoint {
  def props = Props(new EntryPoint())
}
class EntryPoint extends Actor {

  //val gameMakers : ListBuffer[ActorRef] = ListBuffer()

  println("EntryPoint = " + self.path)

  val playersDB =
    Map(("toto", "1234") -> 0,
        ("titi", "1234") -> 1,
        ("Antares", "1234") -> 4,
        ("Sinnlos", "1234") -> 5)

  var id = 0
  override def receive: Receive = {
    case GuestConnection(login) =>
      id += 1
      sender() ! ConnectAck(PlayerId(id, login))
   /* case Connect(login, pass) =>
      playersDB get((login, pass)) match {
        case Some(id) =>
          sender() ! ConnectAck(PlayerId(id, login))
        case None =>
          sender() ! ConnectNack("unknown couple login password")
      }*/

    case CreateGame(desc) =>
      println("Receive create game request")
      leval.ignore(context.actorOf(GameMaker.props(desc)))


    case ListGame =>
      //TODO be more subtle, (manage a gameMakers list ?)
      println("Receive list game request")
      context.children.foreach {
        _.! (ListGame) (sender())
      }
  }
}


