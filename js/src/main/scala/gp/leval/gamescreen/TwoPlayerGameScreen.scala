package gp.leval.gamescreen

import gp.leval.core.{Game,PlayerId}
import gp.pixijs.{Container, DisplayObject, Point}
import gp.leval.text.ValText
import gp.leval.gamescreen.leftcolumn.{StarPane, StatusPane}

class TwoPlayerGameScreen(width: Double, height: Double)(game: Game, thisPlayer: PlayerId)(using textures : TextureDictionary, text: ValText):

  val playerIdx: Int = game.stars.indexWhere(_.id == thisPlayer)
  val opponentIdx: Int = (playerIdx + 1) % 2
  val thisStar = game.stars(playerIdx)
  val opponentStar = game.stars(opponentIdx)

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
        val leftColumn = Container{
          column =>
            val statusPane = new StatusPane(game)
            column.addChild(statusPane.view)
            val opponentStarPane = new StarPane(opponentStar).view
            opponentStarPane.y = 100
            column.addChild(opponentStarPane)
            val back = textures.back
            back.y = 200
            column.addChild(back)
            val thisStarPane = new StarPane(thisStar).view
            thisStarPane.y = 200 + back.height
            column.addChild(thisStarPane)
        
        }
        root.addChild(leftColumn)
        val playerHandView = new PlayerHandView(game.stars.head.hand).view
        playerHandView.y = playerHandAreaY
        root.addChild(playerHandView)

    }

