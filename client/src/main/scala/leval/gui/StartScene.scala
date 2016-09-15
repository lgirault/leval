package leval.gui

import leval.LevalConfig
import leval.core.{Antares, CoreRules, Helios, Rules, Sinnlos}
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
  val rulesCBox = new ComboBox[CoreRules](Seq(Sinnlos, Antares, Helios)){
    value = config.rules()
  }
  val ostein = new CheckBox("O'Stein")
  val osteinMulligan = new CheckBox(texts.allow_mulligan)

  val nedemone = new CheckBox(s"Nédémone (${texts.unsupported})"){
    selected = false
    disable = true
  }

  val janus = new CheckBox(s"Janus (${texts.unsupported})"){
    selected = false
    disable = true
  }

  def coreRules = rulesCBox.value.value
  def rules =
    Rules(coreRules,
      ostein.selected(),
      osteinMulligan.selected(),
      nedemone.selected(),
      janus.selected())

  val createGameButton = new Button(texts.create_game){
    handleEvent(MouseEvent.MouseClicked) {
      () =>
        network.config = config.withAnyRefValue(Keys.defaultRules, coreRules.id)
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
    ostein, nedemone, janus,
    createGameButton, listGameButton, settingsButton)
}
