package gp.leval.codecs.binary

import gp.leval
import gp.leval.core.*

import java.nio.ByteBuffer

/** Created by Loïc Girault on 31/08/16.
  */
object CardSerializer {
  val suits = Array[Suit](Suit.Diamond, Suit.Club, Suit.Heart, Suit.Spade)

  val suitSize = 1

  def toByte(s: Suit): Byte = s match {
    case Suit.Diamond => 0x0
    case Suit.Club    => 0x1
    case Suit.Heart   => 0x2
    case Suit.Spade   => 0x3
  }
  // for jokers
  // 0x4 = Black
  // 0x5 = Red

  private def toByte(r: Rank): Byte = r match {
    case Rank.Numeric(v) => v.toByte // 0x1 to 0xA
    case Rank.Jack       => 0xb // also used for joker
    case Rank.Queen      => 0xc
    case Rank.King       => 0xd
  }

  val cardSize = 2

  import Joker.{Black, Red}
  def toByte(c: Card): (Byte, Byte) = c match {
    case Card.C(dId, r, s) =>
      (dId, (toByte(s) << 4 | toByte(r)).toByte)
    case Card.J(dId, Black) => (dId, 0x4b)
    case Card.J(dId, Red)   => (dId, 0x5b)
  }

  def put(bb: ByteBuffer, card: Card): Unit = {
    val (id, c) = toByte(card)
    bb.put(id)
    leval.ignore(bb.put(c))
  }

  def toBinary(c: Card): Array[Byte] = {
    val bb = ByteBuffer.allocate(2)
    put(bb, c)
    bb.array()
  }

  def fromBinary(deckId: Byte, c: Byte): Card = {
    val suit = (c & 0xf0) >> 4
    val rank = c & 0x0f
    suit match {
      case 0x4 => Card.J(deckId, Black)
      case 0x5 => Card.J(deckId, Red)
      case _ =>
        val s = CardSerializer.suits(suit)
        val r = rank match {
          case 0xb => Rank.Jack
          case 0xc => Rank.Queen
          case 0xd => Rank.King
          case _   => Rank.Numeric(rank)
        }
        Card.C(deckId, r, s)
    }
  }

  def fromBinary(bytes: Array[Byte]): Card =
    fromBinary(ByteBuffer.wrap(bytes))

  def fromBinary(bb: ByteBuffer): Card = {
    val id = bb.get()
    val c = bb.get()
    fromBinary(id, c)
  }

}
