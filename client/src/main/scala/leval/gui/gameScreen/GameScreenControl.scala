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

  def placeBeing(b: Being): Seq[Move[_]] = {
    Seq(PlaceBeing(b), EndPhase)
  }

  def end(c : Card, origin: Origin) : Seq[Move[_]] = origin match {
    case Origin.Hand => fromHand(c)
    case Origin.BeingPane(b, s) => fromBeing(b, s)
    case Origin.CreateBeingPane => Seq()
  }
}

class GameScreenControl
(val game : ObservableGame,
 val playerGameId : Int,
 val actor : ActorRef)
  extends  GameObserver {

  val opponentId = (playerGameId + 1) % 2

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

  def directEffect( c : Card, origin: Origin) : Unit = {
    origin match {
      case Origin.Hand if c.rank == Joker =>
        jokerEffectFromHand(c.suit)
      case Origin.Hand =>
        actor ! MajestyEffect(Card.value(c), c.suit)
      case Origin.BeingPane(b, s) =>
        actor ! MajestyEffect(b.value(s, Card.value).get, s)
    }

    MoveSeq.end(c, origin) foreach (actor ! _)
  }
  def placeBeing(b: Being): Unit = {
    MoveSeq.placeBeing(b) foreach (actor ! _)
  }

  def endPhase() : Unit =
    actor ! EndPhase


  def canCollectFromRiver = game.stars(game.currentPlayer).beings.values exists {
    case Formation(Spectre) => true
    case _ => false
  }

  def drawAndLook(c: Card, origin: Origin) : Unit = {

    val (numDraw, numLook) = drawAndLookValues(c, origin)
    new DrawAndLookAction(this, numDraw, numLook, canCollectFromRiver).apply()
    MoveSeq.end(c, origin) foreach (actor ! _)
  }

  def jokerEffectFromHand(jokerSuit : Suit) : Unit =
    new Alert(AlertType.Information){
      delegate.initOwner(pane.scene().getWindow)
      title = "ALERT !"
      headerText = "Joker effect from hand not implemented"
      //contentText = "Every being has acted"
    }.showAndWait()

  //(num draw, num look)
  def drawAndLookValues(c : Card, origin : Origin) : (Int, Int) =
    (origin, c.rank) match {
      case (Origin.Hand, King) => (1, 3)
      case (Origin.Hand, Queen) => (1, 2)
      case (Origin.Hand, _) => (1, 1)
      case (Origin.BeingPane(b, _), _) =>
        b match {
          case Formation(Fool) => (2, 1)
          case _ => (1, 1)
        }
      case (Origin.CreateBeingPane, _) => leval.error()
    }


  def playOnBeing(c : Card,
                  origin : Origin,
                  target : Being,
                  targetSuit : Suit) = {
    //targetSuit needed if club played from hand
    //heart are not played on being
    val moves : Seq[Move[_]] = (origin, c) match {
      case (Origin.Hand, (Joker, _)) =>
        jokerEffectFromHand(c.suit)
        MoveSeq.fromHand(c)
      case (Origin.Hand, (_, Diamond | Spade)) =>
        AttackBeing(Card.value(c),
          target.face, targetSuit) +:
          Reveal(target.face, targetSuit) +:
          MoveSeq.fromHand(c)
      case (Origin.BeingPane(b, s), (_, Diamond | Spade)) =>
        AttackBeing(b.value(s, Card.value).get,
          target.face, targetSuit) +:
          Reveal(target.face, targetSuit) +:
          MoveSeq.fromBeing(b,s)
      case (_, (_ , Club)) =>
        drawAndLook(c, origin)
        Seq()
      case _ => leval.error()
    }

    moves foreach (actor ! _)
  }

  import pane._
  import game._

  var revealedCards : Seq[BeingResourcePane] = Seq()

  def notify[A](m: Move[A], res: A): Unit = m match {
    case MajestyEffect(_, _) =>

      playerStarPanel.majestyValueLabel.text =
        stars(playerGameId).majesty.toString
      opponentStarPanel.majestyValueLabel.text =
        stars(opponentId).majesty.toString

    case PlaceBeing(b) =>
      if(game.currentPlayer == playerGameId) {
        addPlayerBeingPane(b)
        createBeeingPane.menuMode()
      } else {
        addOpponentBeingPane(b)
        opponentHandPane.update()
      }


    case CollectFromRiver | RemoveFromHand(_) =>
      riverPane.update()
      handPane.update()

    case CollectFromSource =>
      handPane.update()
    case PlaceCardsToRiver(_) =>
      riverPane.update()


    case Reveal(fc, s) =>
      beingPanesMap get fc foreach {
        bp =>
          bp reveal s foreach (brp => revealedCards +:= brp)

      }
    case ActivateBeing(fc) =>
      game.roundState match {
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
      new Alert(AlertType.Information){
        delegate.initOwner(pane.scene().getWindow)
        title = "New phase"
        headerText = game.currentStar.id.name + " : " + game.roundState.toString
        //contentText = "Every being has acted"
      }.showAndWait()

      game.roundState match {
        case SourcePhase  =>
          if(game.currentPlayer == playerGameId) {
            new DrawAndLookAction(this, 1, 0, canCollectFromRiver).apply()
            actor ! EndPhase
          }
          revealedCards foreach (_.hide())
          revealedCards = Seq()
          endPhaseButton.visible = false
        case ActPhase(_) if game.currentPlayer == playerGameId =>
          endPhaseButton.visible = true
        case _ =>
          endPhaseButton.visible = false
      }

    case _ => ()
  }

  def canDragAndDropOnInfluencePhase() : Boolean =
    game.currentPlayer == playerGameId &&
      game.roundState == InfluencePhase

  def canDragAndDropOnActPhase(fc : FaceCard)() : Boolean =
    game.currentPlayer == playerGameId && (game.roundState match {
      case ActPhase(activatedBeings) if ! (activatedBeings contains fc)=> true
      case _ => false
    })


}
