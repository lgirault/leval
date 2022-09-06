package gp.leval.gamescreen

import gp.leval.core.{Card, Joker, Rank}
import gp.pixijs.{Sprite, Texture}
import scala.scalajs.js



class TextureDictionary(tileSet: js.Dictionary[Texture]) {


  def sprite(c: Card): Sprite = {
    new Sprite(tileSet(keyOff(c)))
  }

  private def keyOff(c: Card) : String = 
    c match {
      case Joker(color) => s"Joker_${color.toString().toLowerCase()}"
      case Card(Rank.Numeric(i), suit) => s"${i}_of_${suit.toString().toLowerCase()}"
      case Card(rank, suit) => s"${rank}_of_${suit.toString().toLowerCase()}"
  }
  
}
