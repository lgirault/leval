package leval.gui.gameScreen

/**
  * Created by lorilan on 6/22/16.
  */
import leval.core.{Ace, Being, Card, Club, Game, Heart, InfluencePhase, King, PlayerId, RoundState, Star}
import leval.gui.CardImg
import leval.ignore

import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.{Node, Scene}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.stage.Screen
import scalafx.scene.paint.{Color, Paint}
import scalafx.scene.shape.Rectangle


object GameScene {

  def starPanel(star : Star) = new VBox {
    val majestyValueLabel = new Label(star.majesty.toString)
    spacing = 10
    alignment = Pos.Center
    children = Seq(
      new Label(star.id.name),
      new Label("Majesty"),
      majestyValueLabel

    )
  }

  def handHBox(hand : Seq[Card], pane : Pane) : Pane =
    if(hand.isEmpty) new HBox()
    else {
      val imgs = hand.tail.foldLeft(Seq(CardImg.topHalf(hand.head))){
        case (acc, c) => CardImg.cutTopHalf(c) +: acc
      }

      imgs.foreach {
        img =>
          img.handleEvent(MouseEvent.Any) {
            new CardDragAndDrop(img.card, pane)
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
        left = new HighlightRegion(CardImg.back(cardHeight))

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

      center = CardImg(being.head, cardHeight)
      maxWidth = cardWidth * numCol
      maxHeight = cardHeight * numRow
    }


  val screenHeight = Screen.primary.visualBounds.getHeight

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

import GameScene._



class GameScene(val game : Game) extends Scene {

  var roundSate : RoundState = InfluencePhase

  val h1 = game.star1.hand

  val deck = CardImg.back

  val leftColumn = new VBox(){
    val upSpacer = new Region()
    val downSpacer = new Region()

    VBox.setVgrow(upSpacer, Priority.Always)
    VBox.setVgrow(downSpacer, Priority.Always)
    alignmentInParent = Pos.Center
    children = Seq(upSpacer,
      starPanel(game.star2),
      deck,
      starPanel(game.star1),
      downSpacer
    )
  }

  val oponentArea = new BorderPane { }

  val riverArea = new FlowPane()


  def playerArea(pane : Pane) = new BorderPane {
    val playerBeingPane = new FlowPane()
    center = playerBeingPane
    bottom = handHBox(h1, pane)

    val createBeeingPane =
      new StackPane {
        val rect = Rectangle(100, 100, Color.White)
        rect.setStroke(Color.Green)
        rect.setArcWidth(20)
        rect.setArcHeight(20)
        children = Seq(rect, new Label("Create Beeing"))
      }

    right = createBeeingPane

    playerBeingPane.children add beeingPane(Being((King, Heart),
      heart = Some((Ace, Club)),
      weapon = Some((Ace, Club)), /*None*/
      mind = Some((Ace, Club)),
      power = None/*Some((Ace, Club))*/))
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

