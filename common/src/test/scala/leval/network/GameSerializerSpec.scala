package leval.network

import leval.AcceptanceSpec
import leval.core._
import leval.serialization._
import leval.TestSamples._
import leval.core.Joker.{Black, Red}
/**
  * Created by lorilan on 9/1/16.
  */
class GameSerializerSpec
  extends AcceptanceSpec {

  val serializer = new GameSerializer

  import serializer.{toBinary, fromBinary}

  "A game serializer" - {
    "should encode and decode a GameInit" in {
      val g = GameInit.gameWithoutMulligan(Seq(PlayerId(0, "toto"),
        PlayerId(1, "titi")), Helios)
      val b = toBinary(g)
      fromBinary(b, GameManifest.initGame) shouldBe g
    }
    "should encode and decode a MajesticEffect" in {
      val m = MajestyEffect(12, 1)
      val b = toBinary(m)
      fromBinary(b, GameManifest.majestyEffect) shouldBe m
    }
    "should encode and decode a AttackBeing" in {
      val m = AttackBeing(CardOrigin.Hand(0, (5, Spade)), child, Heart)
      val b = toBinary(m)
      fromBinary(b, GameManifest.attackBeing) shouldBe m
    }
    "should encode and decode a RemoveFromHand" in {
      val m = RemoveFromHand((8, Club))
      val b = toBinary(m)
      fromBinary(b, GameManifest.removeFromHand) shouldBe m
    }
    "should encode and decode a ActivateBeing" in {
      val m = ActivateBeing((King, Diamond))
      val b = toBinary(m)
      fromBinary(b, GameManifest.activateBeing) shouldBe m
    }
    "should encode and decode a Collect source from start" in {
      val m = Collect(Origin.Star(1), Source)
      val b = toBinary(m)
      fromBinary(b, GameManifest.collect) shouldBe m
    }
    "should encode and decode a Collect deathRiver from being" in {
      val m = Collect(CardOrigin.Being(fool, Club), DeathRiver)
      val b = toBinary(m)
      fromBinary(b, GameManifest.collect) shouldBe m
    }
    "should encode and decode a Reveal" in {
      val m = Reveal((Queen, Heart), Spade)
      val b = toBinary(m)
      fromBinary(b, GameManifest.reveal) shouldBe m
    }
    "should encode and decode a LookCard" in {
      val m = LookCard(CardOrigin.Being(spectre, Club), (Queen, Heart), Club)
      val b = toBinary(m)
      fromBinary(b, GameManifest.lookCard) shouldBe m
    }
    "should encode and decode a PlaceBeing" in {
      val m = PlaceBeing(wizard, 1)
      val b = toBinary(m)
      fromBinary(b, GameManifest.placeBeing) shouldBe m
    }
    "should encode and decode a Bury" in {
      val m = Bury((Jack, Spade), List[Card](J(0, Red), (3, Spade), (10, Club)))
      val b = toBinary(m)
      fromBinary(b, GameManifest.bury) shouldBe m
    }
    "should encode and decode a BuryRequest" in {
      val m = BuryRequest(wizard, Set[Card](J(0, Red), (3, Spade), (10, Club)))
      val b = toBinary(m)
      fromBinary(b, GameManifest.buryRequest) shouldBe m
    }
    "should encode and decode a Switch" in {
      val m = Switch(J(1, Black), C(1, Numeric(5), Spade))
      val b = toBinary(m)
      fromBinary(b, GameManifest.educateSwitch) shouldBe m
    }
    "should encode and decode a Rise" in {
      val m = Rise((King, Heart),
        Map[Suit, Card](Club -> ((10, Club)),
        Spade -> ((1, Spade))))
      val b = toBinary(m)
      fromBinary(b, GameManifest.educateRise) shouldBe m
    }
    "should encode and decode a InfluencePhase" in {
      val m = InfluencePhase(1)
      val b = toBinary(m)
      fromBinary(b, GameManifest.influencePhase) shouldBe m
    }
    "should encode and decode a ActPhase" in {
      val m = ActPhase(Set())
      val b = toBinary(m)
      fromBinary(b, GameManifest.actPhase) shouldBe m
    }
    "should encode and decode a SourcePhase" in {
      val m = SourcePhase
      val b = toBinary(m)
      fromBinary(b, GameManifest.sourcePhase) shouldBe m
    }

    "should encode and decode a Twilight" in {
      val m = Twilight(Seq(Seq[Card]((10, Club), (1, Spade)),
        Seq[Card](J(0, Red), (3, Spade)),
        Seq[Card]((Queen, Heart), (King, Diamond))))
      val b = toBinary(m)
      fromBinary(b, GameManifest.twilight) shouldBe m
    }
  }
}
