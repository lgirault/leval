package leval.network.client

import akka.actor.{Props, Actor, ActorRef}

object GameScheduler {
  def props( numPlayers : Int) =
    Props(new GameScheduler( numPlayers))
}

class GameScheduler
(numPlayers : Int
  ) extends Actor {

  

  def waitingPlayers( playerList : List[ActorRef], numPlayersToRegister : Int) : Receive = {
    case BattleMapActorRef(player) if !(playerList contains player) =>
      context watch player

      if(numPlayersToRegister == 1 )
        context become inGame(0, (player :: playerList).toArray)
      else
        context become waitingPlayers(player :: playerList, numPlayersToRegister - 1 )

  }

  def inGame(activePlayerNum : Int, playerList : Array[ActorRef]) : Receive = {
    case action /*: BattleAction */ =>
      assert(playerList(activePlayerNum) == sender())
      playerList.foreach{p =>
        if(p != sender()) p ! action
      }
    case EndTurn =>
      val next = (activePlayerNum + 1) % playerList.length
      context become inGame(next, playerList)

  }

  override def receive: Receive = waitingPlayers(List(), numPlayers)



}
