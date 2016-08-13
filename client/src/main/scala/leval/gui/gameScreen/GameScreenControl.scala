package leval.gui.gameScreen

import akka.actor.ActorRef
import leval.ignore
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

  def end(origin: CardOrigin) : Seq[Move[_]] = origin match {
    case CardOrigin.Hand(_,c) =>  fromHand(c)
    case CardOrigin.Being(b, s) => Seq(ActivateBeing(b.face))
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

  if(isCurrentPlayer)
    pane.endPhaseButton.visible = true

  game.observers += this

  def numLookedCards : Int =
    game.lookedCards.size

  def numResourcesCardinal =
    game.beings.values.map(_.cards.size).sum

  def forbiddenOnFirstRound(origin: CardOrigin) : Boolean =
    origin match {
      case CardOrigin.Hand(_, C(_, Diamond | Spade))
           | CardOrigin.Hand(_, Joker(_))
           | CardOrigin.Being(_, Diamond | Spade) => true
      case _ => false
    }

  def cannotAttackAlert() : Unit =
    ignore(new Alert(AlertType.Information){
      delegate.initOwner(pane.scene().getWindow)
      title = txt.forbidden
      headerText = txt.cannot_attack_on_first_round
    }.showAndWait())

  def directEffect(origin: CardOrigin) : Unit =
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
        case CardOrigin.Hand(_, Joker(j)) =>
          jokerEffectFromHand(j)
        case CardOrigin.Hand(_, c @ C(_, suit)) =>
          actor ! effect(Card.value(c), suit)
        case CardOrigin.Being(b, s)=>
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

  def collect(origin : Origin, target : CollectTarget) : Unit =
    actor ! Collect(origin, target)


  def endPhase() : Unit =
    actor ! game.nextPhase


  def canCollectFromRiver = game.beings.values exists {
    case b @ Formation(Spectre) if b.owner == game.currentStarId =>
      game.deathRiver.nonEmpty
    case _ => false
  }

  def drawAndLook(origin: CardOrigin) : Unit = {
    def doDrawAndLook() = ignore(
      new DrawAndLookAction(this, origin,
        () => MoveSeq.end(origin) foreach (actor ! _)
      ).apply())
    origin match {
      case CardOrigin.Being(b, s) =>
        actor !  Reveal(b.face, s)
        doDrawAndLook()
      case CardOrigin.Hand(_, Joker(j)) =>
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
          title = txt.mind
          headerText = txt.select_to_attack
        }.showAndWait()
        ignore(new JokerMindEffectTargetSelector(this))
      case Joker.Black =>
        ignore(new BlackJokerEffect(this))
    }
  }

  //(num draw, num look)



  def playOnBeing(origin : CardOrigin,
                  target : Being,
                  targetSuit : Suit) = {
    //targetSuit needed if club played from hand
    //heart are not played on being
    val moves : Seq[Move[_]] = origin match {
      case CardOrigin.Hand(_, Joker(clr)) =>
        jokerEffectFromHand(clr)
        MoveSeq.fromHand(clr)

      case CardOrigin.Hand(_, c @ C(_, Diamond | Spade)) =>
        Seq(AttackBeing(origin,target, targetSuit),
          ActPhase(Set()))

      case CardOrigin.Being(b, s @ (Diamond | Spade)) =>
        Seq(Reveal(b.face, s),
          AttackBeing(origin, target, targetSuit),
          ActivateBeing(b.face))

      case CardOrigin.Hand(_,  C(_, Club) ) |
           CardOrigin.Being(_, Club) =>
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
    new Alert(AlertType.Information){
      delegate.initOwner(pane.scene().getWindow)
      title = txt.game_over
      headerText = game.result match {
        case None => txt.both_lose
        case Some((winner, loser)) =>
          winner.name + txt.wins
      }
      //contentText = "Every being has acted"
    }.showAndWait()
    actor ! StartScreen

  }

  def disconnectedPlayerAlert(name : String) : Unit = {
    new Alert(AlertType.Information) {
      delegate.initOwner(pane.scene().getWindow)
      title = txt.game_over
      headerText = txt.disconnected(name)
    }.showAndWait()
    actor ! StartScreen
  }

  def checkEveryBeingHasActedAndEndPhase() =
    game.currentPhase match {
      case ActPhase(activatedBeings) =>
        println("checking it !")
        if (activatedBeings.size == game.beingsOwnBy(currentStarId).size &&
          isCurrentPlayer) {
          println("EveryBeingHasActedAndEndPhase !")

          new Alert(AlertType.Information) {
            delegate.initOwner(pane.scene().getWindow)
            title = txt.end_of_act_phase
            headerText = txt.every_beings_have_acted
            //contentText = "Every being has acted"
          }.showAndWait()


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

        case Collect(origin, tgt) =>

          if(tgt == DeathRiver)
            riverPane.update()

          if (playerGameIdx == origin.owner) {
            for(c <- res)
              new CardDialog(c, pane).showAndWait()

            handPane.update()
          }
          else
            opponentHandPane.update()


        case LookCard(_, fc, s) =>
          beingPanesMap get fc foreach (_ update s)
          if(res) riverPane.update()

        case Reveal(fc, s) =>
          println(s"reveal ($fc, $s)")
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
          println("AttackBeing " + game.beings(target.face))
          origin match {
            case CardOrigin.Being(b, s) =>
              beingPanesMap get b.face foreach (_ update s)
            case _ =>()
          }
          println("revealed = " + game.revealedCard)
          game.beings(target.face) match {
            case Formation(f) =>
              beingPanesMap get target.face foreach (_ update targetSuit)
            case b =>
              if(b.owner != playerGameIdx) {
                val (toBury, toDraw) = res
                if(toDraw > 0)
                  new DrawAndLookAction(this, origin,
                    () => MoveSeq.end(origin) foreach (actor ! _)
                  ).apply()

                println("toBury = " + toBury)
                if(toBury.size > 1) {
                  actor ! BuryRequest(target, toBury)
                  alertWaitEndOfBurial()
                }
                else {
                  actor ! Bury(target.face, toBury.toList)
                }
              }
              if(b.cards.size > 1)
                burialOnGoing = true
          }
          origin match {
            case CardOrigin.Hand(_,_) =>
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
          if (isCurrentPlayer) {
            endPhaseButton.visible = true
          }


        case ActPhase(_) =>
          beingPanesMap.values foreach {
            _.educateButton.visible = false
          }
          statusPane.phase = game.currentPhase
          statusPane.phase = game.currentPhase

        case SourcePhase =>
          if (isCurrentPlayer)
            new DrawAndLookAction(this, Origin.Star(playerGameIdx),
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
    ignore(new TwilightDialog(this, t){
      delegate.initOwner(pane.scene().getWindow)
    }.showAndWait())

  def canDragAndDropOnInfluencePhase() : Boolean =
    game.currentPhase match {
      case InfluencePhase(`playerGameIdx`) => true
      case _ => false
    }

  private [this] var burialOnGoing = false
  def alertWaitEndOfBurial() : Unit =
  ignore {
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
