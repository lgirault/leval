package leval.gui.gameScreen

import leval.core.{Card, LookCard}

import scalafx.Includes._
import scalafx.event.subscriptions.Subscription
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType, Dialog}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.Pane

/**
  * Created by lorilan on 7/6/16.
  */
class CardDialog( c : Card, p : Pane) extends Dialog[Card] {
  //initOwner(window)
  title = "Card"
  dialogPane().content = CardImg(c)
  delegate.initOwner(p.scene().getWindow)
  resultConverter = {
    _ => c
  }


  dialogPane().buttonTypes = Seq(ButtonType.OK)
}

class DrawAndLookAction
( controller : GameScreenControl,
  var collect : Int = 1,
  var look : Int = 1,
  canCollectFromRiver : Boolean,
  onFinish : () => Unit) {

  import controller.pane

  import controller.{numLookedCards, numResourcesCardinal}
  def canLook = look > 0 && numLookedCards < numResourcesCardinal

  val collectFromSource = new ButtonType("Collect from source")
  val collectFromRiver = new ButtonType("Collect from river")
  val lookCard = new ButtonType("Look card")

  def remainingEffect: Seq[ButtonType] = {

    val s0 =
      if (canLook) Seq(lookCard)
      else Seq()

    if (collect > 0) {
      if (canCollectFromRiver && controller.game.deathRiver.nonEmpty)
        collectFromSource +: collectFromRiver +: s0
      else
        collectFromSource +: s0
    }
    else s0
  }

  val subscriptions: Iterable[Subscription] =
    if (look == 0) Iterable.empty
    else
      pane.beingPanes flatMap {
        bp =>
          bp.resourcePanes map {
            brp =>
              brp.unsetCardDragAndDrop()
              brp.handleEvent(MouseEvent.MouseClicked) {
                me: MouseEvent =>
                  if (!(controller.game.lookedCards contains ((bp.being.face, brp.position))) ) {
                    new CardDialog(brp.card, pane).showAndWait() match {
                      case Some(_) =>
                        controller.actor ! LookCard(bp.being.face, brp.position)
                      case None => leval.error()
                    }
                    this.apply()
                  }
              }
          }
      }

  def apply() : Unit =
    if (remainingEffect.isEmpty) {
      subscriptions foreach (_.cancel)
      pane.beingPanes foreach (
        _.resourcePanes foreach ( _.setCardDragAndDrap() )
        )
      onFinish()
    }
    else {
      println("Draw and look")
      println("Draw  = " + collect)
      println("look  = " + look)
      val result =
        if( ! canLook && ! canCollectFromRiver)  Some(collectFromSource)
        else
          new Alert(AlertType.Confirmation) {
            delegate.initOwner(pane.scene().getWindow)
            title = "Draw or look Action"
            headerText = s"Choose next effect of action\n(Draw $collect card(s), look $look card(s)"
            buttonTypes = remainingEffect
          }.showAndWait()

      println("result =" + result)

      result match {
        case Some(`collectFromSource`) =>
          println("collect from source !")
          collect -= 1
          controller.collectFromSource()
          this.apply()
        case Some(`collectFromRiver`) =>
          println("collect from river !")
          collect -= 1
          controller.collectFromRiver()
          this.apply()
        case Some(`lookCard`) =>
          println("look !")
          look -= 1
          new Alert(AlertType.Information){
            delegate.initOwner(pane.scene().getWindow)
            title = "Look Action"
            headerText = "Click on a card to look at it"
            //contentText = "Every being has acted"
          }.showAndWait()
        case _ => leval.error()
      }
    }

}
