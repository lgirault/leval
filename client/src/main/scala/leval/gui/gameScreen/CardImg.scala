package leval.gui.gameScreen

/**
  * Created by lorilan on 6/21/16.
  */

import leval.core.{Joker, _}

import scalafx.geometry.Rectangle2D
import scalafx.scene.image.{Image, ImageView}

import javafx.scene.{image => jfxsi}

class JFXCardImageView(val card : Card, img : Image) extends jfxsi.ImageView(img)
class CardImageView(override val delegate: JFXCardImageView)
  extends ImageView {
  def this(card : Card, img : Image) = this(new JFXCardImageView(card, img))
  def card : Card = delegate.card
}



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
    case Numeric(i) => i - 1
    case Jack => 10
    case Queen => 11
    case King => 12
    case _ => throw new Error()
  }

  def coord(card : Card) : (Double, Double) = {
    val (x, y) =  card match {
      case Joker(Joker.Black) => (0, 4)
      case Joker(Joker.Red) => (1, 4)
      case C(r, s) => (column(r), line(s))
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


  def back(sfitHeight : Option[Double] = None): ImageView =
    new ImageView(img){
      preserveRatio = true
      viewport = new Rectangle2D(backCoord._1, backCoord._2, width = width, height = height)
      sfitHeight foreach fitHeight_=
    }


  def cutRight(card : Card, cut : Double,
               sfitHeight : Option[Double] = None,
               front : Boolean = true): CardImageView = {

    val cc: (Double, Double) =
      if(front) coord(card)
      else backCoord
    val delta = ((cut - 1) / cut) * width
    val civ = new CardImageView(card, img) {
      preserveRatio = true
      viewport = new Rectangle2D(cc._1 + delta,  cc._2,
        width = width / cut, height = height)
    }

    sfitHeight foreach civ.fitHeight_=

    civ
  }

  def cutLeft(card : Card, cut : Double,
              sfitHeight : Option[Double] = None,
              front : Boolean = true): CardImageView = {
    val civ = imageView(card, width/ cut, height, front)
    sfitHeight foreach civ.fitHeight_=
    civ
  }
  def apply(card : Card,
            sfitHeight : Option[Double] = None,
            front : Boolean = true): CardImageView = {
    val civ = imageView(card, width, height, front)
    sfitHeight foreach civ.fitHeight_=
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
