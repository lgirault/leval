package leval.serialization

import java.nio.ByteBuffer

import akka.serialization.SerializerWithStringManifest
import leval.core._

/**
  * Created by Loïc Girault on 31/08/16.
  */
object GameSerializer {

  val ruleSize = 1

  def rulesToBinary(r : Rules): Array[Byte] = {
    val rid : Byte = r match {
      case Sinnlos => 0x00
      case Antares => 0x01
      case Helios =>  0x02
    }

    Array[Byte](rid)
  }

  private val rules = Array(Sinnlos, Antares, Helios)

  def getRules(bb : ByteBuffer) : Rules =
    rules(bb.get().toInt)

}

object GameManifest{
  val initGame = "initGame"

  // moves
  val majestyEffect = "majestyEffect"
  val attackBeing = "attackBeing"
  val removeFromHand = "removeFromHand"
  val activateBeing = "activateBeing"
  val collect = "collect"
  val reveal = "reveal"
  val lookCard = "lookCard"
  val placeBeing = "placeBeing"
  val bury = "bury"
  val buryRequest = "buryRequest"
  val educateSwitch = "educateSwitch"
  val educateRise = "educateRise"
  val influencePhase = "influencePhase"
  val actPhase = "actPhase"
  val sourcePhase = "sourcePhase"

  val twilight = "twilight"

}


