package leval.gui.gameScreen

/**
  * Created by lorilan on 6/22/16.
  */
import leval.core.{Ace, Being, Card, Club, DeathRiver, Diamond, Game, Heart, InfluencePhase, King, MajestyEffect, Move, OpponentStar, Rank, RoundState, SelfStar, Source, Spade, Star, Suit, Target}
import leval.gui.CardImg

import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.{Node, Scene}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.stage.Screen


abstract class CardDropTarget(decorated : Node)
  extends HighlightableRegion(decorated){
  def onDrop(c : Card, origin : Origin) : Unit
}




object TwoPlayerGameScene {

  def starPanel(ogame : ObservableGame, numStar : Int) =
    new CardDropTarget(new VBox with GameObserver {

      ogame.observers += this

      def star : Star = ogame.stars(numStar)
      val majestyValueLabel = new Label(star.majesty.toString)
      spacing = 10
      alignment = Pos.Center
      children = Seq(
        new Label(star.id.name),
        new Label("Majesty"),
        majestyValueLabel

      )

      def notify[A](m: Move[A], res: A): Unit = m match {
        case MajestyEffect((value, Heart)) if ogame.currentPlayer == numStar =>
          majestyValueLabel.text = star.majesty.toString
        case _ => ()
      }
    }){
      def onDrop(c: (Rank, Suit), origin: Origin): Unit = ()
    }

  def handHBox(scene : TwoPlayerGameScene, numStar : Int, pane : Pane) : Pane = {
    val hand = scene.oGame.stars(numStar).hand

    val imgs = hand.tail.foldLeft(Seq(CardImg.topHalf(hand.head))){
      case (acc, c) =>
        val ci = CardImg.cutTopHalf(c)
        ci.alignmentInParent = Pos.BottomCenter
        ci +: acc
    }

    imgs.foreach {
      img =>
        img.handleEvent(MouseEvent.Any) {
          new CardDragAndDrop(scene, numStar, img.card, Hand)
        }
    }
    new FlowPane {
      children = new HBox(imgs: _*)
      alignmentInParent = Pos.BottomCenter
    }
  }

  def beeingPane( being: Being) : BorderPane =
    new BorderPane{

      var numRow = 1
      var numCol = 1

      var alignment = Pos.Center
      if(being.heart.nonEmpty) {
        left = new HighlightableRegion(CardImg.back(cardHeight))

        numCol += 1
      } else {
        alignment = Pos.CenterLeft
      }

      if(being.weapon.nonEmpty){
        right = CardImg.back(cardHeight)
        numCol += 1
      }

      def placeRowCard( place : Node => Unit) : Unit ={
        val img = CardImg.back(cardHeight)
        place(img)
        img.alignmentInParent = alignment
        numRow += 1
      }
      if(being.mind.nonEmpty)
        placeRowCard(top_=)

      if(being.power.nonEmpty)
        placeRowCard(bottom_=)

      center = CardImg(being.face, cardHeight)
      maxWidth = cardWidth * numCol
      maxHeight = cardHeight * numRow
    }


  //val screenHeight = Screen.primary.visualBounds.getHeight

  //  {
  //    val screens = Screen.screensForRectangle(0,0,10,10)
  //    screens.map(_.visualBounds.getHeight).min
  //  }


  val screenHeight = Screen.primary.visualBounds.getHeight
  println(screenHeight)
  //  {
  //    val screens = Screen.screensForRectangle(0,0,10,10)
  //    screens.map(_.visualBounds.getHeight).min
  //  }

  val cardHeight = screenHeight / 10
  val cardResizeRatio = cardHeight / CardImg.height

  val cardWidth = CardImg.width * cardResizeRatio

  val riverSepInfo = new RowConstraints(
    minHeight = screenHeight * 0.15,
    prefHeight = screenHeight * 0.15 ,
    maxHeight = screenHeight * 0.15)

  val playerAreaInfo = new RowConstraints(
    minHeight = screenHeight * 0.325,
    prefHeight = screenHeight * 0.325 ,
    maxHeight = screenHeight * 0.325)

