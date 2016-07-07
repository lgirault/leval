package leval.gui

import leval.network.client.BeforeWaitingRoom.CurrentNumPlayer
import leval.network.client.GameListView.JoinAction
import leval.network.client.NetWorkController
import leval.network.protocol.GameDescription

import scalafx.Includes._
import scalafx.scene.Node
import scalafx.scene.control.{Button, Label}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{BorderPane, FlowPane, HBox, VBox}


class GameListPane
(control : NetWorkController)
  extends BorderPane  {



  val vbox = new VBox()
  val centerPane = new FlowPane(){
    children = vbox
  }
  center = centerPane
  vbox.children = new Label("Game list :")

  def appendGameToList: (GameDescription, CurrentNumPlayer, JoinAction) => Unit = {
    case (GameDescription(creator, maxNumPlayer), currentPlayer , join) =>

      val hbox = new HBox()
      hbox.children += new Label(s"${creator.id.name} ($currentPlayer/$maxNumPlayer)")
      val node : Node = new Button("Join"){
        handleEvent(MouseEvent.MouseClicked)(join)
      }
      hbox.children += node

      val _ = vbox.children += hbox
  }
  bottom = new Button("Back"){
    handleEvent(MouseEvent.MouseClicked) {
      () => control.displayStartScreen()
    }
  }

}
