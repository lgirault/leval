package leval
package network
package server

import protocol._
import akka.actor.Actor
import leval.core.Game

import scala.collection.mutable.ListBuffer



class GameMaker
  extends Actor {

  var maxNumPlayer : Int = _
  def gameOwner = players.head
  val players = ListBuffer[NetPlayerId]()
  def currentNumPlayer : Int = players.size

  

  override def receive: Receive = {
    case CreateGame(gd) =>
      gd match {
        case GameDescription(gameOwner, numPlayer) =>
        maxNumPlayer = numPlayer
        players.append(gameOwner)
        //println("new GameMaker end of init")
        gameOwner.actor ! GameCreated(gd)
      }
    case ListGame =>
      println("GameMaker receives ListGame request")
      sender() ! WaitingPlayersGameInfo(GameDescription(gameOwner, maxNumPlayer), currentNumPlayer)

    case Join(npid) =>
      //println(s"$npid wants to join")
      if(currentNumPlayer >= maxNumPlayer)
        npid.actor ! NackJoin
      else{
        npid.actor ! AckJoin(GameDescription(gameOwner, maxNumPlayer))
        players.foreach {
          pid =>
            pid.actor ! NewPlayer(npid)
            npid.actor ! NewPlayer(pid)
        }
        players.append(npid)
        if(currentNumPlayer == maxNumPlayer){
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
