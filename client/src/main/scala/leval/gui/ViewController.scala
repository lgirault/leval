package leval.gui

import leval.core.PlayerId
import leval.gui.gameScreen.{GameScreenControl, ObservableGame}
import leval.network.client.BeforeWaitingRoom.MaxPlayer
import leval.network.client._

//import scalafx.Includes._
import scalafx.scene.Scene

trait ViewController
  extends InitView
  with StartScreenView {

  val network : NetWorkController
  val scene : Scene

  def displayConnectScreen() : Unit = {
    scene.root = new ConnectScene(network)
  }

  def displayStartScreen() : StartScreenView = {
    scene.root = new StartScene(network)
    this
  }

  def waitingOtherPlayerScreen :
  ( PlayerId, MaxPlayer ) => WaitingOtherPlayerView =
  ( maker, maxNumPlayer ) => {
    println("Displaying waiting room ...")
    val wr = new WaitingRoom(network, maker.name, maxNumPlayer){
      def gameScreen(game : ObservableGame) = {
          val pidx =game.stars.indexWhere(_.id == network.thisPlayer)
          val control =
            new GameScreenControl(game, pidx, network.actor)

        ViewController.this.scene.root = control.pane
      }
    }
    wr addPlayer maker
    scene.root = wr
    wr
  }



  def gameListScreen() : GameListView = {
    val gls = new GameListScene(network, this)
    scene.root = gls
    gls
  }

}
