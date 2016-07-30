package leval.gui.gameScreen

/**
  * Created by lorilan on 6/22/16.
  */
import leval.ignore
import leval.core._
import leval.gui.gameScreen.being._

import scala.collection.mutable
import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Node
import scalafx.scene.control.Button
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.stage.Screen

abstract class CardDropTarget(decorated : Node)
  extends HighlightableRegion(decorated)  {

  def onDrop(origin : CardOrigin) : Unit
}

class RiverPane
(control : GameScreenControl,
 fitHeight : Double) extends HBox {
  def river = control.game.deathRiver

  private def images : Seq[CardImageView] =
    if(river.isEmpty) Seq[CardImageView]()
    else
      river.tail.foldLeft(List(CardImg(river.head, Some(fitHeight)))) {
        case (acc, c) =>
          CardImg.cutLeft(c, 3, Some(fitHeight)) :: acc
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

  import controller.{opponentId, txt}
  import oGame._

  //style = "-fx-background : rgb(0,0,51)"

  def player = stars(playerGameId)

  def opponentSpectrePower : Iterable[BeingResourcePane] =
    beingPanes(opponentId) filter {bp =>
      val Formation(f) = bp.being
      println(bp.being.face + " : " + f)
      bp.being match {
      case Formation(Spectre) => true
      case _ => false
    }} map (_.resourcePane(Club).get)

  def targetBeingResource(s: Suit, sides : Seq[Int]) : Iterable[BeingResourcePane]  = {
    val bps = sides match {
      case Seq(id) => beingPanes(id)
      case _ => beingPanes
    }
    bps.flatMap(_.resourcePane(s))
  }


  private [this] var highlightableRegions = Seq[CardDropTarget]()
  def highlightedTargets = highlightableRegions
  def doHightlightTargets(origin : CardOrigin): Unit = {
    val highlighteds =
      if(createBeeingPane.isOpen) createBeeingPane.targets(origin.card)
      else if(educateBeingPane.isOpen) origin.card match {
        case c : C => educateBeingPane.targets(c)
        case _ => Seq()
      }
      else {
        val highlighteds0 : Seq[CardDropTarget] =
          Target(oGame.game, origin.card) flatMap {
            case SelfStar => Seq(playerStarPanel)
            case OpponentStar => Seq(opponentStarPanel)
            case Source => Seq(deck)
            case DeathRiver => Seq(riverWrapper)
            case OpponentSpectrePower =>
              opponentSpectrePower
            case TargetBeingResource(s, sides) =>
              targetBeingResource(s,sides)
            case _ => Seq()
          }
        origin match{
          case CardOrigin.Hand(_, _) =>
            createBeeingPane.createBeingLabel +: highlighteds0
          case _ => highlighteds0
        }
      }


    println(Target(oGame.game, origin.card))
    println(highlighteds)

    highlighteds foreach (_.activateHighlight())
    highlightableRegions = highlighteds
  }


  def unHightlightTargets(): Unit = {
    val hed = highlightableRegions
    highlightableRegions = Seq()
    hed.foreach(_.deactivateHightLight())
  }

  //Highlightable areas
  val opponentStarPanel = StarPanel(oGame, opponentId, controller)
  opponentStarPanel.alignmentInParent = Pos.BottomCenter
  val playerStarPanel = StarPanel(oGame, playerGameId, controller)
  playerStarPanel.alignmentInParent = Pos.TopCenter

  val deck = new CardDropTarget(CardImg.back(Some(cardHeight))){
    def onDrop(origin: CardOrigin): Unit =
      controller.drawAndLook(origin)
  }

  val riverPane = new RiverPane(controller, cardHeight)
  val riverWrapper = new CardDropTarget(riverPane){
    def onDrop(origin: CardOrigin): Unit =
      controller.drawAndLook(origin)
  }

  val handPane = new PlayerHandPane(controller)

  val endPhaseButton =
    new Button(txt.do_end_act_phase){
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
    new CreateBeingPane(controller,
      handPane,
      cardWidth, cardHeight)

  val educateBeingPane =
    new EducateBeingPane(controller,
      handPane,
      cardWidth, cardHeight)

  val opponentHandPane = new OpponnentHandPane(controller)
  //style = "-fx-border-width: 1; -fx-border-color: black;"
  val opponentBeingsPane = new FlowPane()

  val playerBeingsPane = new FlowPane()
  def beingsPane(o : Orientation) = o match {
    case Player => playerBeingsPane
    case Opponent => opponentBeingsPane
  }

  private [gameScreen] val beingPanesMap = mutable.Map[Card, BeingPane]()

  controller.game beingsOwnBy controller.opponentId foreach addOpponentBeingPane
  controller.game beingsOwnBy controller.playerGameIdx foreach addPlayerBeingPane


  def beingPanes : Iterable[BeingPane] = beingPanesMap.values

  def resourcesPanes : Iterable[BeingResourcePane] =
    beingPanes flatMap (_.resourcePanes)

  def beingsPane(playerId : Int) : Pane =
    if(playerGameId == playerId) playerBeingsPane
    else opponentBeingsPane

  def beingPanes(sideId : Int) : Iterable[BeingPane] =
    beingPanesMap.values filter (_.being.owner == sideId)

  def addOpponentBeingPane(b : Being) : Unit = {
    val bp = new BeingPane(controller, b, cardWidth, cardHeight, Opponent)
    beingPanesMap += (b.face -> bp)
    leval.ignore(opponentBeingsPane.children add bp)
  }

  def addPlayerBeingPane(b : Being) : Unit = {
    val bp = new BeingPane(controller, b, cardWidth, cardHeight, Player)
    beingPanesMap += (b.face -> bp)
    ignore(playerBeingsPane.children add bp)
  }

  val playerArea = new BorderPane() {
//    style = "-fx-border-width: 1; -fx-border-color: black;"
    center = playerBeingsPane
    right = createBeeingPane
  }

  padding = Insets.Empty

  val statusPane = new StatusPane()

  statusPane.star = game.stars(game.currentStarIdx).name

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


  rowConstraints add handAreaInfo
  rowConstraints add playerAreaInfo
  rowConstraints add riverSepInfo
  rowConstraints add playerAreaInfo
  rowConstraints add handAreaInfo
  columnConstraints add leftColumnInfo
  columnConstraints add gameAreaInfo

  children = leftColumn ++: gameAreas


}

