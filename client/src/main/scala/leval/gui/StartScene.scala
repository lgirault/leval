package leval.gui

import leval.LevalConfig
import leval.core.{Antares, Helios, Rules, Sinnlos}
import leval.network.client.NetWorkController

import scalafx.scene.control._
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{BorderPane, HBox, VBox}

class StartScene
( network : NetWorkController
  ) extends BorderPane {
  pane =>
  import network.config
  import LevalConfig._
  val texts = config.lang()
  val rulesCBox = new ComboBox[Rules](Seq(Sinnlos, Antares, Helios)){
    value = config.rules()
  }
  def rules = rulesCBox.value.value
  val osteinn = new CheckBox(s"O'Stein (${texts.unsupported})")

  val nedemone = new CheckBox(s"Nédémone (${texts.unsupported})")

  val janus = new CheckBox(s"Janus (${texts.unsupported})")

  val createGameButton = new Button(texts.create_game){
    handleEvent(MouseEvent.MouseClicked) {
      () =>
        network.config = config.withAnyRefValue(Keys.defaultRules, rules.id)
        network createGame rules
    }
  }

  val listGameButton = new Button(texts.join_game){
    handleEvent(MouseEvent.MouseClicked) {
      () => network.fetchGameList()
    }
  }

  val settingsButton = new Button(texts.settings){
    handleEvent(MouseEvent.MouseClicked) {
      () =>
        val cfgDial = new ConfigDialog(network.config, pane)
        cfgDial.showAndWait() match {
          case Some(ButtonType.OK) =>
            network.config = cfgDial.modifiedConfig()
            network.displayStartScreen() // "refresh" in case language settings has changed
          case _ => ()
      }
    }
  }
  //padding = Insets(25)
  center = new VBox(new HBox(new Label(texts.rules + " :"), rulesCBox),
    osteinn, nedemone, janus,
    createGameButton, listGameButton, settingsButton)
}
