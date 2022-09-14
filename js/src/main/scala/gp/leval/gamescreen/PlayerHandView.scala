package gp.leval.gamescreen

import gp.leval.core.Card
import gp.pixijs.{Container, DisplayObject, Point}

class PlayerHandView(hand: Set[Card])(using textures: TextureDictionary):

  def view: DisplayObject =
    Container { root =>
      val width = 100
      hand.toList.zipWithIndex.foreach { case (c, i) =>
        // println(s"$i : $c")
        val s = textures.sprite(c)
        s.scaleForWidth(width)
        root.addChild(s)
        s.x = i * (width / 2)
      }
    }
