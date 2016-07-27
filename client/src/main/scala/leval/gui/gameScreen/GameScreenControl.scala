package leval.gui.gameScreen

import akka.actor.ActorRef
import leval.core.Game.StarIdx
import leval.core._
import leval.gui.text
import leval.network.client.StartScreen

import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType


/**
  * Created by Loïc Girault on 06/07/16.
  */

object MoveSeq {

  def fromHand(c: Card): Seq[Move[_]] =
    c match {
      case Joker(_) => Seq()
      case _ =>
        Seq(RemoveFromHand(c),
          ActPhase(Set()))
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
    case Origin.BeingPane(b, s) => Seq(ActivateBeing(b.face))
  }

}

class GameScreenControl
(val game : ObservableGame,
 val playerGameIdx : StarIdx,
 val actor : ActorRef)
  extends  GameObserver {

  implicit val txt = text.Fr
  val opponentId = (playerGameIdx + 1) % 2

  def isCurrentPlayer =
    game.currentStarId == playerGameIdx

  val pane : TwoPlayerGamePane =
    new TwoPlayerGamePane(game, playerGameIdx, this)

  game.observers += this

  def numLookedCards : Int =
    game.lookedCards.size

  def numResourcesCardinal =
    game.beings.values.map(_.cards.size).sum

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
          case Heart => MajestyEffect(v, playerGameIdx)
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

  def educate(e : Educate) : Unit = {
    actor ! e
    actor ! ActPhase(Set())
  }

  def placeBeing(b: Being): Unit = {
    MoveSeq.placeBeing(b, playerGameIdx) foreach (actor ! _)
  }

  def collectFromSource() : Unit =
    actor ! CollectFromSource(playerGameIdx)

  def collectFromRiver() : Unit =
    actor ! CollectFromRiver(playerGameIdx)

  def endPhase() : Unit =
    actor ! game.nextPhase


  def canCollectFromRiver = game.beings.values exists {
    case b @ Formation(Spectre) if b.owner == game.currentStarId => true
    case _ => false
  }

  def drawAndLook(origin: Origin) : Unit = {
    def doDrawAndLook() = {
      val (numDraw, numLook) = drawAndLookValues(origin)
      new DrawAndLookAction(this, numDraw, numLook, canCollectFromRiver,
        () => MoveSeq.end(origin) foreach (actor ! _)
      ).apply()
    }
    origin match {
      case Origin.BeingPane(b, s) =>
        actor !  Reveal(b.face, s)
        doDrawAndLook()
      case Origin.Hand(Joker(j)) =>
        jokerEffectFromHand(j)
      case _ => doDrawAndLook()
    }
  }



  def jokerEffectFromHand(joker : Joker) : Unit ={
    println("joker from hand")
    joker match {
      case Joker.Red =>
        actor ! MajestyEffect(1, playerGameIdx) // heart effect
        new Alert(AlertType.Information){
          delegate.initOwner(pane.scene().getWindow)
          title = "Mind Action"
          headerText = "Click on a card or the opponent star to attack"
          //contentText = "Every being has acted"
        }.showAndWait()
        new JokerMindEffectTargetSelector(this)
      case Joker.Black =>
        new BlackJokerEffect(this)
    }
  }

  //(num draw, num look)
  def drawAndLookValues(origin : Origin) : (Int, Int) =
    origin match {
      case Origin.Hand(C(King, _)) => (1, 3)
      case Origin.Hand(C(Queen, _)) => (1, 2)
      case Origin.Hand(_) => (1, 1)
      case Origin.BeingPane(Formation(Fool), _)=> (2, 1)
      case _ => (1, 1)
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
        Seq(AttackBeing(origin,
          target.face, targetSuit),
          ActPhase(Set()))

      case Origin.BeingPane(b, s @ (Diamond | Spade)) =>
        Seq(AttackBeing(origin,
          target.face, targetSuit),
          ActivateBeing(b.face))

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

  def burry(b : Being) : Unit =
    new BurialDialog(b,
      CardImg.width,
      CardImg.height,
      pane).showAndWait() match {
      case Some(move) => actor ! move
      case None => leval.error()
    }

  def endGame(): Unit = {
    val txt = game.result match {
      case None => "Draw !"
      case Some((winner, loser)) =>
        winner.name + " wins !"
    }
    new Alert(AlertType.Information){
      delegate.initOwner(pane.scene().getWindow)
      title = "Game Over"
      headerText = txt
      //contentText = "Every being has acted"
    }.showAndWait()
    actor ! StartScreen

  }

  def checkEveryBeingHasActedAndEndPhase() =
    game.currentPhase match {
      case ap: ActPhase =>
        if (ap.activatedBeings.size == game.beings.values.count(_.owner == currentStar)) {
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

  def notify[A](m: Move[A], res: A): Unit = {
    println(game.stars(playerGameIdx).name +"'s controller notified of " + m)

    if(game.ended) endGame()
    else
      m match {
        case MajestyEffect(_, _) =>

          playerStarPanel.majestyValueLabel.text =
            stars(playerGameIdx).majesty.toString
          opponentStarPanel.majestyValueLabel.text =
            stars(opponentId).majesty.toString

        case PlaceBeing(b, side) =>
          if (playerGameIdx == side) {
            addPlayerBeingPane(b)
            createBeeingPane.menuMode()
          } else {
            addOpponentBeingPane(b)
            opponentHandPane.update()
          }

        case RemoveFromHand(_) =>
          println(game.deathRiver)
          riverPane.update()
          handPane.update()
          opponentHandPane.update()

        case CollectFromRiver(side) =>
          if (playerGameIdx == side)
            new CardDialog(res, pane).showAndWait()

          riverPane.update()
          handPane.update()
          opponentHandPane.update()

        case CollectFromSource(side) =>
          if (playerGameIdx == side)
            new CardDialog(res, pane).showAndWait()

          handPane.update()
          opponentHandPane.update()

        case LookCard(fc, s) =>
          beingPanesMap get fc foreach (_ update s)
          if(res) riverPane.update()

        case Reveal(fc, s) =>
          beingPanesMap get fc foreach (_ update s)
          if(res) riverPane.update()

        case Bury(target, _) =>
          burialOnGoing = false
          beingPanesMap get target foreach {
            bp =>
              beingsPane(bp.orientation).children.remove(bp.delegate)
          }
          beingPanesMap -= target
          riverPane.update()
          checkEveryBeingHasActedAndEndPhase()

        case ActivateBeing(fc) =>
          if(!burialOnGoing)
            checkEveryBeingHasActedAndEndPhase()

        case AttackBeing(origin, target, targetSuit) =>
          println("AttackBeing " + game.beings(target))
          game.beings(target) match {
            case (Formation(f)) =>
              beingPanesMap get target foreach (_ update targetSuit)
            case b =>
              if(b.owner != playerGameIdx) {
                origin match {
                  case Origin.BeingPane(Formation(Wizard), _) =>
                    new DrawAndLookAction(this,
                      collect = game.rules.wizardCollect,
                      look = 0, canCollectFromRiver,
                      () => MoveSeq.end(origin) foreach (actor ! _)
                    ).apply()
                  case _ => ()
                }
                if(b.cards.size > 1) {
                  actor ! BuryRequest(b)
                  alertWaitEndOfBurial()
                }
                else {
                  actor ! Bury(b.face, b.cards)
                }
              }
              if(b.cards.size > 1)
                burialOnGoing = true

            case _ => ()
          }
          origin match {
            case Origin.Hand(_) =>
              riverPane.update()
              handPane.update()
              opponentHandPane.update()
            case _ => ()
          }



        case InfluencePhase(newPlayer) =>
          if(isCurrentPlayer)
            beingPanesMap.values foreach { bp =>
              if(playerBeingsPane.children contains bp)
                bp.educateButton.visible = true
            }

          statusPane.star = game.stars(newPlayer).name
          statusPane.round = game.currentRound
          statusPane.phase = game.currentPhase

        case ActPhase(_) =>
          beingPanesMap.values foreach {
            _.educateButton.visible = false
          }
          statusPane.phase = game.currentPhase
          if (isCurrentPlayer) {
            //do not end phase automatically if no beings
            //to let the player see cards potentially
            // revealed during influence phase
            endPhaseButton.visible = true
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

        case e : Educate =>
          println("Educate update pane !")
          val bp = beingPanesMap(e.target)
          val b = game.beings(e.target)
          bp.update(b)
          if(isCurrentPlayer)
            handPane.update()
          else
            opponentHandPane.update()

        case _ => ()
      }
  }
  def showTwilight(t : Twilight) : Unit =
    new TwilightDialog(this, t){
      delegate.initOwner(pane.scene().getWindow)
    }.showAndWait()

  def canDragAndDropOnInfluencePhase() : Boolean =
    game.currentPhase match {
      case InfluencePhase(`playerGameIdx`) => true
      case _ => false
    }

  private [this] var burialOnGoing = false
  def alertWaitEndOfBurial() : Unit = {
      new Alert(AlertType.Information) {
        delegate.initOwner(pane.scene().getWindow)
        title = txt.burying
        headerText = txt.wait_end_burial
        //contentText = "Every being has acted"
      }.showAndWait()
  }


  def canDragAndDropOnActPhase(fc : Card)() : Boolean =
    game.currentStarId == playerGameIdx &&
      !burialOnGoing && (game.currentPhase match {
      case ActPhase(activatedBeings)
        if ! (activatedBeings contains fc) => true
      case _ => false
    })


}
