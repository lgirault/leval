package leval.gui

import leval.gui.text.ShowRules
import leval.network.GameDescription
import leval.network.client.BeforeWaitingRoom.CurrentNumPlayer
import leval.network.client.GameListView.JoinAction
import leval.network.client.NetWorkController

import scalafx.Includes._
import scalafx.scene.Node
import scalafx.scene.control.{Button, Label}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{BorderPane, FlowPane, HBox, VBox}


class GameListPane
(control : NetWorkController)
  extends BorderPane  {

  import control.texts


  val vbox = new VBox()
  val centerPane = new FlowPane(){
    children = vbox
  }
  center = centerPane
  top = new HBox(){
    children += new Label("Game list :")
    children += (new Button("Refresh"){
      handleEvent(MouseEvent.MouseClicked) {
        () =>
          vbox.children.clear()
          control.fetchGameList()
      }
    } : Node)
  }

  def appendGameToList: (GameDescription, CurrentNumPlayer, JoinAction) => Unit = {
    case (GameDescription(creator, rules), currentPlayer , join) =>

      val hbox = new HBox()
      hbox.children += new Label(s"${creator.name} - ${ShowRules(rules)} ($currentPlayer/${rules.maxPlayer})")
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
