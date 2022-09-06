package gp.leval.network

import gp.leval.AcceptanceSpec
import gp.leval.core.*
import gp.leval.codecs.binary.CardSerializer.{toByte, fromBinary}

/** Created by Loïc Girault on 31/08/16.
  */
class CardSerializerSpec extends AcceptanceSpec {

  "A card serializer" - {
    "should encode and decode a red joker" in {
      import gp.leval.core.Joker.Red
      val redJoker = Card.J(0, Red)
      val (d, c) = toByte(redJoker)
      fromBinary(d, c).shouldBe(redJoker)
    }
    "should encode and decode a black joker" in {
      import gp.leval.core.Joker.Black
      val blackJoker = Card.J(0, Black)
      val (d, c) = toByte(blackJoker)
      fromBinary(d, c).shouldBe(blackJoker)
    }

    "should encode and decode a king of diamond" in {
      val card = Card.C(0, Rank.King, Suit.Diamond)
      val (d, c) = toByte(card)
      fromBinary(d, c).shouldBe(card)
    }

    "should encode and decode a queen of club" in {
      val card = Card.C(0, Rank.Queen, Suit.Club)
      val (d, c) = toByte(card)
      fromBinary(d, c).shouldBe(card)
    }

    "should encode and decode a jack of spade" in {
      val card = Card.C(0, Rank.Jack, Suit.Spade)
      val (d, c) = toByte(card)
      fromBinary(d, c).shouldBe(card)
    }

    "should encode and decode an ace of heart" in {
      val card = Card.C(0, Rank.Numeric(1), Suit.Heart)
      val (d, c) = toByte(card)
      fromBinary(d, c).shouldBe(card)
    }

    "should encode and decode a 10 of diamond" in {
      val card = Card.C(0, Rank.Numeric(10), Suit.Diamond)
      val (d, c) = toByte(card)
      fromBinary(d, c).shouldBe(card)
    }

    "should encode and decode a 7 of club " in {
      val card = Card.C(0, Rank.Numeric(7), Suit.Club)
      val (d, c) = toByte(card)
      fromBinary(d, c).shouldBe(card)
    }
  }

}
