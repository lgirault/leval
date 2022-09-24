// package gp.leval.gamescreen

// import gp.leval.core.Game.StarIdx
// import gp.leval.core.*
// import gp.leval.text.ValText
// import scala.compiletime.uninitialized


// /**
//   * Created by Loïc Girault on 06/07/16.
//   */

// object MoveSeq {

//   def placeBeing(b: Being, side : Int): Seq[Move[?]] = {
//     val s = Seq(PlaceBeing(b, side), Phase.Act(Set()))
//     b match {
//       case Formation(Spectre) => MajestyEffect(-5, side) +: s
//       case _ => s
//     }
//   }

//   def end(origin: CardOrigin) : Seq[Move[?]] = origin match {
//     case CardOrigin.Hand(_,Joker(_)) =>  Seq()
//     case CardOrigin.Hand(_,c) =>
//       Seq(RemoveFromHand(c), Phase.Act(Set()))
//     case CardOrigin.Being(b, s) => Seq(ActivateBeing(b.face))
//   }

// }

// object GameScreenControl:
//   trait Config :
//     def lang: ValText
  

// trait ActorRef {
//   def !(msg: Any) = ???
// }


// class GameScreenControl
// (//val scene : Scene,
//  val game : ObservableGame,
//  val playerGameIdx : StarIdx,
//  cfg : GameScreenControl.Config)
//   extends GameObserver {

//   val actor : ActorRef = ???

// //  val widthRatio: Double = 16d
// //  val heightRatio: Double = 9d

//   //import leval.LevalConfig._

//   //val (widthRatio, heightRatio) = cfg.screenRatio()

//   given texts: ValText = cfg.lang

//   val opponentId = (playerGameIdx + 1) % 2

//   def isCurrentPlayer =
//     game.currentStarId == playerGameIdx

//   private var pane0 : TwoPlayerGameScreen = uninitialized
//   // val rootPane = new StackPane() {
//   //   style = "-fx-background-color: midnightblue"
//   // }
//   // private var topPadding0 : Double = _
//   // private var leftPadding0 : Double = _
//   // def topPadding : Double = topPadding0
//   // def leftPadding : Double = leftPadding0
//   // def setPane(): Unit = {
//   //   val (w,h) = contentPaneDimention()

//   //   topPadding0 = (scene.height() - h) /2
//   //   leftPadding0 = (scene.width() - w) /2

//   //   pane0 = new TwoPlayerGamePane(game, playerGameIdx, this, w, h)
//   //   rootPane.children.clear()
//   //   leval.ignore(rootPane.children.add(pane0))
//   // }
//   // setPane()
//   def pane : TwoPlayerGameScreen = pane0

//   // override def changed(observableValue: ObservableValue[_ <: Number],
//   //                      oldValue: Number, newValue: Number) : Unit =
//   //   setPane()

//   // scene.root = rootPane
//   // scene.widthProperty.addListener(this)
//   // scene.heightProperty.addListener(this)


//   game.observers += this

//   def numLookedCards : Int =
//     game.lookedCards.size

//   def numResourcesCardinal =
//     game.beings.values.map(_.cards.size).sum

//   def forbiddenOnFirstRound(origin: CardOrigin) : Boolean =
//     origin match {
//       case CardOrigin.Hand(_, Card(_, Suit.Diamond | Suit.Spade))
//            | CardOrigin.Hand(_, Joker(_))
//            | CardOrigin.Being(_, Suit.Diamond | Suit.Spade) => true
//       case _ => false
//     }

//   def cannotAttackAlert() : Unit = ???
//     // ignore(new Alert(AlertType.Information){
//     //   delegate.initOwner(pane.scene().getWindow)
//     //   title = texts.forbidden
//     //   headerText = texts.cannot_attack_on_first_round
//     // }.showAndWait())

