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
    (Queen, Spade),
    Map(Club -> ((3, Club)),
      Diamond -> ((3, Diamond)),
      Spade -> ((Jack, Spade))
    ),
    lover = true)

  val loverAcomplished = new Being(1,
    (King, Spade),
    Map(Club -> ((3, Club)),
      Heart ->((Queen, Spade)),
      Diamond -> ((3, Diamond)),
      Spade -> ((Jack, Spade))
    ),
    lover = true)
  val acomplished = new Being(1,
    (King, Spade),
    Map(Club -> ((3, Club)),
      Heart ->((3, Heart)),
      Diamond -> ((3, Diamond)),
      Spade -> ((Jack, Spade))
    ))
  val fool = new Being(0,
    (King, Spade),
    Map(Club -> ((1, Club)),
      Heart -> ((6, Heart)),
      Spade -> ((Jack, Spade))
    ),
    hasAlreadyDrawn = true)

  val knight = new Being(0,
    (Jack, Club),
    Map(Diamond -> ((2, Diamond)),
      Heart -> ((4, Heart)),
      Spade -> ((6, Spade))
    ),
    hasAlreadyDrawn = true)


}
