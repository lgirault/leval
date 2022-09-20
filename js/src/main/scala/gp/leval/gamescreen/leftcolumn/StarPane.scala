package gp.leval.gamescreen.leftcolumn

import gp.leval.core.Star
import gp.leval.text.ValText
import gp.pixijs.{Container, DisplayObject, Text}
import gp.pixijs.TextStyle

class StarPane(star: Star)(using txt : ValText):

  def view: DisplayObject =
    Container { root =>
      val text =
         s"""${star.id.name}
         |${txt.majesty}
         |${star.majesty}""".stripMargin

      //println(text)
      val style = new TextStyle
      style.fill = "red"
      root.addChild(new Text(text, style))

    }