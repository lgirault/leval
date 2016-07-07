package leval.gui

import leval.core.PlayerId
import leval.gui.gameScreen.ObservableGame
import leval.network.client._

import scalafx.scene.Scene

class ClientViewMockController(override val network: NetWorkController)
  extends ViewController {

  override val scene: Scene = null

  override def displayConnectScreen() : Unit = ()

  override def displayStartScreen() : Unit = ()


  def addPlayer(pid : PlayerId) : Unit = ()
  def gameReady(launcher : NetWorkController, umr : UserMapRelationship) : Unit = ()
  def gameScreen(desc : ObservableGame ) = ()

  override def gameListScreen() : GameListPane = ???

}