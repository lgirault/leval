package leval.gui.gameScreen

/**
  * Created by lorilan on 6/22/16.
  */
import leval.core.{Being, Card, DeathRiver, Diamond, Formation, OpponentSpectrePower, OpponentStar, SelfStar, Source, Spectre, Target, TargetBeingResource}
import leval.gui.{CardImageView, CardImg}
import leval.gui.text
import scala.collection.mutable
import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Node
import scalafx.scene.control.Button
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.stage.Screen

abstract class CardDropTarget(decorated : Node)
  extends HighlightableRegion(decorated){
  def onDrop(origin : Origin) : Unit
}

class RiverPane
(control : GameScreenControl,
 fitHeight : Double) extends HBox {
  def river = control.game.deathRiver

  private def images : Seq[CardImageView] =
    if(river.isEmpty) Seq[CardImageView]()
    else river.tail.foldLeft(Seq(CardImg(river.head, fitHeight))) {
      case (acc, c) =>
        CardImg.cutLeft(c, 3, Some(fitHeight)) +: acc
    }

  def update() : Unit = {
    children.clear()
    children = images
  }

}

object TwoPlayerGamePane {

  //val screenHeight = Screen.primary.visualBounds.getHeight

  //  {
  //    val screens = Screen.screensForRectangle(0,0,10,10)
  //    screens.map(_.visualBounds.getHeight).min
  //  }


  val screenHeight = Screen.primary.visualBounds.getHeight

  val cardHeight = screenHeight / 10
  val cardResizeRatio = cardHeight / CardImg.height

  val cardWidth = CardImg.width * cardResizeRatio

  val leftColumnInfo = new ColumnConstraints {
    percentWidth = 10
  }
  val gameAreaInfo = new ColumnConstraints {
    percentWidth = 90
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

import leval.gui.gameScreen.TwoPlayerGamePane._

class TwoPlayerGamePane
( val oGame : ObservableGame,
  val playerGameId : Int,
  val controller : GameScreenControl)
  extends GridPane {
  pane =>

  import oGame._
  import controller.opponentId
  import controller.txt


  def player = stars(playerGameId)

  private [this] var highlightableRegions = Seq[CardDropTarget]()
  def highlightedTargets = highlightableRegions
  def doHightlightTargets(origin : Origin): Unit = {
    val highlighteds =
      if(createBeeingPane.isOpen) createBeeingPane.targets(origin.card)
      else {
        val highlighteds0 : Seq[CardDropTarget] =
          Target(oGame.game, origin.card) flatMap {
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
          case Origin.Hand(_) =>
            createBeeingPane.createBeingLabel +: highlighteds0
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
  opponentStarPanel.alignmentInParent = Pos.BottomCenter
  val playerStarPanel = StarPanel(oGame, playerGameId, controller)
  playerStarPanel.alignmentInParent = Pos.TopCenter

  val deck = new CardDropTarget(CardImg.back(Some(cardHeight))){
    def onDrop(origin: Origin): Unit =
      controller.drawAndLook(origin)
  }

  val riverPane = new RiverPane(controller, cardHeight)
  val riverWrapper = new CardDropTarget(riverPane){
    def onDrop(origin: Origin): Unit =
      controller.drawAndLook(origin)
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
      new CardDragAndDrop(controller,
        controller.canDragAndDropOnInfluencePhase, _)())


  val opponentHandPane = new OpponnentHandPane(controller)
  val opponentBeingsPane = new FlowPane() /*{
    style = "-fx-border-width: 1; -fx-border-color: black;"
  }*/
  val playerBeingsPane = new FlowPane() /*{
    style = "-fx-border-width: 1; -fx-border-color: black;"
  }*/

  private [gameScreen] val beingPanesMap = mutable.Map[Card, BeingPane]()

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
    leval.ignore(opponentBeingsPane.children add bp)
  }

  def addPlayerBeingPane(b : Being) : Unit = {
    val bp = new BeingPane(controller, b, cardHeight, cardWidth, Player)
    beingPanesMap += (b.face -> bp)
    playerBeingsPane.children add bp
  }

  val playerArea = new BorderPane() {
//    style = "-fx-border-width: 1; -fx-border-color: black;"
    center = playerBeingsPane
    right = createBeeingPane
  }

  padding = Insets.Empty

  val statusPane = new StatusPane()

  statusPane.star = game.stars(game.currentStarId).name

  val leftColumn = Seq(
    statusPane,
    opponentStarPanel,
    deck,
    playerStarPanel
  )

  leftColumn.zipWithIndex.foreach {
    case (area, index) =>
      GridPane.setConstraints(area, 0, index)

  }

  val gameAreas: List[Node] =
    List(opponentHandPane,
      opponentBeingsPane, riverWrapper,
      playerArea, handPaneWrapper)


  gameAreas.zipWithIndex.foreach {
    case (area, index) => GridPane.setConstraints(area, 1, index)
  }

  val areas : List[Node ] = leftColumn ++: gameAreas

  rowConstraints add handAreaInfo
  rowConstraints add playerAreaInfo
  rowConstraints add riverSepInfo
  rowConstraints add playerAreaInfo
  rowConstraints add handAreaInfo
  columnConstraints add leftColumnInfo
  columnConstraints add gameAreaInfo
  children = gameAreas

  children = areas

//  val dnd = new GridPaneDnD(this)
//  handleEvent(MouseEvent.Any)(dnd)

}

