package leval

import leval.core.{Being, C, Club, Diamond, Face, Heart, Jack, King, Numeric, Queen, Spade, Suit}

/**
  * Created by lorilan on 9/1/16.
  */
object TestSamples {
  implicit def numPair2card(p:(Int, Suit)) : C = C(0, Numeric(p._1), p._2)
  implicit def facePair2card(p:(Face, Suit)) : C = C(0, p._1, p._2)

  val child = new Being(1,
    (Jack, Spade),
    Map(Heart -> ((2, Heart)))
  )
  val wizard = new Being(1,
    (Queen, Heart),
    Map(Club -> ((8, Club)),
      Heart -> ((King, Heart)),
      Diamond -> ((1, Diamond))
    ))
  val spectre = new Being(0,
    (King, Spade),
    Map(Club -> ((1, Club)),
      Diamond -> ((6, Diamond)),
      Spade -> ((Jack, Spade))
    ))
  val blackLady = new Being(0,
    (King, Spade),
    Map(Club -> ((3, Club)),
      Diamond -> ((2, Diamond)),
      Spade -> ((Jack, Spade))
    ),
    lover = true)
  val fool = new Being(0,
    (King, Spade),
    Map(Club -> ((1, Club)),
      Heart -> ((6, Heart)),
      Spade -> ((Jack, Spade))
    ),
    hasDrawn = true)
}
