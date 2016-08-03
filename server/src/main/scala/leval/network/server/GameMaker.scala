package leval
package network
package server

import protocol._
import akka.actor.{Actor, ActorRef, Props, Terminated}
import leval.core.Game

import scala.collection.mutable.ListBuffer



object GameMaker {
  def props(description : GameDescription) =
    Props(new GameMaker(description))
}

class GameMaker
(val description : GameDescription)
  extends Actor {

  import description._
  val players = ListBuffer[NetPlayerId](description.owner)
  def currentNumPlayer : Int = players.size
  context watch owner.actor
  owner.actor ! GameCreated(description)


  def disconnect(ref : ActorRef) : Unit = {
    val idx = players.indexWhere(_.actor == ref)
    val netId = players.remove(idx)
    players foreach (_.actor ! Disconnected(netId))
    if(ref == owner.actor){
      context stop self
    }
  }

  override def receive: Receive = {

    case t @ Terminated(ref) => disconnect(ref)
    case Disconnected(netId) => disconnect(netId.actor)


    case ListGame =>
      println("GameMaker receives ListGame request")
      sender() ! WaitingPlayersGameInfo(description, currentNumPlayer)

    case Join(npid) =>
      //println(s"$npid wants to join")
      if(currentNumPlayer >= rules.maxPlayer)
        npid.actor ! NackJoin
      else{
        npid.actor ! AckJoin(description)
        players.foreach {
          pid =>
            pid.actor ! NewPlayer(npid)
            npid.actor ! NewPlayer(pid)
        }
        players.append(npid)
        if(currentNumPlayer == rules.maxPlayer){
          players.foreach {
            _.actor ! GameReady
          }
        }
      }

    case GameStart =>
      val g = Game.gameWithoutMulligan(players map (_.id))

      players.foreach {
        _.actor ! g
      }
      println("Gamestart : GameMaker stopping")
      context stop self

    case msg => println(s"$msg ignored")
  }

}
