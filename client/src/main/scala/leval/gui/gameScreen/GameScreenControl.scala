package leval.gui.gameScreen

import akka.actor.ActorRef
import leval.core.{ActivateBeing, Being, Card, MajestyEffect, PlaceBeing, RemoveFromHand, Reveal, Suit, _}

import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType

/**
  * Created by Loïc Girault on 06/07/16.
  */

object MoveSeq {
  def fromHand(c: Card): Seq[Move[_]] =
    Seq(RemoveFromHand(c),
      EndPhase)

  def fromBeing(b: Being, suit: Suit): Seq[Move[_]] = {
    Seq(Reveal(b.face, suit),
      ActivateBeing(b.face))
  }

  def placeBeing(b: Being, side : Int): Seq[Move[_]] = {
    val s = Seq(PlaceBeing(b, side), EndPhase)
    b match {
      case Formation(Spectre) => MajestyEffect(-5, side) +: s
      case _ => s
    }

  }

  def end(origin: Origin) : Seq[Move[_]] = origin match {
    case Origin.Hand(c) => fromHand(c)
    case Origin.BeingPane(b, s) => fromBeing(b, s)
    case Origin.CreateBeingPane(_) => Seq()
  }
}

class GameScreenControl
(val game : ObservableGame,
 val playerGameId : Int,
 val actor : ActorRef)
  extends  GameObserver {

  val opponentId = (playerGameId + 1) % 2

  def isCurrentPlayer =
    game.currentPlayer == playerGameId

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
        actor ! effect(b.value(s, Card.value).get, s)
    }

    MoveSeq.end(origin) foreach (actor ! _)
  }
  def placeBeing(b: Being): Unit = {
    MoveSeq.placeBeing(b, playerGameId) foreach (actor ! _)
  }

  def endPhase() : Unit =
    actor ! EndPhase


  def canCollectFromRiver = game.stars(game.currentPlayer).beings.values exists {
    case Formation(Spectre) => true
    case _ => false
  }

  def drawAndLook(origin: Origin) : Unit = {

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

  var revealedCards : Seq[BeingResourcePane] = Seq()
  var lookedCards : Seq[BeingResourcePane] = Seq()

  def notify[A](m: Move[A], res: A): Unit = m match {
    case MajestyEffect(_, _) =>

      playerStarPanel.majestyValueLabel.text =
        stars(playerGameId).majesty.toString
      opponentStarPanel.majestyValueLabel.text =
        stars(opponentId).majesty.toString

    case PlaceBeing(b, side) =>
      if(playerGameId == side) {
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
      if(playerGameId == side)
        new CardDialog(res, pane).showAndWait()

      riverPane.update()
      handPane.update()
      opponentHandPane.update()

    case CollectFromSource(side) =>
      if(playerGameId == side)
        new CardDialog(res, pane).showAndWait()

      handPane.update()

    case PlaceCardsToRiver(_) =>
      riverPane.update()

    case LookCard(fc, s) =>
      beingPanesMap get fc flatMap ( _ resourcePane s) foreach {
        brp =>
          brp.looked = true
          lookedCards +:= brp
      }


    case Reveal(fc, s) =>
      beingPanesMap get fc flatMap ( _ resourcePane s) foreach {
        brp =>
          brp.reveal = true
          revealedCards +:= brp
      }

    case ActivateBeing(fc) =>
      game.currentPhase match {
        case ap : ActPhase =>
          if(ap.activatedBeings.size == playerBeingsPane.children.size()){
            new Alert(AlertType.Information){
              delegate.initOwner(pane.scene().getWindow)
              title = "End of act phase"
              headerText = "Every being has acted"
              //contentText = "Every being has acted"
            }.showAndWait()
            controller.endPhase()
          }
        case _ => leval.error()
      }
    case EndPhase =>


      game.currentPhase match {
        case InfluencePhase =>
          statusPane.star = game.stars(game.currentPlayer).name
          statusPane.round = game.currentRound
        case SourcePhase  =>
          if(isCurrentPlayer) {
            new DrawAndLookAction(this, 1, 0, canCollectFromRiver,
              () => actor ! EndPhase
            ).apply()
          }
          else{
            new Alert(AlertType.Information){
              delegate.initOwner(pane.scene().getWindow)
              title = "New phase"
              headerText = "End of Act Phase"
              //contentText = "Every being has acted"
            }.showAndWait()
          }
          revealedCards foreach (_.reveal = false)
          revealedCards = Seq()
          lookedCards foreach (_.looked = false)
          lookedCards = Seq()
          endPhaseButton.visible = false
        case ActPhase(_) if isCurrentPlayer =>
          endPhaseButton.visible = true
        case _ =>
      }

      statusPane.phase = game.currentPhase


    case _ => ()
  }

  def canDragAndDropOnInfluencePhase() : Boolean =
    game.currentPlayer == playerGameId &&
      game.currentPhase == InfluencePhase

  def canDragAndDropOnActPhase(fc : Card)() : Boolean =
    game.currentPlayer == playerGameId && (game.currentPhase match {
      case ActPhase(activatedBeings) if ! (activatedBeings contains fc)=> true
      case _ => false
    })


}
