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

  val width : Double = 208
  val height : Double = 303

  val backUrl = this.getClass.getResource("/cards/back.png").toExternalForm
  val backImg = new Image(backUrl)
  private def suit2string(s : Suit) = s match {
    case Club => "clubs"
    case Heart => "hearts"
    case Diamond => "diamonds"
    case Spade => "spades"
  }
  private def rank2string(s : Rank) = s match {
    case Numeric(i) => i.toString
    case Jack => "Jack"
    case Queen => "Queen"
    case King => "King"
  }

  import Joker._
  def cardBaseFileName(c : Card): String = c match {
    case Joker(Black) => "Joker_black"
    case Joker(Red) => "Joker_red"
    case Card(r, s) => s"${rank2string(r)}_of_${suit2string(s)}"
  }

  def cardUrl(c : Card) =
    this.getClass.getResource(s"/cards/${cardBaseFileName(c)}.png").toExternalForm

  def cardImg(c : Card) = new Image(cardUrl(c))

  def imageView(card : Card,
                minX : Double, minY : Double,
                width : Double, height : Double,
                sfitHeight : Option[Double],
                front : Boolean) =
    new CardImageView(card, if(front) cardImg(card) else backImg){
      preserveRatio = true
      viewport = new Rectangle2D(minX, minY, width = width, height = height)
      sfitHeight foreach fitHeight_=
    }



  def back(sfitHeight : Option[Double] = None): ImageView =
    new ImageView(backImg){
      preserveRatio = true
      sfitHeight foreach fitHeight_=
    }


  def cutRight(card : Card, cut : Double,
               sfitHeight : Option[Double] = None,
               front : Boolean = true): CardImageView = {
    val delta = ((cut - 1) / cut) * width
    imageView(card, delta, 0, width / cut, height, sfitHeight, front)
  }

  def cutLeft(card : Card, cut : Double,
              sfitHeight : Option[Double] = None,
              front : Boolean = true): CardImageView =
    imageView(card, 0, 0, width/ cut, height, sfitHeight, front)

  def apply(card : Card,
            sfitHeight : Option[Double] = None,
            front : Boolean = true): CardImageView =
    imageView(card, 0, 0, width, height, sfitHeight, front)



  private def bottomHalfView(card : Card,
                             width : Double,
                             sfitHeight : Option[Double],
                             front : Boolean) =
    imageView(card, 0, height/2, width, height/2, sfitHeight, front)

  def bottomHalf(card : Card,
                 sfitHeight : Option[Double],
                 front : Boolean = true): CardImageView =
    bottomHalfView(card, width, sfitHeight, front)

  def cutBottomHalf(card : Card,
                    sfitHeight : Option[Double],
                    front : Boolean = true): CardImageView =
    bottomHalfView(card, width/3, sfitHeight, front)

  def topHalf(card : Card,
              sfitHeight : Option[Double],
              front : Boolean = true): CardImageView =
    imageView(card, 0, 0, width, height/2, sfitHeight, front)

  def cutTopHalf(card : Card,
                 sfitHeight : Option[Double],
                 front : Boolean = true): CardImageView =
    imageView(card, 0, 0, width/3, height/2, sfitHeight, front)



}
