package gp.leval.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

case class CreateGameFormText(
    createGameFormTitle: String = "Créer une partie",
    createSubmit: String = "Créer"
)

val CreateGameForm = ScalaComponent
  .builder[CreateGameFormText]("HomeMenu")
  .render { $ =>
    val txt = $.props
    <.div(
      <.h1(txt.createGameFormTitle),
      <.form(
        ^.action := "",
        <.button(txt.createSubmit)
      )
    )
  }
  .build
