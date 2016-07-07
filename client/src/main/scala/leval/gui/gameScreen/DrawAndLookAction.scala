package leval.gui.gameScreen

import leval.core.{Card, CollectFromRiver, CollectFromSource, LookCard}
import leval.gui.CardImg

import scalafx.Includes._
import scalafx.event.subscriptions.Subscription
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType, Dialog}
import scalafx.scene.input.MouseEvent

/**
  * Created by lorilan on 7/6/16.
  */
class LookedCardDialog
( c : Card) extends Dialog[Card] {
  //initOwner(window)
  title = "Looked Card"
  dialogPane().content = CardImg(c)

  resultConverter = {
    _ => c
  }

  dialogPane().buttonTypes = Seq(ButtonType.OK)
}

class DrawAndLookAction
( controller : GameScreenControl,
  var collect : Int = 1,
  var look : Int = 1,
  canCollectFromRiver : Boolean) {

  import controller.pane

  val collectFromSource = new ButtonType("Collect from source")
  val collectFromRiver = new ButtonType("Collect from river")
  val lookCard = new ButtonType("Look card")

  def remainingEffect: Seq[ButtonType] = {
    import controller.{numLookedCards, numResourcesCardinal}
    val s0 =
      if (look > 0 && numLookedCards < numResourcesCardinal ) Seq(lookCard)
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
                    val dialog = new LookedCardDialog(brp.card) {
                      delegate.initOwner(pane.scene().getWindow)
                    }
                    dialog.showAndWait() match {
                      case Some(_) =>
                        look -= 1
                        controller.actor ! LookCard(bp.being.face, brp.position)
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
      new Alert(AlertType.Information){
        delegate.initOwner(pane.scene().getWindow)
        title = "Draw or look Action"
        headerText = "End of action"
        //contentText = "Every being has acted"
      }.showAndWait()

    }
    else
    {
      val alert = new Alert(AlertType.Confirmation) {
        delegate.initOwner(pane.scene().getWindow)
        title = "Draw or look Action"
        headerText = "Choose next effect of action"
        // contentText = "Choose your option."
        // Note that we override here default dialog buttons, OK and Cancel, with new ones.
        buttonTypes = remainingEffect
      }

      val result = alert.showAndWait()

      result match {
        case Some(`collectFromSource`) =>
          collect -= 1
          controller.actor ! CollectFromSource
          this.apply()
        case Some(`collectFromRiver`) =>
          collect -= 1
          controller.actor ! CollectFromRiver
          this.apply()
        case Some(`lookCard`) =>
          new Alert(AlertType.Information){
            delegate.initOwner(pane.scene().getWindow)
            title = "Look Action"
            headerText = "Click on a card to look at it"
            //contentText = "Every being has acted"
          }.showAndWait()
      }
    }

}
