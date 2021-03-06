package leval
package network
package server

import akka.actor.{Actor, ActorLogging, Props}
import leval.core.PlayerId


object EntryPoint {
  def props(majorVersion : Int,
            minorVersion : Int) =
    Props(new EntryPoint(majorVersion, minorVersion))
}
class EntryPoint
(val majorVersion : Int,
 val minorVersion : Int)
  extends Actor with ActorLogging {

    def checkVersion(version : String) : Boolean = {
    val a = version.split('.')
    val major = a(0).toInt
    val minor = a(1).toInt
    major == majorVersion && minor == minorVersion
  }
  /*val playersDB =
    Map(("toto", "1234") -> 0,
        ("titi", "1234") -> 1,
        ("Antares", "1234") -> 4,
        ("Sinnlos", "1234") -> 5)*/

  var id = 0
  override def receive: Receive = {
    case GuestConnect(clientVersion, login) =>
      id += 1
      if(checkVersion(clientVersion)) {
        log info s"connection of ${sender()} ack"
        sender() ! ConnectAck(PlayerId(id, login))
      }
      else
        sender() ! ConnectNack(s"Mauvaise version, veuillez utiliser un client version $majorVersion.$minorVersion")

   /* case Connect(login, pass) =>
      playersDB get((login, pass)) match {
        case Some(id) =>
          sender() ! ConnectAck(PlayerId(id, login))
        case None =>
          sender() ! ConnectNack("unknown couple login password")
      }*/

    case CreateGame(desc) =>
      leval.ignore(context.actorOf(GameMaker.props(sender(), desc)))

    case ListGame =>
      //TODO be more subtle, (manage a gameMakers list ?)
      context.children.foreach {
        _.! (ListGame) (sender())
      }
  }
}