//   def directEffect(origin: CardOrigin) : Unit =
//     if game.currentRound == 1 && forbiddenOnFirstRound(origin) then
//       cannotAttackAlert()
//     else {
//       def effect(v : Int, playedSuit : Suit) =
//         playedSuit match {
//           case Suit.Heart => MajestyEffect(v, playerGameIdx)
//           case Suit.Diamond | Suit.Spade => MajestyEffect(-1 * v, opponentId)
//           case _ => ???//leval.error()
//         }

//       origin match {
//         case CardOrigin.Hand(_, j : Card.J) =>
//           jokerEffectFromHand(j)
//         case CardOrigin.Hand(_, c @ Card(_, suit)) =>
//           actor ! effect(Card.value(c), suit)
//         case CardOrigin.Being(b, s)=>
//           actor ! Reveal(b.face, s)
//           actor ! effect(b.value(s, Card.value).get, s)
//       }

//       MoveSeq.end(origin) foreach (actor ! _)
//     }

//   def educate(e : Educate) : Unit = {
//     actor ! e
//     actor ! Phase.Act(Set())
//   }

//   def placeBeing(b: Being): Unit = {
//     MoveSeq.placeBeing(b, playerGameIdx) foreach (actor ! _)
//   }

//   def collect(origin : Origin,
//               target : CollectTarget,
//               remainingDrawAction : Int ) : Unit = {
//     //remainingDrawAction BEFORE this collect
//     val n = game.rules.numCardDrawPerAction(origin, target, remainingDrawAction)
//     for(_ <- 0 until n) {
//       actor ! Collect(origin, target)
//     }
//   }


//   def endPhase() : Unit =
//     actor ! game.nextPhase


//   def canCollectFromRiver = game.beings.values exists {
//     case b @ Formation(Spectre) if b.owner == game.currentStarId =>
//       game.deathRiver.nonEmpty
//     case _ => false
//   }

//   def drawAndLook(origin: CardOrigin) : Unit = {

//     def doDrawAndLook() = ???
//       // ignore(
//       // new DrawAndLookAction(this, origin,
//       //   () => MoveSeq.end(origin) foreach (actor ! _)
//       // ).apply())

//     origin match {
//       case CardOrigin.Being(b, s) =>
//         actor !  Reveal(b.face, s)
//         doDrawAndLook()
//       case CardOrigin.Hand(_, j : Card.J) =>
//         jokerEffectFromHand(j)
//         ()
//       case _ => doDrawAndLook()
//     }
//   }



//   def jokerEffectFromHand(joker : Card.J) : Seq[Move[?]] ={
//     // joker.color match {
//     //   case Joker.Red =>
//     //     actor ! MajestyEffect(1, playerGameIdx) // heart effect
//     //     new Alert(AlertType.Information){
//     //       delegate.initOwner(pane.scene().getWindow)
//     //       title = texts.mind
//     //       headerText = texts.select_to_attack
//     //     }.showAndWait()
//     //     new JokerMindEffectTargetSelector(this, joker)
//     //   case Joker.Black =>
//     //     new BlackJokerEffect(this, joker)
//     // }
//     Seq()
//   }

//   //(num draw, num look)



//   def playOnBeing(origin : CardOrigin,
//                   target : Being,
//                   targetSuit : Suit) = {
//     //targetSuit needed if club played from hand
//     //heart are not played on being
//     val moves : Seq[Move[?]] = origin match {
//       case CardOrigin.Hand(_, j : Card.J) =>
//         jokerEffectFromHand(j)

//       case CardOrigin.Hand(_, c @ Card(_, Suit.Diamond | Suit.Spade)) =>
//         Seq(AttackBeing(origin,target, targetSuit),
//           Phase.Act(Set()))

//       case CardOrigin.Being(b, s @ (Suit.Diamond | Suit.Spade)) =>
//         Seq(Reveal(b.face, s),
//           AttackBeing(origin, target, targetSuit),
//           ActivateBeing(b.face))

//       case CardOrigin.Hand(_,  Card(_, Suit.Club) ) |
//            CardOrigin.Being(_, Suit.Club) =>
//         drawAndLook(origin)
//         Seq()
//       case _ => ??? //leval.error()
//     }

