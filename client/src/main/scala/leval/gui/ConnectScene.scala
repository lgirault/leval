package leval.gui

import leval.network.client.NetWorkController

import scalafx.scene.control.{TextField, Button}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{VBox, BorderPane}


class ConnectScene
( network : NetWorkController
  ) extends BorderPane {

  val loginTextField = new TextField()
  val passWordTextField = new TextField()


  val startButton = new Button("Connect"){
    handleEvent(MouseEvent.MouseClicked) {
      () => network.connect(loginTextField.text.value,
      passWordTextField.text.value)
    }
  }
  //padding = Insets(25)
  center = new VBox(loginTextField,
    passWordTextField,
    startButton)

}
