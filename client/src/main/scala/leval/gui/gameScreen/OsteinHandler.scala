package leval.gui.gameScreen

import leval.core.Game.StarIdx
import leval.core.{Card, GameInit, InfluencePhase, MutableGame, OsteinSelection, Star, Twilight}

import scalafx.application.Platform
import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.layout.{FlowPane, GridPane}

/**
  * Created by lorilan on 9/15/16.
  */
object OsteinHandler {
  implicit class OsteinGameOps( val mg : MutableGame ) extends AnyVal {

    def setStars(newStars : Seq[Star]) : Unit =
      mg.game = mg.game.copy( stars = newStars )

    import leval.core.Game.SeqOps

    def clearHands() : Unit =
      setStars(mg.stars map (_.copy(hand = Star.emptyHand)))


    def addCard(sId : StarIdx, c : Card) : Unit =
      setStars(mg.stars.set(sId, _ + c))


    def doTwilight() : Twilight = {
      val (newSource, Seq(h1, h2)) = GameInit.doTwilight(mg.source)
      val firstPlayer =
        if(Card.value(h1.head) > Card.value(h2.head)) 0
        else 1

      val Seq(s1, s2) = mg.stars
      mg.game = mg.game.copy(source = newSource,
        stars = Seq(s1 ++ h1, s2 ++ h2),
        currentStarIdx = firstPlayer,
        currentPhase = InfluencePhase(firstPlayer))
      Twilight(Seq(h1, h2))
    }
  }
}
import OsteinHandler._
class OsteinHandler
(val control: GameScreenControl) {
  import control._

  val hands = new Array[Set[Card]](2)
  def hand = hands(playerGameIdx)

  var thisChose = false
  var opponentChose = false

  def swapHands(): Unit = {
    val h0 = hands(0)
    hands(0) = hands(1)
    hands(1) = h0
  }

  def checkAndSwap() = synchronized {
    if(thisChose && opponentChose) {
      swapHands()
      thisChose = false
      opponentChose = false
      pick()
    }
  }
  def pick(starIdx: StarIdx, card: Card) = {
    game.addCard(starIdx, card)
    hands(starIdx) -= card
  }

  def opponentPick(card : Card) : Unit = {
    pick((playerGameIdx + 1) % 2, card)
    pane.opponentHandPane.update()
    opponentChose = true
    checkAndSwap()
  }

  val msg = new FlowPane {
    children = new Label(texts.draft_ongoing){
      alignmentInParent = Pos.Center
    }
  }

  def pick(): Unit =
    if(hand.isEmpty) {
      pane.children remove msg
      actor ! game.doTwilight()
    }
    else
      Platform runLater (new OSteinDialog(pane, hand).showAndWait() match {
        case Some(c : Card) =>
          pick(playerGameIdx, c)
          pane.handPane.update()
          control.actor ! OsteinSelection(c)
          thisChose = true
          checkAndSwap()
        case _ => leval.error()
      })

  def start() : Unit = {
    GridPane.setConstraints(msg, 1, 2)
    pane.children add msg
    game.stars.zipWithIndex foreach {
      case (star, idx) =>
        hands(idx) = star.hand
        println(hands(idx).size)
    }

    game.clearHands()
    pane.handPane.update()
    pane.opponentHandPane.update()
    pick()
  }
}
