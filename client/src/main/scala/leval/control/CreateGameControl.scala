package leval.control

import akka.actor.ActorRef
import com.typesafe.config.Config
import leval.core.{Antares, CoreRules, Helios, PlayerId, Rules, Sinnlos}
import leval.LevalConfig.{ConfigOps, Keys}
import leval.gui.ConfigDialog
import leval.network._

import scalafx.scene.control.{ButtonType, CheckBox, ComboBox}
import scalafxml.core.macros.sfxml

/**
  * Created by lorilan on 9/30/16.
  */
@sfxml
class CreateGameControl
(private val actor : ActorRef,
 private val rulesCBox : ComboBox[CoreRules],
 private val ostein : CheckBox,
 private val osteinMulligan : CheckBox,
 private val nedemone : CheckBox,
 private val janus : CheckBox,
 private val config : Config) {

  Seq(Sinnlos, Antares, Helios) foreach rulesCBox.+=
  rulesCBox.value = config.rules()

  def coreRules = rulesCBox.value.value
  def rules =
    Rules(coreRules,
      ostein.selected(),
      osteinMulligan.selected(),
      nedemone.selected(),
      janus.selected())

  def create() : Unit = {
    actor ! ConfigChange(Keys.defaultRules, coreRules.id)
    actor ! CreateRequest(rules)
  }

  def list() : Unit = actor ! ListGame

  def settings() =  {
    val cfgDial = new ConfigDialog(config, ostein.scene())
    cfgDial.showAndWait() match {
      case Some(ButtonType.OK) =>
        actor ! ConfigChange(cfgDial.values())
      case _ => ()
    }
  }

}
