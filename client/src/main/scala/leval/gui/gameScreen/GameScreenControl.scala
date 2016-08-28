package leval.gui.gameScreen

import javafx.beans.value.ObservableValue

import akka.actor.ActorRef
import leval.ignore
import leval.core.Game.StarIdx
import leval.core._
import leval.gui.{SceneSizeChangeListener, text}
import leval.network.client.StartScreen

import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.layout._


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
(val scene : Scene,
 val game : ObservableGame,
 val playerGameIdx : StarIdx,
 val actor : ActorRef)
  extends GameObserver
    with SceneSizeChangeListener{

  val widthRatio: Double = 16d
  val heightRatio: Double = 9d


  implicit val txt = text.Fr
  val opponentId = (playerGameIdx + 1) % 2

  def isCurrentPlayer =
    game.currentStarId == playerGameIdx

  private [this] var pane0 : TwoPlayerGamePane = _
  val rootPane = new StackPane() {
    style = "-fx-background-color: midnightblue"
  }
  private [this] var topPadding0 : Double = _
  private [this] var leftPadding0 : Double = _
  def topPadding : Double = topPadding0
  def leftPadding : Double = leftPadding0
  def setPane(): Unit = {
    val (w,h) = contentPaneDimention()

    topPadding0 = (scene.height() - h) /2
    leftPadding0 = (scene.width() - w) /2

    pane0 = new TwoPlayerGamePane(game, playerGameIdx, this, w, h)
    pane0.alignmentInParent = Pos.Center
    rootPane.children.clear()
    leval.ignore(rootPane.children.add(pane0))
  }
  setPane()
  def pane : TwoPlayerGamePane = pane0

  override def changed(observableValue: ObservableValue[_ <: Number],
                       oldValue: Number, newValue: Number) : Unit =
    setPane()

  scene.root = rootPane
  scene.widthProperty.addListener(this)
  scene.heightProperty.addListener(this)


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

  def collect(origin : Origin,
              target : CollectTarget,
              remainingDrawAction : Int ) : Unit = {
    //remainingDrawAction BEFORE this collect
    val n = game.rules.numCardDrawPerAction(origin, target, remainingDrawAction)
    for(_ <- 0 until n) {
      actor ! Collect(origin, target)
    }
  }


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

  import game._

  def burry(br : BuryRequest) : Unit =
    new BurialDialog(br,
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

          endPhase()
        }
      case _ => leval.error()
    }

  def updateStarPanels() = {
    pane.playerStarPanel.update()
    pane.opponentStarPanel.update()
  }

  def notify[A](m: Move[A], res: A): Unit = {
    println(game.stars(playerGameIdx).name +"'s controller notified of " + m)

    if(game.ended) endGame()
    else
      m match {
        case MajestyEffect(_, _) => updateStarPanels()

        case PlaceBeing(b, side) =>
          if (playerGameIdx == side) {
            pane.addPlayerBeingPane(b)
            pane.createBeeingPane.menuMode()
          } else {
            pane.addOpponentBeingPane(b)
            pane.opponentHandPane.update()
          }

        case RemoveFromHand(_) =>
          println(game.deathRiver)
          pane.riverPane.update()
          pane.handPane.update()
          pane.opponentHandPane.update()

        case Collect(origin, tgt) =>

          if(tgt == DeathRiver)
            pane.riverPane.update()

          if (playerGameIdx == origin.owner) {
            new CardDialog(res, pane).showAndWait()

            pane.handPane.update()
          }
          else
            pane.opponentHandPane.update()


        case LookCard(_, fc, s) =>
          pane.beingPanesMap get fc foreach (_ update s)
          if(res) pane.riverPane.update()

        case Reveal(fc, s) =>
          println(s"reveal ($fc, $s)")
          pane.beingPanesMap get fc foreach (_ update s)
          if(res) pane.riverPane.update()

        case Bury(target, _) =>
          burialOnGoing = false
          pane.beingPanesMap get target foreach {
            bp =>
              pane.beingsPane(bp.orientation).children.remove(bp.delegate)
          }
          pane.beingPanesMap -= target
          pane.riverPane.update()
          checkEveryBeingHasActedAndEndPhase()

        case ActivateBeing(fc) =>
          if(!burialOnGoing)
            checkEveryBeingHasActedAndEndPhase()

        case AttackBeing(origin, target, targetSuit) =>
          origin match {
            case CardOrigin.Being(b, s) =>
              pane.beingPanesMap get b.face foreach (_ update s)
            case _ =>()
          }
          pane.beingPanesMap get target.face foreach (_ update targetSuit)
          game.beings(target.face) match {
            case Formation(f) => ()
            case b =>
              pane.riverPane.update()
              if(b.owner != playerGameIdx) {
                val (toBury, toDraw) = res
                if(toDraw > 0)
                  new DrawAndLookAction(this, origin,
                    () => MoveSeq.end(origin) foreach (actor ! _)
                  ).apply()

                println(s"toBury = $toBury")
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

              updateStarPanels()
          }
          origin match {
            case CardOrigin.Hand(_,_) =>
              pane.riverPane.update()
              pane.handPane.update()
              pane.opponentHandPane.update()
            case _ => ()
          }



        case InfluencePhase(newPlayer) =>
          if(isCurrentPlayer)
            pane.beingPanesMap.values foreach { bp =>
              if(pane.playerBeingsPane.children contains bp)
                bp.educateButton.visible = true
            }

          pane.statusPane.update()
          if (isCurrentPlayer) {
            pane.endPhaseButton.visible = true
          }


        case ActPhase(_) =>
          pane.beingPanesMap.values foreach {
            _.educateButton.visible = false
          }
          pane.statusPane.update()

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

          pane.beingPanesMap.values foreach (_.update())
          pane.endPhaseButton.visible = false

          pane.statusPane.update()

        case e : Educate =>
          println("Educate update pane !")
          val bp = pane.beingPanesMap(e.target)
          val b = game.beings(e.target)
          bp.update(b)
          if(isCurrentPlayer)
            pane.handPane.update()
          else
            pane.opponentHandPane.update()

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
