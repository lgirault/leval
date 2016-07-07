package leval
package network
package client


import leval.core.PlayerId
import leval.gui.gameScreen.ObservableGame
import leval.network.client.BeforeWaitingRoom.CurrentNumPlayer
import leval.network.client.GameListView._
import leval.network.protocol.GameDescription


trait InitView {
  def displayStartScreen() : StartScreenView
}

object BeforeWaitingRoom{
  type MaxPlayer = Int
  type CurrentNumPlayer = Int
}

trait BeforeWaitingRoom {
  import BeforeWaitingRoom._
  def waitingOtherPlayerScreen :
    ( PlayerId, MaxPlayer ) => WaitingOtherPlayerView
}

trait StartScreenView extends BeforeWaitingRoom {
  def gameListScreen() : GameListView
}

object GameListView {
  type JoinAction = () => Unit
}

trait GameListView extends BeforeWaitingRoom {
  def appendGameToList : (GameDescription, CurrentNumPlayer, JoinAction) => Unit
}

sealed abstract class UserMapRelationship
case object Owner extends UserMapRelationship
case object Joiner extends UserMapRelationship

trait WaitingOtherPlayerView {
  def addPlayer(pid : PlayerId) :Unit
  def gameReady(launcher : GameLauncher, umr : UserMapRelationship) : Unit
  def gameScreen(desc : ObservableGame ) /*BattleMapController*/
}
