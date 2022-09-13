package gp.leval

import gp.leval.core.{
  Being,
  Card,
  Rank,
  Suit
}
import gp.leval.core.Card.given

/** Created by lorilan on 9/1/16.
  */
object TestSamples {


  val child = new Being(1, (Rank.Jack, Suit.Spade), Map(Suit.Heart -> ((2, Suit.Heart))))
  val wizard = new Being(
    1,
    (Rank.Queen, Suit.Heart),
    Map(
      Suit.Club -> ((8, Suit.Club)),
      Suit.Heart -> ((Rank.King, Suit.Heart)),
      Suit.Diamond -> ((1, Suit.Diamond))
    )
  )
  val spectre = new Being(
    0,
    (Rank.King, Suit.Spade),
    Map(
      Suit.Club -> ((1, Suit.Club)),
      Suit.Diamond -> ((6, Suit.Diamond)),
      Suit.Spade -> ((Rank.Jack, Suit.Spade))
    )
  )
  val blackLady = new Being(
    0,
    (Rank.Queen, Suit.Spade),
    Map(
      Suit.Club -> ((3, Suit.Club)),
      Suit.Diamond -> ((3, Suit.Diamond)),
      Suit.Spade -> ((Rank.Jack, Suit.Spade))
    ),
    lovedOne = Some((Rank.King, Suit.Spade))
  )

  val loverAcomplished = new Being(
    1,
    (Rank.King, Suit.Spade),
    Map(
      Suit.Club -> ((3, Suit.Club)),
      Suit.Heart -> ((Rank.Queen, Suit.Spade)),
      Suit.Diamond -> ((3, Suit.Diamond)),
      Suit.Spade -> ((Rank.Jack, Suit.Spade))
    ),
    lovedOne = Some((Rank.Queen, Suit.Spade))
  )
  val acomplished = new Being(
    1,
    (Rank.King, Suit.Spade),
    Map(
      Suit.Club -> ((3, Suit.Club)),
      Suit.Heart -> ((3, Suit.Heart)),
      Suit.Diamond -> ((3, Suit.Diamond)),
      Suit.Spade -> ((Rank.Jack, Suit.Spade))
    )
  )
  val fool = new Being(
    0,
    (Rank.King, Suit.Spade),
    Map(Suit.Club -> ((1, Suit.Club)), Suit.Heart -> ((6, Suit.Heart)), Suit.Spade -> ((Rank.Jack, Suit.Spade))),
    hasAlreadyDrawn = true
  )

  val knight = new Being(
    0,
    (Rank.Jack, Suit.Club),
    Map(
      Suit.Diamond -> ((2, Suit.Diamond)),
      Suit.Heart -> ((4, Suit.Heart)),
      Suit.Spade -> ((6, Suit.Spade))
    ),
    hasAlreadyDrawn = true
  )

}
