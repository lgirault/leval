package leval.gui.gameScreen

/**
  * Created by lorilan on 6/22/16.
  */
import leval.core.{Being, Card, DeathRiver, Diamond, FaceCard, Formation, OpponentSpectrePower, OpponentStar, SelfStar, Source, Spectre, Target, TargetBeingResource}
import leval.gui.{CardImageView, CardImg}

import scala.collection.mutable
import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Button
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.{Node, Scene}
import scalafx.stage.Screen

abstract class CardDropTarget(decorated : Node)
  extends HighlightableRegion(decorated){
  def onDrop(c : Card, origin : Origin) : Unit
}

class RiverPane
(control : GameScreenControl,
 fitHeight : Double) extends HBox {
  def river = control.oGame.deathRiver

  private def images : Seq[CardImageView] =
      if(river.isEmpty) Seq[CardImageView]()
      else river.tail.foldLeft(Seq(CardImg(river.head, fitHeight))) {
        case (acc, c) =>
          CardImg.cut(c, fitHeight, 3) +: acc
      }

  def update() : Unit = {
     children.clear()
     children = images
  }

}

object TwoPlayerGameScene {

  //val screenHeight = Screen.primary.visualBounds.getHeight

  //  {
  //    val screens = Screen.screensForRectangle(0,0,10,10)
  //    screens.map(_.visualBounds.getHeight).min
  //  }


  val screenHeight = Screen.primary.visualBounds.getHeight
  //  {
  //    val screens = Screen.screensForRectangle(0,0,10,10)
  //    screens.map(_.visualBounds.getHeight).min
  //  }

  val cardHeight = screenHeight / 10
  val cardResizeRatio = cardHeight / CardImg.height

  val cardWidth = CardImg.width * cardResizeRatio

//  val riverSepInfo = new RowConstraints(
//    minHeight = screenHeight * 0.15,
//    prefHeight = screenHeight * 0.15 ,
//    maxHeight = screenHeight * 0.15)
//
//  val playerAreaInfo = new RowConstraints(
//    minHeight = screenHeight * 0.325,
//    prefHeight = screenHeight * 0.325 ,
//    maxHeight = screenHeight * 0.325)
//
//  val handAreaInfo = new RowConstraints(
//    minHeight = screenHeight * 0.1,
//    prefHeight = screenHeight * 0.1 ,
//    maxHeight = screenHeight * 0.1
//  )

  val gameAreaColumn = new ColumnConstraints {
    percentWidth = 100
  }
  val riverSepInfo = new RowConstraints {
    percentHeight = 15
  }
  val playerAreaInfo = new RowConstraints {
    percentHeight = 32.5
  }
  val handAreaInfo = new RowConstraints {
    percentHeight = 10
  }

}

import leval.gui.gameScreen.TwoPlayerGameScene._

