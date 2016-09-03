package leval.gui.gameScreen

import leval.core._
import leval.gui.gameScreen.being.BeingResourcePane
import leval.gui.text.ValText

import scalafx.Includes._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType, Dialog}
import scalafx.scene.layout.Pane

/**
  * Created by lorilan on 7/6/16.
  */
class CardDialog(c : Card, p : Pane)(implicit txt : ValText) extends Dialog[Card] {
  title = txt.card
  dialogPane().content = CardImg(c)
  delegate.initOwner(p.scene().getWindow)
  resultConverter = {
    _ => c
  }
  dialogPane().buttonTypes = Seq(ButtonType.OK)
}

class DrawAndLookAction
(val controller : GameScreenControl,
 origin : Origin,
 onFinish : () => Unit)
(implicit valText : ValText)
  extends ResourceSelector {

  var (collect, look) = controller.game.rules.drawAndLookValues(origin)

  import controller.{pane, canCollectFromRiver, numLookedCards, numResourcesCardinal}

  def canLook = look > 0 && numLookedCards < numResourcesCardinal

  val collectFromSource = new ButtonType(valText.collect_from_source)
  val collectFromRiver = new ButtonType(valText.collect_from_river)
  val lookResource = new ButtonType(valText.look_resource)
  val doNothing = new ButtonType(valText.do_nothing)


  def remainingEffect: Seq[ButtonType] = {

    val s0 =
      if (canLook) {
        if (collect > 0) Seq(lookResource)
        else Seq(lookResource, doNothing)
      } else Seq()

    if (collect > 0) {
      if (canCollectFromRiver && controller.game.deathRiver.nonEmpty)
        collectFromSource +: collectFromRiver +: s0
      else
        collectFromSource +: s0
    }
    else s0
  }

  def canSelect(b : Being, pos : Suit) : Boolean = true

  def onClick(brp : BeingResourcePane) : Unit =
    if (!(controller.game.lookedCards contains ((brp.being.face, brp.position))) ) {
      new CardDialog(brp.card, pane).showAndWait() match {
        case Some(_) =>
          controller.actor ! LookCard(origin.asInstanceOf[CardOrigin],
            brp.being.face, brp.position)
        case None => leval.error()
      }
      this.apply()
    }


  val subscriptions =
    if (! canLook) Iterable.empty
    else {
      pane.resourcesPanes foreach (_.unsetCardDragAndDrop())
      suscribe(pane.resourcesPanes)
    }


  def apply() : Unit =
    if (remainingEffect.isEmpty) {
      unsuscribeSelector(subscriptions)
      pane.resourcesPanes foreach (_.setCardDragAndDrap())
      onFinish()
    }
    else {
      val result =
        if( ! canLook && ! canCollectFromRiver)  Some(collectFromSource)
        else
          new Alert(AlertType.Confirmation) {
            delegate.initOwner(pane.scene().getWindow)
            title = valText.draw_or_look
            headerText = valText.choose_next_effect + "\n" +
              valText.remainingEffects(collect, look)

            buttonTypes = remainingEffect
          }.showAndWait()

      result match {
        case Some(`collectFromSource`) =>
          controller.collect(origin, Source, collect)
          collect -= 1
          this.apply()
        case Some(`collectFromRiver`) =>
          controller.collect(origin, DeathRiver, collect)
          collect -= 1
          this.apply()
        case Some(`lookResource`) =>
          look -= 1
        case Some(`doNothing`) =>
          look = 0
          this.apply()
        case _ => leval.error()
      }
    }

}