//     moves foreach (actor ! _)
//   }

//   import game.*

//   def burry(br : BuryRequest) : Unit = ???
//     // new BurialDialog(br,
//     //   CardImg.width,
//     //   CardImg.height,
//     //   pane).showAndWait() match {
//     //   case Some(move) => actor ! move
//     //   case None => leval.error()
//     // }

//   def endGame(): Unit = { ???
//     // game.observers.remove(game.observers.indexOf(this))
//     // new Alert(AlertType.Information){
//     //   delegate.initOwner(pane.scene().getWindow)
//     //   title = texts.game_over
//     //   headerText = game.result match {
//     //     case None => texts.both_lose
//     //     case Some((winner, loser)) =>
//     //       winner.name + texts.wins
//     //   }
//     //   //contentText = "Every being has acted"
//     // }.showAndWait()
//     // actor ! StartScreen
//   }

//   def disconnectedPlayerAlert(name : String) : Unit = { ???
//     // new Alert(AlertType.Information) {
//     //   delegate.initOwner(pane.scene().getWindow)
//     //   title = texts.game_over
//     //   headerText = texts.disconnected(name)
//     // }.showAndWait()
//     // actor ! StartScreen
//   }

//   def checkEveryBeingHasActedAndEndPhase() =
//     game.currentPhase match {
//       case Phase.Act(activatedBeings) =>
//         if activatedBeings.size == game.beingsOwnBy(currentStarId).size &&
//           isCurrentPlayer then {
//           new Alert(AlertType.Information) {
//             delegate.initOwner(pane.scene().getWindow)
//             title = texts.end_of_act_phase
//             headerText = texts.every_beings_have_acted
//             //contentText = "Every being has acted"
//           }.showAndWait()

//           endPhase()
//         }
//       case _ => ??? //leval.error()
//     }

//   def updateStarPanels() = {
//     pane.playerStarPanel.update()
//     pane.opponentStarPanel.update()
//   }

//   def notify[A](m: Move[A], res: A): Unit = {
//     println(game.stars(playerGameIdx).name +"'s controller notified of " + m)

//     if game.ended then endGame()
//     else
//       m match {
//         case MajestyEffect(_, _) => updateStarPanels()

//         case PlaceBeing(b, side) =>
//           if playerGameIdx == side then 
//             pane.addPlayerBeingPane(b)
//             pane.createBeeingPane.menuMode()
//           else 
//             pane.addOpponentBeingPane(b)
//             pane.opponentHandPane.update()
          

//           res match {
//             case None => ()
//             case Some(darkLady) =>
//               burialOnGoing = true
//               updateStarPanels()
//               val b = beings(darkLady)
//               if b.owner == playerGameIdx then
//                 burry(BuryRequest(b, b.cards.toSet))
//               else
//                 alertWaitEndOfBurial()
//           }
//         case RemoveFromHand(_) =>
//           pane.riverPane.update()
//           pane.handPane.update()
//           pane.opponentHandPane.update()

//         case Collect(origin, tgt) =>

//           if tgt == DeathRiver then
//             pane.riverPane.update()

//           if playerGameIdx == origin.owner then
//             new CardDialog(res, pane).showAndWait()
//             pane.handPane.update()
//           else
//             pane.opponentHandPane.update()


//         case LookCard(_, fc, s) =>
//           pane.beingPanesMap get fc foreach (_.update())
//           if res then pane.riverPane.update()

//         case Reveal(fc, s) =>
//           pane.beingPanesMap get fc foreach (_.update())
//           if res then pane.riverPane.update()

//         case Bury(target, _) =>
//           burialOnGoing = false
//           pane.beingPanesMap get target foreach {
//             bp =>
//               pane.beingsPane(bp.orientation).children.remove(bp.delegate)
//           }
//           pane.beingPanesMap -= target
//           pane.riverPane.update()
//           checkEveryBeingHasActedAndEndPhase()

//         case ActivateBeing(fc) =>
//           if !burialOnGoing then
//             checkEveryBeingHasActedAndEndPhase()

