package gp.leval.serialization

import gp.leval
import java.nio.ByteBuffer

import gp.leval.core._

/** Created by Loïc Girault on 31/08/16.
  */
object CardSerializer {
  val suits = Array[Suit](Diamond, Club, Heart, Spade)

  val suitSize = 1

  def toByte(s: Suit): Byte = s match {
    case Diamond => 0x0
    case Club    => 0x1
    case Heart   => 0x2
    case Spade   => 0x3
  }
  // for jokers
  // 0x4 = Black
  // 0x5 = Red

  private def toByte(r: Rank): Byte = r match {
    case Numeric(v) => v.toByte // 0x1 to 0xA
    case Jack       => 0xb // also used for joker
    case Queen      => 0xc
    case King       => 0xd
  }

  val cardSize = 2

  import Joker.{Black, Red}
  def toByte(c: Card): (Byte, Byte) = c match {
    case C(dId, r, s) =>
      (dId, (toByte(s) << 4 | toByte(r)).toByte)
    case J(dId, Black) => (dId, 0x4b)
    case J(dId, Red)   => (dId, 0x5b)
  }

  def put(bb: ByteBuffer, card: Card): Unit = {
    val (id, c) = toByte(card)
    bb put id
    leval.ignore(bb put c)
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
      case 0x4 => J(deckId, Black)
      case 0x5 => J(deckId, Red)
      case _ =>
        val s = CardSerializer.suits(suit)
        val r = rank match {
          case 0xb => Jack
          case 0xc => Queen
          case 0xd => King
          case _   => Numeric(rank)
        }
        C(deckId, r, s)
    }
  }

  def fromBinary(bytes: Array[Byte]): Card =
    fromBinary(ByteBuffer wrap bytes)

  def fromBinary(bb: ByteBuffer): Card = {
    val id = bb.get()
    val c = bb.get()
    fromBinary(id, c)
  }

}