class TwoPlayerGameScene
( val oGame : ObservableGame,
  val playerGameId : Int,
  val controller : GameScreenControl)
  extends Scene {
  self =>
  import oGame._

  val opponentId = (playerGameId + 1) % 2
  def player = stars(playerGameId)

  private [this] var highlightableRegions = Seq[CardDropTarget]()
  def highlightedTargets = highlightableRegions
  def doHightlightTargets(origin : Origin, c : Card ): Unit = {

    val highlighteds =
      if(createBeeingPane.isOpen) createBeeingPane.targets(c)
      else {
        val highlighteds0 : Seq[CardDropTarget] =
          Target(oGame.game, c.suit) flatMap {
            case SelfStar => Seq(playerStarPanel)
            case OpponentStar => Seq(opponentStarPanel)
            case Source => Seq(deck)
            case DeathRiver => Seq(riverWrapper)
            case OpponentSpectrePower =>

              beingPanes(opponentId) filter (bp => bp.being match {
                case Formation(Spectre) => true
                case _ => false
              }) map (_.resourcePane(Diamond).get)

            case TargetBeingResource(s, sides) =>
              val bps = sides match {
                case Seq(id) => beingPanes(id)
                case _ => beingPanes
              }
              bps.flatMap(_.resourcePane(s))
            case _ => Seq()
          }
        origin match{
          case Origin.Hand => createBeeingPane.createBeingLabel +: highlighteds0
          case _ => highlighteds0
        }
      }


    highlighteds foreach (_.activateHighlight())
    highlightableRegions = highlighteds
  }


  def unHightlightTargets(): Unit = {
    val hed = highlightableRegions
    highlightableRegions = Seq()
    hed.foreach(_.deactivateHightLight())
  }




  //Hightable areas
  val opponentStarPanel = StarPanel(oGame, opponentId, controller)
  val playerStarPanel = StarPanel(oGame, playerGameId, controller)

  val deck = new CardDropTarget(CardImg.back){
    def onDrop(c: Card, origin: Origin): Unit =
      controller.drawAndLook(c, origin)
  }

  val riverPane = new RiverPane(controller, cardHeight)
  val riverWrapper = new CardDropTarget(riverPane){
    def onDrop(c: Card, origin: Origin): Unit =
      controller.drawAndLook(c, origin)
  }

  val handPane = new PlayerHandPane(controller)

  val endPhaseButton =
    new Button("EndPhase"){
      onMouseClicked = {
        me : MouseEvent =>
          controller.endPhase()
      }
      visible = false
    }
  val handPaneWrapper = new BorderPane {
    center = handPane
    right = endPhaseButton
  }
  val createBeeingPane =
    new CreateBeingPane(controller, handPane,
      cardWidth, cardHeight,
      new CardDragAndDrop(controller, controller.canDragAndDropOnInfluencePhase, _, _)())


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

  val opponentHandPane = new BorderPane { }
  val opponentBeingsPane = new BorderPane { }
  val playerBeingsPane = new FlowPane() {
    style = "-fx-border-width: 1; -fx-border-color: black;"
  }

  private [gameScreen] val beingPanesMap = mutable.Map[FaceCard, BeingPane]()

  def beingPanes : Iterable[BeingPane] = beingPanesMap.values

  def beingsPane(playerId : Int) : Pane =
    if(playerGameId == playerId) playerBeingsPane
    else opponentBeingsPane

  def beingPanes(playerId : Int) : Iterable[BeingPane] = {
    val parent = beingsPane(playerId).delegate
    beingPanes filter (_.parent == parent)
  }

  def addOpponentBeingPane(b : Being) : Unit = {
    val bp = new BeingPane(controller, b, cardHeight, cardWidth, Opponent)
    beingPanesMap += (b.face -> bp)
    playerBeingsPane.children add bp
  }

  def addPlayerBeingPane(b : Being) : Unit = {
    val bp = new BeingPane(controller, b, cardHeight, cardWidth, Player)
    beingPanesMap += (b.face -> bp)
    playerBeingsPane.children add bp
  }

  def playerArea() = new BorderPane() {
    style = "-fx-border-width: 1; -fx-border-color: black;"

    player.beings.values foreach addPlayerBeingPane

    //createBeeingPane.alignmentInParent = Pos.CenterRight
    //children = Seq( playerBeingPane, createBeeingPane)
    center = playerBeingsPane
    right = createBeeingPane
  }

  val bpRoot = new BorderPane {
    pane =>
    padding = Insets.Empty

    val gameAreas: List[Node] =
      List(opponentHandPane,
        opponentBeingsPane, riverWrapper,
        playerArea(), handPaneWrapper)

    gameAreas.zipWithIndex.foreach {
      case (area, index) => GridPane.setConstraints(area, 0, index)
    }

    val gameArea = new GridPane {
      rowConstraints add handAreaInfo
      rowConstraints add playerAreaInfo
      rowConstraints add riverSepInfo
      rowConstraints add playerAreaInfo
      rowConstraints add handAreaInfo
      columnConstraints add gameAreaColumn
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