//         case AttackBeing(origin, target, targetSuit) =>
//           origin match {
//             case CardOrigin.Being(b, s) =>
//               pane.beingPanesMap get b.face foreach (_ update s)
//               pane.riverPane.update()
//             case _ =>()
//           }
//           pane.beingPanesMap get target.face foreach (_.update())
//           val (toBury, toDraw) = res
//           val b = game.beings(target.face)

//           def checkAndDoDraw() =
//             if b.owner != playerGameIdx && toDraw > 0 then
//               new DrawAndLookAction(this, origin, () => ()).apply()

//           def checkAndDoBury() = {
//             if b.owner != playerGameIdx then {
//               if(toBury.size > 1) {
//                 actor ! BuryRequest(target, toBury)
//                 alertWaitEndOfBurial()
//               }
//               else {
//                 actor ! Bury(target.face, toBury.toList)
//               }
//             }

//             burialOnGoing = true
//           }

//           b match {
//             case Formation(f) => checkAndDoDraw()
//             case _ =>
//               pane.riverPane.update()
//               checkAndDoDraw()
//               checkAndDoBury()
//               updateStarPanels()
//           }
//           origin match {
//             case CardOrigin.Hand(_,_) =>
//               pane.riverPane.update()
//               pane.handPane.update()
//               pane.opponentHandPane.update()
//             case _ => ()
//           }



//         case Phase.Influence(newPlayer) =>
//           if(isCurrentPlayer)
//             pane.beingPanesMap.values foreach { bp =>
//               if(pane.playerBeingsPane.children contains bp)
//                 bp.educateButton.visible = true
//             }

//           pane.statusPane.update()
//           if isCurrentPlayer then {
//             pane.endPhaseButton.visible = true
//           }


//         case Phase.Act(_) =>
//           pane.beingPanesMap.values foreach {
//             _.educateButton.visible = false
//           }
//           pane.statusPane.update()

//         case Phase.Source =>
//           if isCurrentPlayer then
//             new DrawAndLookAction(this, Origin.Star(playerGameIdx),
//               () => actor ! game.nextPhase
//             ).apply()
//           else
//             new Alert(AlertType.Information) {
//               delegate.initOwner(pane.scene().getWindow)
//               title = texts.end_of_act_phase
//               headerText = texts.end_of_act_phase
//               //contentText = "Every being has acted"
//             }.showAndWait()

//           pane.beingPanesMap.values foreach (_.update())
//           pane.endPhaseButton.visible = false

//           pane.statusPane.update()

//         case e : Educate =>
//           pane.beingPanesMap(e.target).update()
//           if(isCurrentPlayer)
//             pane.handPane.update()
//           else
//             pane.opponentHandPane.update()

//         case _ => ()
//       }
//   }




//   def showTwilight(t : Twilight) : Unit = ???
//   //   {
//   //   new TwilightDialog(this, t) {
//   //     delegate.initOwner(pane.scene().getWindow)
//   //   }.showAndWait()
//   //   notify(game.currentPhase, ())
//   //   pane.handPane.update()
//   //   pane.opponentHandPane.update()
//   // }

//   def canDragAndDropOnInfluencePhase() : Boolean =
//     game.currentPhase match {
//       case Phase.Influence(`playerGameIdx`) => true
//       case _ => false
//     }

//   private var burialOnGoing = false
//   def alertWaitEndOfBurial() : Unit = ???
//     // ignore {
//     //   new Alert(AlertType.Information) {
//     //     delegate.initOwner(pane.scene().getWindow)
//     //     title = texts.burying
//     //     headerText = texts.wait_end_burial
//     //   }.showAndWait()
//     // }


//   def canDragAndDropOnActPhase(fc : Card)() : Boolean =
//     game.currentStarId == playerGameIdx &&
//       !burialOnGoing && (game.currentPhase match {
//       case Phase.Act(activatedBeings)
//         if ! (activatedBeings.contains(fc)) => true
//       case _ => false
//     })


// }
