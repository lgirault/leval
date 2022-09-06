package gp.leval.network

import gp.leval.AcceptanceSpec
import gp.leval.core.*
import gp.leval.codecs.binary.*
import gp.leval.TestSamples.{given, *}
import gp.leval.core.Joker.{Black, Red}

/** Created by lorilan on 9/1/16.
  */
class GameSerializerSpec extends AcceptanceSpec {

  val serializer = new GameSerializer

  import serializer.{toBinary, fromBinary}

  "A game serializer" - {
    "should encode and decode a GameInit" in {
      val g = GameInit.gameWithoutMulligan(
        List(PlayerId(None, "toto"), PlayerId(Some(1), "titi")),
        Rules(Helios)
      )
      val b = toBinary(g)
      fromBinary(b, GameManifest.initGame).shouldBe(g)
    }
    "should encode and decode a MajesticEffect" in {
      val m = MajestyEffect(12, 1)
      val b = toBinary(m)
      fromBinary(b, GameManifest.majestyEffect).shouldBe(m)
    }
    "should encode and decode a AttackBeing" in {
      val m = AttackBeing(CardOrigin.Hand(0, (5, Suit.Spade)), child, Suit.Heart)
      val b = toBinary(m)
      fromBinary(b, GameManifest.attackBeing).shouldBe(m)
    }
    "should encode and decode a RemoveFromHand" in {
      val m = RemoveFromHand((8, Suit.Club))
      val b = toBinary(m)
      fromBinary(b, GameManifest.removeFromHand).shouldBe(m)
    }
    "should encode and decode a ActivateBeing" in {
      val m = ActivateBeing((Rank.King, Suit.Diamond))
      val b = toBinary(m)
      fromBinary(b, GameManifest.activateBeing).shouldBe(m)
    }
    "should encode and decode a Collect source from start" in {
      val m = Collect(Origin.Star(1), Source)
      val b = toBinary(m)
      fromBinary(b, GameManifest.collect).shouldBe(m)
    }
    "should encode and decode a Collect deathRiver from being" in {
      val m = Collect(CardOrigin.Being(fool, Suit.Club), DeathRiver)
      val b = toBinary(m)
      fromBinary(b, GameManifest.collect).shouldBe(m)
    }
    "should encode and decode a Reveal" in {
      val m = Reveal((Rank.Queen, Suit.Heart), Suit.Spade)
      val b = toBinary(m)
      fromBinary(b, GameManifest.reveal).shouldBe(m)
    }
    "should encode and decode a LookCard" in {
      val m = LookCard(CardOrigin.Being(spectre, Suit.Club), (Rank.Queen, Suit.Heart), Suit.Club)
      val b = toBinary(m)
      fromBinary(b, GameManifest.lookCard).shouldBe(m)
    }
    "should encode and decode a PlaceBeing" in {
      val m = PlaceBeing(wizard, 1)
      val b = toBinary(m)
      fromBinary(b, GameManifest.placeBeing).shouldBe(m)
    }
    "should encode and decode a Bury" in {
      val m = Bury((Rank.Jack, Suit.Spade), List[Card](Card.J(0, Red), (3, Suit.Spade), (10, Suit.Club)))
      val b = toBinary(m)
      fromBinary(b, GameManifest.bury).shouldBe(m)
    }
    "should encode and decode a BuryRequest" in {
      val m = BuryRequest(wizard, Set[Card](Card.J(0, Red), (3, Suit.Spade), (10, Suit.Club)))
      val b = toBinary(m)
      fromBinary(b, GameManifest.buryRequest).shouldBe(m)
    }
    "should encode and decode a Switch" in {
      val m = Switch(Card.J(1, Black), Card.C(1, Rank.Numeric(5), Suit.Spade))
      val b = toBinary(m)
      fromBinary(b, GameManifest.educateSwitch).shouldBe(m)
    }
    "should encode and decode a Rise" in {
      val m = Rise(
        (Rank.King, Suit.Heart),
        Map[Suit, Card](Suit.Club -> ((10, Suit.Club)), Suit.Spade -> ((1, Suit.Spade)))
      )
      val b = toBinary(m)
      fromBinary(b, GameManifest.educateRise).shouldBe(m)
    }
    "should encode and decode a InfluencePhase" in {
      val m = InfluencePhase(1)
      val b = toBinary(m)
      fromBinary(b, GameManifest.influencePhase).shouldBe(m)
    }
    "should encode and decode a ActPhase" in {
      val m = ActPhase(Set())
      val b = toBinary(m)
      fromBinary(b, GameManifest.actPhase).shouldBe(m)
    }
    "should encode and decode a SourcePhase" in {
      val m = SourcePhase
      val b = toBinary(m)
      fromBinary(b, GameManifest.sourcePhase).shouldBe(m)
    }

    "should encode and decode a Twilight" in {
      val m = Twilight(
        Seq(
          Seq[Card]((10, Suit.Club), (1, Suit.Spade)),
          Seq[Card](Card.J(0, Red), (3, Suit.Spade)),
          Seq[Card]((Rank.Queen, Suit.Heart), (Rank.King, Suit.Diamond))
        )
      )
      val b = toBinary(m)
      fromBinary(b, GameManifest.twilight).shouldBe(m)
    }

    "should encode and decode an empty Twilight" in {
      val m = Twilight(Seq())
      val b = toBinary(m)
      fromBinary(b, GameManifest.twilight).shouldBe(m)
    }
    "should encode and decode a Twilight with empty seqs" in {
      val m = Twilight(Seq(Seq(), Seq()))
      val b = toBinary(m)
      fromBinary(b, GameManifest.twilight).shouldBe(m)
    }

    "should encode and decode Rules" in {
      val r = Rules(Helios, ostein = true, allowMulligan = true)
      val b = GameSerializer.rulesToBinary(r)
      GameSerializer.getRules(b(0)).shouldBe(r)
    }
    "should encode and decode Rules (2)" in {
      val r = Rules(Antares, janus = true)
      val b = GameSerializer.rulesToBinary(r)
      GameSerializer.getRules(b(0)).shouldBe(r)
    }
    "should encode and decode Rules (3)" in {
      val r = Rules(Sinnlos, nedemone = true)
      val b = GameSerializer.rulesToBinary(r)
      GameSerializer.getRules(b(0)).shouldBe(r)
    }
  }
}
