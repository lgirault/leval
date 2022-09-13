package gp.leval.gamescreen

import gp.leval.core.deck54
import gp.leval.core.{Card, Joker, Rank}
import gp.pixijs.{Sprite, Texture}
import scala.scalajs.js
import gp.leval.gamescreen.TextureDictionary.keyOff
import cats.effect.Sync

object TextureDictionary:
  private def keyOff(c: Card) : String = 
    c match {
      case Joker(color) => s"Joker_${color.toString().toLowerCase()}"
      case Card(Rank.Numeric(i), suit) => s"${i}_of_${suit.toString().toLowerCase()}s"
      case Card(rank, suit) => s"${rank}_of_${suit.toString().toLowerCase()}s"
  }

  def load[F[_]](using F: Sync[F]) : F[TextureDictionary] = 
    F.delay(TextureDictionary(js.Dictionary(deck54().map(keyOff)
    .map(k => (k -> Texture.from(s"assets/cards/$k.png")))*)))




class TextureDictionary(tileSet: js.Dictionary[Texture]):

  def sprite(c: Card): Sprite = 
    new Sprite(tileSet(keyOff(c)))
  

  
  

