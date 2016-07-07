package leval.gui

import leval.core.PlayerId
import leval.network.client.BeforeWaitingRoom.MaxPlayer
import leval.network.client._

//import scalafx.Includes._
import scalafx.scene.Scene

trait ViewController  {

  val network : NetWorkController
  val scene : Scene

  def displayConnectScreen() : Unit = {
    scene.root = new ConnectScene(network)
  }

  def displayStartScreen() : Unit = {
    scene.root = new StartScene(network)
  }

  def waitingOtherPlayerScreen
  ( maker : PlayerId, maxNumPlayer :MaxPlayer ) : WaitingRoom = {
    println("Displaying waiting room ...")
    val wr =  new WaitingRoom(network, maker.name, maxNumPlayer)
    wr.addPlayer(maker)
    scene.root = wr
    wr
  }

  def gameListScreen() : GameListPane  = {
    val glp = new GameListPane(network)
    scene.root = glp
    glp
  }

}
