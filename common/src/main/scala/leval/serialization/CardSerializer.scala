package leval.serialization

import leval.core._

/**
  * Created by Loïc Girault on 31/08/16.
  */
object CardSerializer {
  private val suits = Array[Suit](Diamond, Club, Heart, Spade)
}
class CardSerializer {

  private def toBinary(s : Suit) : Byte = s match {
    case Diamond => 0x0
    case Club => 0x1
    case Heart => 0x2
    case Spade => 0x3
  }
  //for jokers
  //0x4 = Black
  //0x5 = Red

  private def toBinary(r: Rank): Byte = r match {
    case Numeric(v) => v.toByte // 0x1 to 0xA
    case Jack => 0xB // also used for joker
    case Queen => 0xC
    case King =>  0xD
  }

  import Joker.{Black, Red}
  def toBinary(c: Card): (Byte, Byte) = c match {
    case C(dId, r, s) =>
      (dId, (toBinary(s) << 4 | toBinary(r)).toByte)
    case J(dId, Black) => (dId, 0x4B)
    case J(dId, Red) => (dId, 0x5B)
  }

  def fromBinary(deckId : Byte, c : Byte): Card = {
    val suit = (c & 0xF0) >> 4
    val rank = c & 0x0F
    suit match {
      case 0x4 => J(deckId, Black)
      case 0x5 => J(deckId, Red)
      case _ =>
        val s = CardSerializer.suits(suit)
        val r = rank match {
          case 0xB => Jack
          case 0xC => Queen
          case 0xD => King
          case _=> Numeric(rank)
        }
        C(deckId, r, s)
    }

  }

}
