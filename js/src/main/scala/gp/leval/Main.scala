package gp.leval

import gp.leval.routes.AppRouter
import gp.leval.gamescreen.*

import org.scalajs.dom
import cats.implicits.*
import scala.scalajs.js.annotation.JSExport

import gp.leval.core.GameInit
import gp.leval.core.Card.given
import gp.leval.core.Suit
import gp.leval.text.{ValText, Fr}
import gp.pixijs.*
import gp.pixijs.Point.*
import gp.leval.core.*
import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {

  val run: IO[Unit] = {

      val toto = PlayerId(None, "Toto")
      for {
        game <- GameInit[IO](
          List(toto,
            PlayerId(None, "Titi")
          ),
          Rules(Sinnlos)
        )
        (textures @ given TextureDictionary) <- TextureDictionary.load[IO]
      } yield {

        val (height,width) = (1024, 768)
        val app = Application(height, width)

        val dbox = new DialogBox("toto", "0 + 0 = la tête à toto").view
        dbox.x = 300
        dbox.y = 150
        
        given ValText = Fr
        val gameView = new TwoPlayerGameScreen(height, width)(game.doTwilight.game, toto)
        app.stage.addChild(gameView.view)
        app.stage.addChild(dbox)
      


        dom.document.getElementById("root-container").append(app.view)
      
        //AppRouter().renderIntoDOM(dom.document.getElementById("root-container"))

      }
      
  }

}
