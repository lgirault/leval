package leval
package network
package server

import protocol._
import akka.actor.{Actor, ActorRef, Props, Terminated}
import leval.core.{BuryRequest, Game, Move}

import scala.collection.mutable.ListBuffer
import akka.actor._
import akka.event.Logging

object GameMaker {
  def props(description : GameDescription) =
    Props(new GameMaker(description))
}



class GameMaker
(val description : GameDescription)
  extends Actor {

  val log = Logging.getLogger(context.system, this)




  def scheduling(orderedPlayers : Array[NetPlayerId]) : Receive = {

    def disconnect(nid : NetPlayerId) : Unit = {
      orderedPlayers map (_.actor) foreach {
        _ ! Disconnected(nid)
      }
    }

    {
      case m: Move[_] =>
        orderedPlayers map (_.actor) foreach {
          a => if (a != sender())
            a ! m
        }

      case br: BuryRequest =>
        orderedPlayers(br.target.owner).actor ! br

      case Disconnected(nid) =>
        disconnect(nid)
        context stop self

      case t @ Terminated(ref) =>
        val sNetId = orderedPlayers find (_.actor == ref)
        sNetId foreach disconnect

        context stop self
    }
  }

  def receive: Receive = {

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


    {

      case t@Terminated(ref) => disconnect(ref)
      case Disconnected(netId) => disconnect(netId.actor)
      case ListGame =>
        log info "GameMaker receives ListGame request"
        sender() ! WaitingPlayersGameInfo(description, currentNumPlayer)

      case j@Join(newPlayerId) =>
        log info s"$newPlayerId wants to join"
        if (currentNumPlayer >= rules.maxPlayer)
          newPlayerId.actor ! NackJoin
        else {
          newPlayerId.actor ! AckJoin(description)
          players.foreach {
            pid =>
              pid.actor ! j
              newPlayerId.actor ! Join(pid)
          }
          players append newPlayerId
          newPlayerId.actor ! j

          context watch newPlayerId.actor

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
        log info "Gamestart : GameMaker stopping"

        val orderedPlayers = g.stars map { s =>
          val Some(nid) = players.find(_.id == s.id)
          nid
        }

        context become scheduling(orderedPlayers.toArray)

      case msg => log debug s"$msg ignored"
    }
  }

}
