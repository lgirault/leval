package gp.leval.gamescreen

import gp.leval.core.Game
import gp.pixijs.{Container, DisplayObject, Point}
import gp.leval.text.ValText

class GameScreen(width: Double, height: Double)(game: Game)(using textures : TextureDictionary, text: ValText):

  val cardHeight = (height / 10).floor
  
  
  // val cardResizeRatio = cardHeight / CardImg.height

  // val cardWidth = CardImg.width * cardResizeRatio

  val riverAreaHeight = cardHeight

  val handAreaHeight = cardHeight //((height - riverAreaHeight - (2 * playerAreaHeight))/2).floor

  val playerAreaHeight = ((height - riverAreaHeight - 2 * handAreaHeight) / 2).floor
  //(0.325 * height).ceil


  val opponentHandAreaY = 0
  val opponentBeingsAreaY = opponentHandAreaY + cardHeight
  val riverAreaY = opponentBeingsAreaY + playerAreaHeight
  val playerBeingsAreaY = riverAreaY + riverAreaHeight 
  val playerHandAreaY = playerBeingsAreaY + playerAreaHeight
  //= ~ 0.15 * height


  // val leftColumnInfo = new ColumnConstraints(0.1 * width)
  // val gameAreaInfo = new ColumnConstraints(0.9 * width)

  def view : DisplayObject = 
    Container {
      root =>
        root.width = width
        root.height = height
        val statusPane = new StatusPane(game)
        root.addChild(statusPane.view)
        val playerHandView = new PlayerHandView(game.stars.head.hand).view
        playerHandView.y = playerHandAreaY
        root.addChild(playerHandView)

    }

