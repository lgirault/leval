package leval.gui

import leval.network.client.NetWorkController

import scalafx.scene.control.{Button, ComboBox, Label}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{BorderPane, HBox, VBox}

class StartScene
( network : NetWorkController
  ) extends BorderPane() {
  val maxPlayer = new ComboBox[Int](Seq(2/*, 4*/)){
    value = 2
  }

  val createGameButton = new Button("Create Game"){
    handleEvent(MouseEvent.MouseClicked) {
      () => network.createGame(maxPlayer.value.value)
    }
  }

  val listGameButton = new Button("Game List"){
    handleEvent(MouseEvent.MouseClicked) {
      () => network.fetchGameList()
    }
  }
  //padding = Insets(25)
  center = new VBox(new HBox(new Label("Number of player :"), maxPlayer), createGameButton, listGameButton)
}
