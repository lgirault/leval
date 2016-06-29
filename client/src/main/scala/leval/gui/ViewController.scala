package leval.gui

import leval.core.PlayerId
import leval.network.client.BeforeWaitingRoom.MaxPlayer
import leval.network.client._
import leval.network.protocol.GameDescription

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
      def gameScreen(desc : GameDescription) = ???
      /*{
        case MapDescription(w, h, c) =>
          val bms = BattleMapScene(BattleMap(w,h).set(c))
          scene.root = bms
          bms.controller
      }*/
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
