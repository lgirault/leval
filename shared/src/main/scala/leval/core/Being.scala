package leval.core

/**
  * Created by Loïc Girault on 20/06/16.
  */
case class Being
( head : FaceCard,
  heart : Option[Card],
  weapon : Option[Card],
  mind : Option[Card],
  power : Option[Card])


object Being {
  type State = (Option[Int], Option[Int]) // (Heart casualty, Power Casualty)
}

object Star {
  def apply(id : PlayerId, hand : Seq[Card]) : Star = new Star(id, 25, hand, Seq())
}

case class Star
( id : PlayerId,
  majesty : Int,
  hand : Seq[Card],
  beeings : Seq[Being])

