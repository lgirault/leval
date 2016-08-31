package leval
package network
package server

import protocol._
import akka.actor.{Actor, ActorRef, Props, Terminated}
import leval.core.{BuryRequest, Game, Move, PlayerId}

import scala.collection.mutable.ListBuffer
import akka.actor._
import akka.event.Logging

object GameMaker {
  def props(ownerRef : ActorRef,
            description : GameDescription) =
    Props(new GameMaker(ownerRef, description))
}



class GameMaker
( ownerRef : ActorRef,
  val description : GameDescription)
  extends Actor {

  val log = Logging.getLogger(context.system, this)




  def scheduling(orderedPlayers : Array[NetPlayerId]) : Receive = {

    def disconnect(nid : NetPlayerId) : Unit = {
      orderedPlayers map (_.actor) foreach {
        _ ! Disconnect(nid.id)
      }
    }

    {
      case m: Move[_] =>
       log debug m.toString
        orderedPlayers map (_.actor) foreach {
          a => if (a != sender())
            a ! m
        }

      case br: BuryRequest =>
        log debug br.toString
        orderedPlayers(br.target.owner).actor ! br

      case Disconnect(id) =>
        disconnect((sender(), id))

        log debug s"Disconnected($id) : GameMaker stopping"

        context stop self

      case t @ Terminated(ref) =>
        val sNetId = orderedPlayers find (_.actor == ref)
        sNetId foreach disconnect

        log debug s"Terminated($ref) : GameMaker stopping"

        context stop self
    }
  }

  def receive: Receive = {

    import description._
    val players = ListBuffer[NetPlayerId]((ownerRef, description.owner))
    def currentNumPlayer : Int = players.size
    context watch ownerRef
    ownerRef ! CreateGameAck(description)
    ownerRef ! Join(owner)

    def disconnectFromId(ref : PlayerId) : Unit =
      disconnect(players.indexWhere(_.id.uuid == ref.uuid))
    def disconnectFromRef(ref : ActorRef) : Unit =
      disconnect(players.indexWhere(_.actor == ref))

    def disconnect(idx : Int) : Unit = {
      val netId = players.remove(idx)
      players foreach (_.actor ! Disconnect(netId.id))
      if(netId.id == description.owner){
        context stop self
      }
    }

    {

      case t@Terminated(ref) => disconnectFromRef(ref)
      case Disconnect(id) => disconnectFromId(id)
      case ListGame =>
        log info "GameMaker receives ListGame request"
        sender() ! PlayDescription(description, currentNumPlayer)

      case j@Join(newPlayerId) =>
        log info s"$newPlayerId wants to join"
        val newPlayerActor = sender()
        if (currentNumPlayer >= rules.maxPlayer)
          newPlayerActor ! JoinNack
        else {
          newPlayerActor ! JoinAck(description)
          players.foreach {
            pid =>
              pid.actor ! j
              newPlayerActor ! Join(pid.id)
          }
          players append ((newPlayerActor, newPlayerId))
          newPlayerActor ! j

          context watch newPlayerActor

          if (currentNumPlayer == rules.maxPlayer) {
            players.foreach {
              _.actor ! GameReady
            }
          }
        }

      case GameStart =>
        val tg = Game.gameWithoutMulligan(players map (_.id), description.rules)
        val (_, g) = tg
        players.foreach {
          _.actor ! tg
        }

        val orderedPlayers = g.stars map { s =>
          val Some(nid) = players.find(_.id == s.id)
          nid
        }

        context become scheduling(orderedPlayers.toArray)

      case msg => log debug s"$msg ignored"
    }
  }

}
