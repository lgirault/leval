package gp.leval.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router.RouterCtl
import gp.leval.routes.AppPage

object HomeMenu {

  case class Text(
      createGame: String = "Créer une partie",
      joinGame: String = "Rejoindre une partie",
      logIn: String = "Connection",
      register: String = "Inscription"
  )

  case class Props(ctrl: RouterCtl[AppPage], text: Text = Text())

  val component = {
    ScalaComponent
      .builder[Props]("HomeMenu")
      .render_P { P =>
        val txt = P.text
        <.menu(
          <.li(txt.createGame, P.ctrl setOnClick AppPage.CreateGameForm),
          <.li(txt.joinGame),
          <.li(txt.logIn)
        )
      }
      .build
  }

  def apply(P: Props) = component(P)
}
