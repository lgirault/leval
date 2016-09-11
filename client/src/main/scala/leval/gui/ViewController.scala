package leval.gui

import leval.core.{PlayerId, Rules}
import leval.gui.gameScreen.{GameScreenControl, ObservableGame}
import leval.network.client._

import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType

//import scalafx.Includes._
import scalafx.scene.Scene

trait ViewController  {
  this : NetWorkController =>

  val scene : Scene

  def exit() : Unit

  def displayConnectScreen() : Unit = {
    scene.root = new ConnectScene(this)
  }

  def displayStartScreen() : Unit = {
    scene.root = new StartScene(this)
    actor ! StartScreen
  }

  def waitingOtherPlayerScreen
  ( maker : PlayerId, rules : Rules ) : WaitingRoom = {
    val wr =  new WaitingRoom(this, maker.name, rules)
    scene.root = wr
    wr
  }

  def gameScreen(game : ObservableGame) : GameScreenControl ={
    val pidx = game.stars.indexWhere(_.id == thisPlayer)
    new GameScreenControl(scene, game, pidx, actor, config)
  }

  def gameListScreen() : GameListPane  = {
    val glp = new GameListPane(this)
    scene.root = glp
    glp
  }

  def connectError(msg : String) : Unit = {
    new Alert(AlertType.Confirmation) {
      delegate.initOwner(scene.getWindow)
      title = "Erreur de connection"
      headerText = msg
    }.showAndWait()
    exit()

  }
}
