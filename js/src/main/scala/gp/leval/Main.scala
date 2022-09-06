package gp.leval

import gp.leval.routes.AppRouter

import org.scalajs.dom
import scala.scalajs.js.annotation.JSExport

import gp.leval.core.GameInit
import gp.pixijs.*
import gp.pixijs.Point.*
import gp.leval.core.*

object Main {

  @JSExport
  def main(args: Array[String]): Unit = {

    val game = GameInit(
      List(
        PlayerId(None, "Toto"),
        PlayerId(None, "Titi")
      ),
      Rules(Sinnlos)
    )
    val app = Application(1024, 768)

    val sprite = new Sprite(Texture.from("assets/cards/1_of_clubs.png"))
    sprite.scale = (0.5, 0.5)
    app.stage.addChild(sprite)
    dom.document.getElementById("root-container").append(app.view)

    // AppRouter().renderIntoDOM(dom.document.getElementById("root-container"))
  }

}
