package gp.leval.components

import monocle.syntax.all._
import gp.leval.core.{PlayerId, CoreRules, Rules, Antares, Helios, Sinnlos}
import gp.leval.network.GameDescription
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.Ajax

object CreateGameForm {

  // controlled component pattern for forms:
  // https://reactjs.org/docs/forms.html#controlled-components
  // add private/public game option
  type State = GameDescription
  val State = GameDescription

  case class Text(
      createGameFormTitle: String = "Créer une partie",
      createSubmit: String = "Créer"
  )

  // https://japgolly.github.io/scalajs-react/#examples/ajax-1
  // or maybe directly open a websocket:
  // https://japgolly.github.io/scalajs-react/#examples/websockets
  // https://scala-js.github.io/scala-js-dom/#dom.Websocket

  private val coreRulesList =
    List(Antares, Helios, Sinnlos)

  class Backend($ : BackendScope[Text, State]) {

    def onChange(e: ReactEventFromInput): Callback = {
      val name = e.target.value
      $.modState(_.focus(_.owner.name).replace(name))
    }
    def handleSelectCoreRules(rule: CoreRules)(e: ReactEventFromInput): Callback =
      e.preventDefaultCB >>
          $.modState(_.focus(_.rules.coreRules).replace(rule))

    def handleSubmit(e: ReactEventFromInput): Callback =
      e.preventDefaultCB >>
        CallbackTo[Unit](println("foo"))

    def render(txt: Text, state: State) = {
      <.div(
        <.h1(txt.createGameFormTitle),
        <.form(
          ^.onSubmit ==> handleSubmit,
          <.input(
            ^.value := state.owner.name,
            ^.onChange ==> onChange
          ),
          <.select(
            coreRulesList.toTagMod(r =>
              <.option(
                ^.value := r,
                ^.selected := state.rules.coreRules == r,
                ^.onClick ==> handleSelectCoreRules(r),
                r.toString
              )
            )

          ),
          <.button(txt.createSubmit)
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Text]("HomeMenu")
    .initialState(State(PlayerId(None, ""), Rules(Sinnlos)))
    .renderBackend[Backend]
    .build

  def apply(props: Text) = component(props)
}
