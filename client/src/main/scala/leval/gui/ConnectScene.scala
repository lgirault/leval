package leval.gui

import leval.network.client.NetWorkController

import scalafx.geometry.Pos
import scalafx.scene.control.{Button, TextField}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{BorderPane, FlowPane, VBox}


class ConnectScene
( network : NetWorkController
) extends BorderPane {

  val img = logoImage()

  val loginTextField = new TextField()
  //val passWordTextField = new TextField()

  val startButton = new Button("Connect"){
    handleEvent(MouseEvent.MouseClicked) {
      () => network guestConnect loginTextField.text.value
    }
  }

  /*val startButton = new Button("Connect"){
    handleEvent(MouseEvent.MouseClicked) {
      () => network.connect(loginTextField.text.value,
      passWordTextField.text.value)
    }
  }*/
  //padding = Insets(25)
  center = new VBox(new FlowPane{
    alignment = Pos.Center
    children = img
  },
    loginTextField,
    //passWordTextField,
    startButton)

}
