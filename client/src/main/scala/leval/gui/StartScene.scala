package leval.gui

import leval.core.{Antares, Helios, Rules, Sinnlos}
import leval.network.client.NetWorkController

import scalafx.scene.control.{Button, ComboBox, Label, CheckBox}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{BorderPane, HBox, VBox}

class StartScene
( network : NetWorkController
  ) extends BorderPane {
  val rules = new ComboBox[Rules](Seq(Sinnlos, Antares, Helios)){
    value = Sinnlos
  }
  val osteinn = new CheckBox("O'Stein (non supporté pour le moment)")

  val nedemone = new CheckBox("Nédémone (non supporté pour le moment)")

  val janus = new CheckBox("Janus (non supporté pour le moment)")

  val createGameButton = new Button("Create Game"){
    handleEvent(MouseEvent.MouseClicked) {
      () => network.createGame(rules.value.value)
    }
  }

  val listGameButton = new Button("Game List"){
    handleEvent(MouseEvent.MouseClicked) {
      () => network.fetchGameList()
    }
  }
  //padding = Insets(25)
  center = new VBox(new HBox(new Label("Rules :"), rules), osteinn, nedemone, janus, createGameButton, listGameButton)
}
