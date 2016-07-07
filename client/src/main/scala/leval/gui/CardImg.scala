package leval.gui

/**
  * Created by lorilan on 6/21/16.
  */

import leval.core.{Joker, _}

import scalafx.geometry.Rectangle2D
import scalafx.scene.image.{Image, ImageView}

class CardImageView(val card : Card, img : Image) extends ImageView(img)
object CardImg {

  val url = this.getClass.getResource("/svg-z-cards.png").toExternalForm
  val img = new Image(url)
  val width : Double = 167.552 //167.552307692
  val height : Double = 243.238

  val backCoord : (Double, Double) = (width * 2, height * 4)

  def line(suit : Suit) : Int = suit match {
    case Club => 0
    case Diamond => 1
    case Heart => 2
    case Spade => 3
  }
  def column(r : Rank) : Int = r match {
    case Ace => 0
    case Numeric(i) => i - 1
    case Jack => 10
    case Queen => 11
    case King => 12
    case _ => throw new Error()
  }

  def coord(card : Card) : (Double, Double) = {
    val (x, y) =  card match {
      case (Joker, (Club | Spade)) => (0, 4)
      case (Joker, (Heart | Diamond)) => (1, 4)
      case (r, s) => (column(r), line(s))
    }
    (x * width, y * height)
  }

  def imageView(card : Card,
                width : Double,
                height : Double,
                front : Boolean) = {
    val cc: (Double, Double) =
      if(front) coord(card)
      else backCoord

    new CardImageView(card, img) {
      preserveRatio = true
      viewport = new Rectangle2D(cc._1, cc._2, width = width, height = height)
    }
  }


  def back : ImageView = new ImageView(img){
    preserveRatio = true
    viewport = new Rectangle2D(backCoord._1, backCoord._2, width = width, height = height)
  }
  def back(fitHeight : Double): ImageView = {
    val civ = back
    civ.fitHeight = fitHeight
    civ
  }

  def apply(card : Card,
            front : Boolean = true): CardImageView = imageView(card, width, height, front)

  def cut(card : Card, fitHeight : Double, cut : Double,
          front : Boolean = true): CardImageView = {
    val civ = imageView(card, width/ cut, height, front)
    civ.fitHeight = fitHeight
    civ
  }
  def apply(card : Card, fitHeight : Double): CardImageView = {
    val civ = imageView(card, width, height, front = true)
    civ.fitHeight = fitHeight
    civ
  }


  private def bottomHalfView(card : Card,
                          width : Double,
                          front : Boolean) = {
    val cc: (Double, Double) =
      if(front) coord(card)
      else backCoord

    new CardImageView(card, img) {
      preserveRatio = true
      viewport = new Rectangle2D(cc._1, cc._2 + height/2, width = width, height = height/2)
    }
  }

  def bottomHalf(card : Card,
                 front : Boolean = true): CardImageView =
    bottomHalfView(card, width, front)

  def cutBottomHalf(card : Card,
                 front : Boolean = true): CardImageView =
    bottomHalfView(card, width/3, front)

  def topHalf(card : Card,
              front : Boolean = true): CardImageView =
    imageView(card, width, height/2, front)

  def cutTopHalf(card : Card,
                 front : Boolean = true): CardImageView =
    imageView(card, width/3, height/2, front)



}