  val handAreaInfo = new RowConstraints(
    minHeight = screenHeight * 0.1,
    prefHeight = screenHeight * 0.1 ,
    maxHeight = screenHeight * 0.1
  )

  //  def riverSepInfo = new RowConstraints {
  //    percentHeight = 15
  //  }
  //
  //  def playerAreaInfo = new RowConstraints {
  //    percentHeight = 27.5
  //  }
  //
  //  def handAreaInfo = new RowConstraints {
  //    percentHeight = 15
  //  }
  //
  //  def leftColumnInfo = new ColumnConstraints {
  //    percentWidth = 15
  //  }
}



import TwoPlayerGameScene._



class TwoPlayerGameScene(val oGame : ObservableGame, val playerGameId : Int) extends Scene {
  self =>
  import oGame._

  val activeRound = currentPlayer == playerGameId
  val opponentId = (playerGameId + 1) % 2
  def player = stars(playerGameId)
  def opponent = stars(opponentId)

  private [this] var highlightableRegions = Seq[CardDropTarget]()
  def highlightedTargets = highlightableRegions
  def doHightlightTargets(origin : Origin, c : Card ): Unit = {

    val highlighteds =
      if(createBeeingPane.isOpen)
        createBeeingPane.targets(c)
      else {
        val highlighteds0 : Seq[CardDropTarget] =
          Target(oGame.game, c.suit) flatMap {
            case SelfStar => Seq(playerStarPanel)
            case OpponentStar => Seq(opponentStarPanel)
            case Source => Seq(deck)
            case DeathRiver => Seq(riverArea)
            case _ => Seq()
          }
        origin match{
          case Hand => createBeeingPane.createBeingLabel +: highlighteds0
          case _ => highlighteds0
        }
      }


    highlighteds foreach (_.activateHighlight())
    highlightableRegions = highlighteds
  }


  def unHightlightTargets(): Unit ={
    val hed = highlightableRegions
    highlightableRegions = Seq()
    hed.foreach(_.deactivateHightLight())
  }

  //Hightable areas
  val opponentStarPanel = starPanel(oGame, opponentId)
  val playerStarPanel = starPanel(oGame, playerGameId)
  val deck = new CardDropTarget(CardImg.back){
    def onDrop(c: (Rank, Suit), origin: Origin): Unit = ()
  }
  val riverArea = new CardDropTarget(new FlowPane()){
    def onDrop(c: (Rank, Suit), origin: Origin): Unit = ()
  }
  val createBeeingPane = new CreateBeingPane(cardWidth, cardHeight,
    new CardDragAndDrop(self, playerGameId, _, _))


  val leftColumn = new VBox(){
    val upSpacer = new Region()
    val downSpacer = new Region()

    VBox.setVgrow(upSpacer, Priority.Always)
    VBox.setVgrow(downSpacer, Priority.Always)
    alignmentInParent = Pos.Center
    children = Seq(upSpacer,
      opponentStarPanel,
      deck,
      playerStarPanel,
      downSpacer
    )
  }

  val openentHandPane = new BorderPane { }
  val oponentArea = new BorderPane { }



  def playerArea() = new FlowPane {
    val playerBeingPane = new FlowPane()
    player.beings.values foreach  (b => playerBeingPane.children add beeingPane(b))

    createBeeingPane.alignmentInParent = Pos.CenterRight
    children = Seq( playerBeingPane, createBeeingPane)
  }

  val bpRoot = new BorderPane {
    pane =>
    padding = Insets.Empty

    val gameAreas: List[Node] =
      List(openentHandPane, oponentArea, riverArea,
        playerArea(), handHBox(self, playerGameId, pane))

    gameAreas.zipWithIndex.foreach {
      case (area, index) => GridPane.setConstraints(area, 0, index)
    }

    val gameArea = new GridPane {
      rowConstraints.add(handAreaInfo)
      rowConstraints.add(playerAreaInfo)
      rowConstraints.add(riverSepInfo)
      rowConstraints.add(playerAreaInfo)
      rowConstraints.add(handAreaInfo)
      children = gameAreas
    }

    left = leftColumn
    center = gameArea
    bottom = null
    top = null
    right = null

  }

  root = bpRoot
}

