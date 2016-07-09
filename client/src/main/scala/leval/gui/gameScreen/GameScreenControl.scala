package leval.gui.gameScreen

import akka.actor.ActorRef
import leval.core.{ActivateBeing, Being, Card, MajestyEffect, PlaceBeing, RemoveFromHand, Reveal, Suit, _}
import leval.gui.text
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType

/**
  * Created by Loïc Girault on 06/07/16.
  */

object MoveSeq {

  def fromHand(c: Card): Seq[Move[_]] =
    Seq(RemoveFromHand(c),
      ActPhase(Set()))

  def fromBeing(b: Being, suit: Suit): Seq[Move[_]] = {
    Seq(ActivateBeing(b.face))
  }

  def placeBeing(b: Being, side : Int): Seq[Move[_]] = {
    val s = Seq(PlaceBeing(b, side), ActPhase(Set()))
    b match {
      case Formation(Spectre) => MajestyEffect(-5, side) +: s
      case _ => s
    }

  }

  def end(origin: Origin) : Seq[Move[_]] = origin match {
    case Origin.Hand(c) =>  fromHand(c)
    case Origin.BeingPane(b, s) => fromBeing(b, s)
    case Origin.CreateBeingPane(_) => Seq()
  }
}

class GameScreenControl
(val game : ObservableGame,
 val playerGameId : Int,
 val actor : ActorRef)
  extends  GameObserver {

  implicit val txt = text.Fr
  val opponentId = (playerGameId + 1) % 2

  def isCurrentPlayer =
    game.currentStarId == playerGameId

  val pane : TwoPlayerGamePane =
    new TwoPlayerGamePane(game, playerGameId, this)

  game.observers += this

  def numLookedCards : Int =
    game.lookedCards.size

  def numResourcesCardinal = {
    def aux(s : Star) : Int =
      s.beings.values.map(_.cards.size).sum

    (game.stars map aux).sum
  }

  def forbiddenOnFirstRound(origin: Origin) : Boolean =
    origin match {
      case Origin.Hand(C(_, Diamond | Spade))
           | Origin.BeingPane(_, Diamond | Spade) => true
      case _ => false
    }

  def cannotAttackAlert() : Unit =
    new Alert(AlertType.Information){
      delegate.initOwner(pane.scene().getWindow)
      title = "Forbidden"
      headerText = "You cannot attack during the first round"
    }.showAndWait()

  def directEffect(origin: Origin) : Unit =
    if(game.currentRound == 1 && forbiddenOnFirstRound(origin))
      cannotAttackAlert()
    else {

      def effect(v : Int, playedSuit : Suit) =
        playedSuit match {
          case Heart => MajestyEffect(v, playerGameId)
          case Diamond | Spade => MajestyEffect(-1 * v, opponentId)
          case _ => leval.error()
        }

      origin match {
        case Origin.Hand(Joker(j)) =>
          jokerEffectFromHand(j)
        case Origin.Hand(c @ C(_, suit)) =>
          actor ! effect(Card.value(c), suit)
        case Origin.BeingPane(b, s)=>
          actor ! Reveal(b.face, s)
          actor ! effect(b.value(s, Card.value).get, s)
      }

      MoveSeq.end(origin) foreach (actor ! _)
    }
  def placeBeing(b: Being): Unit = {
    MoveSeq.placeBeing(b, playerGameId) foreach (actor ! _)
  }

  def collectFromSource() : Unit =
    actor ! CollectFromSource(playerGameId)

  def collectFromRiver() : Unit =
    actor ! CollectFromRiver(playerGameId)

  def endPhase() : Unit =
    actor ! game.nextPhase


  def canCollectFromRiver = game.stars(game.currentStarId).beings.values exists {
    case Formation(Spectre) => true
    case _ => false
  }

  def drawAndLook(origin: Origin) : Unit = {

    origin match {
      case Origin.BeingPane(b, s) =>
        actor !  Reveal(b.face, s)
      case _ => ()
    }
    val (numDraw, numLook) = drawAndLookValues(origin)
    new DrawAndLookAction(this, numDraw, numLook, canCollectFromRiver,
      () => MoveSeq.end(origin) foreach (actor ! _)
    ).apply()

  }

  def jokerEffectFromHand(jokerSuit : Joker) : Unit = {
    // /!\ premier tour que effet du trèfle ou que effet du cœur
    new Alert(AlertType.Information) {
      delegate.initOwner(pane.scene().getWindow)
      title = "ALERT !"
      headerText = "Joker effect from hand not implemented"
      //contentText = "Every being has acted"
    }.showAndWait()
  }

  //(num draw, num look)
  def drawAndLookValues(origin : Origin) : (Int, Int) =
    origin match {
      case Origin.Hand(C(King, _)) => (1, 3)
      case Origin.Hand(C(Queen, _)) => (1, 2)
      case Origin.Hand(_) => (1, 1)
      case Origin.BeingPane(b, _)=>
        b match {
          case Formation(Fool) => (2, 1)
          case _ => (1, 1)
        }
      case Origin.CreateBeingPane(_) => leval.error()
    }


  def playOnBeing(origin : Origin,
                  target : Being,
                  targetSuit : Suit) = {
    //targetSuit needed if club played from hand
    //heart are not played on being
    val moves : Seq[Move[_]] = origin match {
      case Origin.Hand(Joker(clr)) =>
        jokerEffectFromHand(clr)
        MoveSeq.fromHand(clr)
      case Origin.Hand(c @ C(_, Diamond | Spade)) =>
        AttackBeing(Card.value(c),
          target.face, targetSuit) +:
          Reveal(target.face, targetSuit) +:
          MoveSeq.fromHand(c)
      case Origin.BeingPane(b, s @ (Diamond | Spade)) =>
          Reveal(b.face, s) +:
          AttackBeing(b.value(s, Card.value).get,
            target.face, targetSuit) +:
          Reveal(target.face, targetSuit) +:
          MoveSeq.fromBeing(b,s)
      case Origin.Hand( C(_, Club) )|
           Origin.BeingPane(_, Club) =>
        drawAndLook(origin)
        Seq()
      case _ => leval.error()
    }

    moves foreach (actor ! _)
  }

  import pane._
  import game._

  def notify[A](m: Move[A], res: A): Unit = {
    println(game.stars(playerGameId).name +"'s controller notified of " + m)
    m match {
      case MajestyEffect(_, _) =>

        playerStarPanel.majestyValueLabel.text =
          stars(playerGameId).majesty.toString
        opponentStarPanel.majestyValueLabel.text =
          stars(opponentId).majesty.toString

      case PlaceBeing(b, side) =>
        if (playerGameId == side) {
          addPlayerBeingPane(b)
          createBeeingPane.menuMode()
        } else {
          addOpponentBeingPane(b)
          opponentHandPane.update()
        }

      case RemoveFromHand(_) =>
        riverPane.update()
        handPane.update()
        opponentHandPane.update()

      case CollectFromRiver(side) =>
        if (playerGameId == side)
          new CardDialog(res, pane).showAndWait()

        riverPane.update()
        handPane.update()
        opponentHandPane.update()

      case CollectFromSource(side) =>
        println("CollectFromSource(" + side + ")")
        println("playerGameId = " + playerGameId)
        if (playerGameId == side)
          new CardDialog(res, pane).showAndWait()

        handPane.update()

      case PlaceCardsToRiver(_) =>
        riverPane.update()

      case LookCard(fc, s) =>
        beingPanesMap get fc foreach (_ update s)


      case Reveal(fc, s) =>
        beingPanesMap get fc foreach (_ update s)

      case ActivateBeing(fc) =>
        game.currentPhase match {
          case ap: ActPhase =>
            if (ap.activatedBeings.size == game.currentStar.beings.size) {
              new Alert(AlertType.Information) {
                delegate.initOwner(pane.scene().getWindow)
                title = txt.end_of_act_phase
                headerText = txt.every_being_has_acted
                //contentText = "Every being has acted"
              }.showAndWait()

              if(isCurrentPlayer)
                controller.endPhase()
            }
          case _ => leval.error()
        }

      case InfluencePhase(newPlayer) =>
        println("******************************************")
        println("******************************************")
        println("************ round " + game.currentRound + "******************")
        println("******************************************")

        statusPane.star = game.stars(newPlayer).name
        statusPane.round = game.currentRound
        statusPane.phase = game.currentPhase

      case ActPhase(_) =>
        statusPane.phase = game.currentPhase
        if (isCurrentPlayer) {
          if (game.currentStar.beings.nonEmpty)
            endPhaseButton.visible = true
          else
            endPhase()
        }
        statusPane.phase = game.currentPhase

      case SourcePhase =>
        if (isCurrentPlayer)
          new DrawAndLookAction(this, 1, 0, canCollectFromRiver,
            () => actor ! game.nextPhase
          ).apply()
        else
          new Alert(AlertType.Information) {
            delegate.initOwner(pane.scene().getWindow)
            title = txt.end_of_act_phase
            headerText = txt.end_of_act_phase
            //contentText = "Every being has acted"
          }.showAndWait()

        beingPanesMap.values foreach (_.update())
        endPhaseButton.visible = false

        statusPane.phase = game.currentPhase


      case _ => ()
    }
  }
  def showTwilight(t : Twilight) : Unit =
    new TwilightDialog(this, t){
      delegate.initOwner(pane.scene().getWindow)
    }.showAndWait()

  def canDragAndDropOnInfluencePhase() : Boolean =
      game.currentPhase match {
        case InfluencePhase(`playerGameId`) => true
        case _ => false
      }

  def canDragAndDropOnActPhase(fc : Card)() : Boolean =
    game.currentStarId == playerGameId && (game.currentPhase match {
      case ActPhase(activatedBeings)
        if ! (activatedBeings contains fc) => true
      case _ => false
    })


}
