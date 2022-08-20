package gp.leval.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.Ajax

object CreateGameForm {

  type CoreRules = "sinnlos" | "antares" | "helios"

  // controlled component pattern for forms:
  // https://reactjs.org/docs/forms.html#controlled-components
  // add private/public game option
  case class State(
      coreRules: CoreRules,
      ostein: Boolean = false,
      allowMulligan: Boolean = false,
      nedemone: Boolean = false,
      janus: Boolean = false
  )

  case class Text(
      createGameFormTitle: String = "Créer une partie",
      createSubmit: String = "Créer"
  )

  // https://japgolly.github.io/scalajs-react/#examples/ajax-1
  // or maybe directly open a websocket:
  // https://japgolly.github.io/scalajs-react/#examples/websockets
  // https://scala-js.github.io/scala-js-dom/#dom.Websocket

  def handleSubmit(e: ReactEventFromInput): CallbackTo[Unit] =
    e.preventDefaultCB >>
      CallbackTo[Unit](println("foo"))

  class Backend($ : BackendScope[Text, State]) {

    def render(txt: Text, state: State) = {
      <.div(
        <.h1(txt.createGameFormTitle),
        <.form(
          ^.onSubmit ==> handleSubmit,
          <.button(txt.createSubmit)
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Text]("HomeMenu")
    .initialState(State("sinnlos"))
    .renderBackend[Backend]
    .build

  def apply(props: Text) = component(props)
}
