package leval.gui

import leval.core.PlayerId
import leval.gui.gameScreen.ObservableGame
import leval.network.client.BeforeWaitingRoom._
import leval.network.client.GameListView._
import leval.network.client._
import leval.network.protocol.GameDescription

import scalafx.scene.Scene

class ClientViewMockController(override val network: NetWorkController)
  extends ViewController
  with WaitingOtherPlayerView
  with GameListView {

  override val scene: Scene = null

  override def displayConnectScreen() : Unit = ()

  override def displayStartScreen() : StartScreenView = this


  override def waitingOtherPlayerScreen :
  ( PlayerId, MaxPlayer ) => WaitingOtherPlayerView =
    ( maker, maxNumPlayer ) => this


  def addPlayer(pid : PlayerId) : Unit = ()
  def gameReady(launcher : GameLauncher, umr : UserMapRelationship) : Unit = ()
  def gameScreen(desc : ObservableGame ) = ()

  override def gameListScreen() : GameListView = this

  override def appendGameToList: (GameDescription, CurrentNumPlayer, JoinAction) => Unit =
    (_,_,_) => ()
}

//val systemClientTestConfig = "client"
//
//val clientSystem = ActorSystem("TestingClientSystem",
//ConfigFactory.load(systemClientTestConfig))
//val netHandle = new NetWorkController {
//  override val view: ViewController = new ClientViewMockController(this)
//}
