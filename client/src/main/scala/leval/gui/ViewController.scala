package leval.gui

import leval.core.PlayerId
import leval.network.client.BeforeWaitingRoom.MaxPlayer
import leval.network.client._

//import scalafx.Includes._
import scalafx.scene.Scene

trait ViewController  {
  this : NetWorkController =>

  val scene : Scene

  def displayConnectScreen() : Unit = {
    scene.root = new ConnectScene(this)
  }

  def displayStartScreen() : Unit = {
    scene.root = new StartScene(this)
    actor ! StartScreen
  }

  def waitingOtherPlayerScreen
  ( maker : PlayerId, maxNumPlayer :MaxPlayer ) : WaitingRoom = {
    println("Displaying waiting room ...")
    val wr =  new WaitingRoom(this, maker.name, maxNumPlayer)
    wr.addPlayer(maker)
    scene.root = wr
    wr
  }

  def gameListScreen() : GameListPane  = {
    val glp = new GameListPane(this)
    scene.root = glp
    glp
  }

}