class GameSerializer
  extends SerializerWithStringManifest {

  def identifier: Int = 79658247

  def manifest(o: AnyRef): String = o match {
    case _ : GameInit => GameManifest.initGame //not a move

    case _ : MajestyEffect => GameManifest.majestyEffect
    case _ : AttackBeing => GameManifest.attackBeing
    case _ : RemoveFromHand => GameManifest.removeFromHand
    case _ : ActivateBeing => GameManifest.activateBeing
    case _ : Collect => GameManifest.collect
    case _ : Reveal => GameManifest.reveal
    case _ : LookCard => GameManifest.lookCard
    case _ : PlaceBeing => GameManifest.placeBeing
    case _ : Bury => GameManifest.bury

    case _ : BuryRequest => GameManifest.buryRequest //not a move

    case _ : Switch => GameManifest.educateSwitch
    case _ : Rise => GameManifest.educateRise
    case _ : InfluencePhase => GameManifest.influencePhase
    case _ : ActPhase => GameManifest.actPhase
    case SourcePhase => GameManifest.sourcePhase

    case _ : Twilight => GameManifest.twilight //not a move
  }

  def toBinary(o: AnyRef): Array[Byte] = o match {
    case GameInit(twilight, stars, source, rules) =>

      val pids = stars map (_.id) map playerIdToBinary

      val handSize = CardSerializer.cardSize * stars.head.hand.size
      //all hands have same size
      val starsSize = byte * 2 + /*numStar + card per hand*/
        pids.map(_.length).sum +
        stars.size * (int + handSize)
      val size = TwilightSerializer.binarySize(twilight) +
        starsSize + byte + source.size * CardSerializer.cardSize +
        GameSerializer.ruleSize

      val bb = ByteBuffer allocate size

      TwilightSerializer.put(bb, twilight)

      bb put stars.size.toByte
      bb put stars.head.hand.size.toByte
      (pids zip stars).reverseIterator foreach {
        case (pid, Star(_, maj, h)) =>
          bb put pid
          bb putInt maj
          h.foreach(CardSerializer.put(bb, _))
      }

      bb put source.size.toByte
      source.reverseIterator.foreach(CardSerializer.put(bb, _))

      bb put GameSerializer.rulesToBinary(rules)
      bb.array()
    case MajestyEffect(v, tgt) =>
      val bb = ByteBuffer.allocate( int * 2)
      bb putInt v
      bb putInt tgt
      bb.array()

    case AttackBeing(origin, tgtBeing, tgtSuit) =>
      val bb = ByteBuffer.allocate(OriginSerializer.binarySize(origin) +
        BeingSerializer.binarySize(tgtBeing) +
        CardSerializer.suitSize)
      OriginSerializer.put(bb, origin)
      BeingSerializer.put(bb, tgtBeing)
      bb put (CardSerializer toByte tgtSuit)
      bb.array()

    case RemoveFromHand(c) =>
      CardSerializer.toBinary(c)
    case ActivateBeing(c) =>
      CardSerializer.toBinary(c)

    case Collect(or, tgt) =>
      val bb = ByteBuffer.allocate(OriginSerializer.binarySize(or) + byte)
      OriginSerializer.put(bb, or)
      if(tgt == Source)
        bb.put(0x00.toByte)
      else bb.put(0x01.toByte)
      bb.array()

    case Reveal(tgt, s) =>
      val bb = ByteBuffer.allocate(CardSerializer.cardSize + byte)
      CardSerializer.put(bb, tgt)
      bb.put(CardSerializer toByte s)
      bb.array()

    case LookCard(or, tgt , s) =>
      val bb = ByteBuffer.allocate(OriginSerializer.binarySize(or) +
        CardSerializer.cardSize + byte)
      OriginSerializer.put(bb, or)
      CardSerializer.put(bb, tgt)
      bb.put(CardSerializer toByte s)
      bb.array()

    case PlaceBeing(being, side) =>
      val bb = ByteBuffer.allocate(BeingSerializer.binarySize(being) + int)
      BeingSerializer.put(bb, being)
      bb putInt side
      bb.array()

    case Bury(tgt, order) =>
      val bb = ByteBuffer.allocate(CardSerializer.cardSize * (order.size + 1) + int)
      CardSerializer.put(bb, tgt)
      bb putInt order.size
      order.reverseIterator foreach (CardSerializer.put(bb, _))
      bb.array()
    case BuryRequest(tgt, toBury) =>
      val bb = ByteBuffer.allocate(BeingSerializer.binarySize(tgt) +
        int + toBury.size * CardSerializer.cardSize )
      BeingSerializer.put(bb, tgt)
      bb putInt toBury.size
      toBury foreach (CardSerializer.put(bb, _))
      bb.array()
    case Switch(tgt, c) =>
      val bb = ByteBuffer.allocate(CardSerializer.cardSize * 2)
      CardSerializer.put(bb, tgt)
      CardSerializer.put(bb, c)
      bb.array()

    case Rise(tgt, cs) =>
      val bb = ByteBuffer.allocate(CardSerializer.cardSize * (cs.size + 1) + int)
      CardSerializer.put(bb, tgt)
      bb putInt cs.size
      cs.reverseIterator foreach (CardSerializer.put(bb, _))
      bb.array()
    case InfluencePhase(newPlayer) =>
      val bb = ByteBuffer.allocate(int)
      bb.putInt(newPlayer)
      bb.array()
    case ActPhase(activatedBeings) =>
      val bb = ByteBuffer.allocate(int + CardSerializer.cardSize * activatedBeings.size)
      bb putInt activatedBeings.size
      activatedBeings foreach ( CardSerializer.put(bb, _) )
      bb.array()
    case SourcePhase => Array.empty
    case t: Twilight =>
      TwilightSerializer.toBinary(t)

  }

  def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {
    manifest match {
      case GameManifest.`initGame` =>
        val bb = ByteBuffer wrap bytes
        val t = TwilightSerializer.fromBinary(bb)
        val numStar = bb.get().toInt
        val handSize = bb.get().toInt
        var ss = List[Star]()
        for(_ <- 1 to numStar) {
          val pid = playerIdFromBinary(bb)
          val m = bb.getInt()
          var hand = Star.emptyHand
          for(_ <- 1 to handSize){
            hand += CardSerializer fromBinary bb
          }
          ss ::= Star(pid, m, hand)
        }

        val deckSize = bb.get().toInt
        var deck = List[Card]()
        for(_ <- 1 to deckSize) {
          deck ::= CardSerializer fromBinary bb
        }
        val rules = GameSerializer.getRules(bb)
        GameInit(t, ss, deck, rules)

      case GameManifest.majestyEffect =>
        val bb = ByteBuffer wrap bytes
        val v = bb.getInt()
        val tgt = bb.getInt()
        MajestyEffect(v, tgt)

      case GameManifest.attackBeing =>
        val bb = ByteBuffer.wrap(bytes)
        val kindId = bb.get()
        val o = OriginSerializer.fromBinary(bb, kindId)
        val tgt = BeingSerializer.fromBinary(bb)
        val s = CardSerializer.suits(bb.get().toInt)
        AttackBeing(o, tgt, s)

      case GameManifest.removeFromHand =>
        RemoveFromHand(CardSerializer fromBinary bytes)
      case GameManifest.activateBeing =>
        ActivateBeing(CardSerializer fromBinary bytes)
      case GameManifest.collect =>
        val bb = ByteBuffer wrap bytes
        val or = OriginSerializer.fromBinary(bb)
        val tgt =
          if(bb.get == 0) Source
          else DeathRiver
        Collect(or, tgt)

      case GameManifest.reveal =>
        val bb = ByteBuffer wrap bytes
        val tgt = CardSerializer.fromBinary(bb)
        val s = CardSerializer.suits(bb.get().toInt)
        Reveal(tgt, s)

      case GameManifest.lookCard =>
        val bb = ByteBuffer wrap bytes
        val kindId = bb.get()
        val or = OriginSerializer.fromBinary(bb, kindId)
        val tgt = CardSerializer.fromBinary(bb)
        val s = CardSerializer.suits(bb.get().toInt)
        LookCard(or, tgt, s)

      case GameManifest.placeBeing =>
        val bb = ByteBuffer wrap bytes
        val being = BeingSerializer fromBinary bb
        val side = bb.getInt()
        PlaceBeing(being, side)

      case GameManifest.bury =>
        val bb = ByteBuffer wrap bytes

        val tgt = CardSerializer.fromBinary(bb)
        val size = bb.getInt()
        var l = List[Card]()
        for( _ <- 1 to size ){
          l ::= CardSerializer.fromBinary(bb)
        }
        Bury(tgt, l)

      case GameManifest.buryRequest =>
        val bb = ByteBuffer wrap bytes
        val tgt = BeingSerializer.fromBinary(bb)
        val size = bb.getInt()
        var s = Set[Card]()
        for( _ <- 1 to size ){
          s += CardSerializer.fromBinary(bb)
        }
        BuryRequest(tgt, s)

      case GameManifest.educateSwitch =>
        val bb = ByteBuffer wrap bytes
        val tgt = CardSerializer.fromBinary(bb)
        val c = CardSerializer.fromBinary(bb).asInstanceOf[C]
       Switch(tgt, c)

      case GameManifest.educateRise =>
        val bb = ByteBuffer wrap bytes

        val tgt = CardSerializer.fromBinary(bb)
        val size = bb.getInt()
        var l = List[C]()
        for( _ <- 1 to size ){
          l ::= CardSerializer.fromBinary(bb).asInstanceOf[C]
        }
        Rise(tgt, l)
      case GameManifest.influencePhase =>
        val bb = ByteBuffer wrap bytes
        InfluencePhase(bb.getInt())

      case GameManifest.actPhase =>
        val bb = ByteBuffer wrap bytes
        val s = bb.getInt
        var set = Set[Card]()
        for( _ <- 1 to s){
          set += CardSerializer fromBinary bb
        }
        ActPhase(set)

      case GameManifest.sourcePhase => SourcePhase
      case GameManifest.twilight =>
        TwilightSerializer fromBinary bytes

    }
  }
}
object TwilightSerializer {


  def binarySize(t : Twilight) = {
    val s = t.cards.size
    CardSerializer.cardSize *
      (t.cards.head.size * t.cards.size) + 2 * byte
  }


  def toBinary(twilight: Twilight) : Array[Byte] = {
    val bb = ByteBuffer.allocate( binarySize(twilight))
    put(bb, twilight)
    bb.array()
  }

  def put(bb : ByteBuffer, t : Twilight) : Unit = {
    bb put t.cards.size.toByte
    bb put t.cards.head.size.toByte

    t.cards.reverseIterator.foreach {
      cs =>
        cs.reverseIterator foreach (CardSerializer.put(bb, _))
    }
  }

  def fromBinary(bytes : Array[Byte]) : Twilight =
    fromBinary(ByteBuffer wrap bytes)

  def fromBinary(bb : ByteBuffer) : Twilight = {
    val size = bb.get().toInt
    val s = bb.get().toInt
    var cards = List[Seq[Card]]()
    for( _ <- 1 to size) {
      var cs = List[Card]()
      for( _ <- 1 to s) {
        cs ::= (CardSerializer fromBinary bb)
      }
      cards ::= cs
    }
    Twilight(cards)
  }
}

