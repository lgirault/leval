package leval.gui.gameScreen

/**
  * Created by lorilan on 6/22/16.
  */
import leval.core.{Ace, Being, Card, Club, Diamond, Game, Heart, InfluencePhase, King, MajestyEffect, Move, OpponentStar, RoundState, SelfStar, Spade, Star, Suit, Target}
import leval.gui.CardImg

import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.{Node, Scene}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.stage.Screen
import scalafx.scene.paint.{Color, Paint}
import scalafx.scene.shape.Rectangle



object TwoPlayerGameScene {

  def starPanel(ogame : ObservableGame, numStar : Int) =
    new HighlightableRegion(new VBox with GameObserver {

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
  })

  def handHBox(scene : TwoPlayerGameScene, numStar : Int, pane : Pane) : Pane = {
      val hand = scene.oGame.stars(numStar).hand

      val imgs = hand.tail.foldLeft(Seq(CardImg.topHalf(hand.head))){
        case (acc, c) => CardImg.cutTopHalf(c) +: acc
      }

      imgs.foreach {
        img =>
          img.handleEvent(MouseEvent.Any) {
            new CardDragAndDrop(scene, numStar, img.card, pane)
          }
      }
      new FlowPane {
        children = new HBox(imgs: _*) {
          alignmentInParent = Pos.Center

        }
      }
    }

  def beeingPane(being: Being) : BorderPane =
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


  val screenHeight = Screen.primary.visualBounds.getHeight
//  {
//    val screens = Screen.screensForRectangle(0,0,10,10)
//    screens.map(_.visualBounds.getHeight).min
//  }

  val cardHeight = screenHeight / 10
  val cardResizeRatio = CardImg.height / cardHeight
  val cardWidth = CardImg.width * cardResizeRatio

  val riverSepInfo = new RowConstraints(
    minHeight = screenHeight * 0.15,
    prefHeight = screenHeight * 0.15 ,
    maxHeight = screenHeight * 0.15)
  val playerAreaInfo = new RowConstraints(
    minHeight = screenHeight * 0.425,
    prefHeight = screenHeight * 0.425 ,
    maxHeight = screenHeight * 0.425)
}

import TwoPlayerGameScene._



class TwoPlayerGameScene(val oGame : ObservableGame, val playerGameId : Int) extends Scene {
  self =>
  import oGame._

  val activeRound = currentPlayer == playerGameId
  val opponentId = (playerGameId + 1) % 2
  def player = stars(playerGameId)
  def opponent = stars(opponentId)

  private [this] var highlightedTargets = Seq[HighlightableRegion]()
  def hightlightTargets(tgts : Seq[Target] ): Unit = {
    val highlighteds = tgts flatMap {
      case SelfStar => Seq(playerStarPanel)
      case OpponentStar => Seq(opponentStarPanel)
      case _ => Seq()
    }
    highlighteds foreach (_.activateHighlight())
    highlightedTargets = highlighteds
  }


  def unHightlightTargets(): Unit ={
    val hed = highlightedTargets
    highlightedTargets = Seq()
    hed.foreach(_.deactivateHightLight())
  }

  val opponentStarPanel = starPanel(oGame, opponentId)
  val playerStarPanel = starPanel(oGame, playerGameId)
  val deck = CardImg.back

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

  val oponentArea = new BorderPane { }

  val riverArea = new FlowPane()


  def playerArea(pane : Pane) = new BorderPane {
    val playerBeingPane = new FlowPane()
    center = playerBeingPane
    bottom = handHBox(self, playerGameId, pane)

    val createBeeingPane =
      new StackPane {
        val rect = Rectangle(100, 100, Color.White)
        rect.setStroke(Color.Green)
        rect.setArcWidth(20)
        rect.setArcHeight(20)
        children = Seq(rect, new Label("Create Beeing"))
      }

    right = createBeeingPane

    player.beings.values foreach  (b => playerBeingPane.children add beeingPane(b))

  }

  root = new BorderPane {
    pane =>
      val gameAreas: List[Node] = List(oponentArea, riverArea, playerArea(pane))

      gameAreas.zipWithIndex.foreach {
        case (area, index) => GridPane.setConstraints(area, 0, index)
      }

      val gameArea = new GridPane {
        rowConstraints.add(playerAreaInfo)
        rowConstraints.add(riverSepInfo)
        rowConstraints.add(playerAreaInfo)
        children = gameAreas
      }

      HBox.setHgrow(gameArea, Priority.Always)
      left = leftColumn
      center = gameArea

  }

}

