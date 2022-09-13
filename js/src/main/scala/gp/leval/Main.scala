package gp.leval

import gp.leval.routes.AppRouter
import gp.leval.gamescreen.TextureDictionary

import org.scalajs.dom
import cats.implicits.*
import scala.scalajs.js.annotation.JSExport

import gp.leval.core.GameInit
import gp.leval.core.Card.given
import gp.leval.core.Suit
import gp.pixijs.*
import gp.pixijs.Point.*
import gp.leval.core.*
import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {

  val run: IO[Unit] = {

      for {
        game <- GameInit[IO](
          List(
            PlayerId(None, "Toto"),
            PlayerId(None, "Titi")
          ),
          Rules(Sinnlos)
        )
        textures <- TextureDictionary.load[IO]
      } yield {

        println(game)
        val app = Application(1024, 768)

        //val sprite =  new Sprite(Texture.from("assets/cards/1_of_clubs.png"))
        val sprite =  textures.sprite((1, Suit.Club))
        sprite.scale = (0.5, 0.5)
        app.stage.addChild(sprite)
        dom.document.getElementById("root-container").append(app.view)

        // AppRouter().renderIntoDOM(dom.document.getElementById("root-container"))

      }
      
  }

}
