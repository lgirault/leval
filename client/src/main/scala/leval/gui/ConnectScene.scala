package leval.gui

import leval._
import leval.network.client.NetWorkController

import scalafx.geometry.Pos
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, TextField}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{BorderPane, FlowPane, VBox}


class ConnectScene
( network : NetWorkController
) extends BorderPane {
  pane =>
  val img = logoImage()
  val texts = network.texts
  import LevalConfig._
  val loginTextField = new TextField(){
    text = network.config getString Keys.login


  }
  def login = loginTextField.text.value
  //val passWordTextField = new TextField()

  val startButton = new Button(texts.connect){
    handleEvent(MouseEvent.MouseClicked) {
      () =>
        if(login.isEmpty)
          ignore(new Alert(AlertType.Information){
            delegate.initOwner(pane.scene().getWindow)
            headerText = texts.empty_login
          }.showAndWait())
        else {
          network.config = network.config.withAnyRefValue(Keys.login, login)
          network guestConnect login
        }
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
