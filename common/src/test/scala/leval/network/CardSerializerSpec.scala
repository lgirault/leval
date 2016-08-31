package leval.network

import leval.AcceptanceSpec
import leval.core._

/**
  * Created by Loïc Girault on 31/08/16.
  */
class CardSerializerSpec
  extends AcceptanceSpec {

  val serializer = new CardSerializer
  import serializer.{toBinary, fromBinary}

  "A card serializer" - {
    "should encode and decode a red joker" in {
      import leval.core.Joker.Red
      val redJoker = J(0, Red)
      val (d, c) = toBinary(redJoker)
      fromBinary(d, c) shouldBe redJoker
    }
    "should encode and decode a black joker" in {
      import leval.core.Joker.Black
      val blackJoker = J(0, Black)
      val (d,c) = toBinary(blackJoker)
      fromBinary(d,c) shouldBe blackJoker
    }

    "should encode and decode a king of diamond" in {
      val card = C(0, King, Diamond)
      val (d,c) = toBinary(card)
      fromBinary(d,c) shouldBe card
    }

    "should encode and decode a queen of club" in {
      val card = C(0, Queen, Club)
      val (d,c) = toBinary(card)
      fromBinary(d,c) shouldBe card
    }

    "should encode and decode a jack of spade" in {
      val card = C(0, Jack, Spade)
      val (d,c) = toBinary(card)
      fromBinary(d,c) shouldBe card
    }

    "should encode and decode an ace of heart" in {
      val card = C(0, Numeric(1), Heart)
      val (d,c) = toBinary(card)
      fromBinary(d,c) shouldBe card
    }

    "should encode and decode a 10 of diamond" in {
      val card = C(0, Numeric(10), Diamond)
      val (d,c) = toBinary(card)
      fromBinary(d,c) shouldBe card
    }

    "should encode and decode a 7 of club " in {
      val card = C(0, Numeric(7), Club)
      val (d,c) = toBinary(card)
      fromBinary(d,c) shouldBe card
    }
  }


}
