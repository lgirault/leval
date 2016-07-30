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

  def cardBaseFileName(c : Card): String = c match {
    case Joker.Black => "Joker_black"
    case Joker.Red => "Joker_red"
    case C(r, s) => s"${rank2string(r)}_of_${suit2string(s)}"
  }

  def cardUrl(c : Card) =
    this.getClass.getResource(s"/cards/${cardBaseFileName(c)}.png").toExternalForm

  def cardImg(c : Card) = new Image(cardUrl(c))

  def imageView(card : Card,
                width : Double,
                height : Double,
                front : Boolean) =
    new CardImageView(card, if(front) cardImg(card) else backImg){
      preserveRatio = true
      viewport = new Rectangle2D(0, 0, width = width, height = height)
    }



  def back(sfitHeight : Option[Double] = None): ImageView =
    new ImageView(backImg){
      preserveRatio = true
      sfitHeight foreach fitHeight_=
    }


  def cutRight(card : Card, cut : Double,
               sfitHeight : Option[Double] = None,
               front : Boolean = true): CardImageView = {

    val img = if(front) cardImg(card)
    else backImg
    val delta = ((cut - 1) / cut) * width
    val civ = new CardImageView(card, img) {
      preserveRatio = true
      viewport = new Rectangle2D(delta, 0,
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
                             front : Boolean) =
    new CardImageView(card, if(front) cardImg(card) else backImg) {
      preserveRatio = true
      viewport = new Rectangle2D(0, height/2, width = width, height = height/2)
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
