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
  owner.actor ! Join(owner)

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

    case j @ Join(newPlayerId) =>
      println(s"$newPlayerId wants to join")
      if(currentNumPlayer >= rules.maxPlayer)
        newPlayerId.actor ! NackJoin
      else{
        newPlayerId.actor ! AckJoin(description)
        players.foreach {
          pid =>
            pid.actor ! j
            newPlayerId.actor ! Join(pid)
        }
        players append newPlayerId
        newPlayerId.actor ! j

        if(currentNumPlayer == rules.maxPlayer){
          players.foreach {
            _.actor ! GameReady
          }
        }
      }

    case GameStart =>
      val tg = Game.gameWithoutMulligan(players map (_.id), description.rules)
      val (t,g) = tg
      val numCards = g.stars.foldLeft(g.source.length){
        case (acc, s) => acc + s.hand.size
      }
      val tcards = t.cards.map(_.size).sum
      println("!!!!!!!!!!!!!!!!!! game cards = " + numCards)
      println("!!!!!!!!!!!!!!!!!! twilight cards = " + tcards)

      players.foreach {
        _.actor ! tg
      }
      println("Gamestart : GameMaker stopping")
      context stop self

    case msg => println(s"$msg ignored")
  }

}
