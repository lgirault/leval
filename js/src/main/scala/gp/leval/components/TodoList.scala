package gp.leval.components

import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.extra.Ajax

val TodoList = ScalaFnComponent[List[String]] { props =>
  def createItem(itemText: String) = <.li(itemText)
  <.ul(props.map(createItem)*)
}

case class State(items: List[String], text: String)

class Backend($ : BackendScope[Unit, State]) {
  def onChange(e: ReactEventFromInput) = {
    val newValue = e.target.value
    $.modState(_.copy(text = newValue))
  }

  def handleSubmit(e: ReactEventFromInput): CallbackTo[Unit] =
    e.preventDefaultCB >>
      $.modState(s => State(s.items :+ s.text, ""))

  def render(state: State) =
    <.div(
      <.h3("TODO"),
      TodoList(state.items),
      <.form(
        ^.onSubmit ==> handleSubmit,
        <.input(^.onChange ==> onChange, ^.value := state.text),
        <.button("Add #", state.items.length + 1)
      )
    )
}

val TodoApp = ScalaComponent
  .builder[Unit]
  .initialState(State(Nil, ""))
  .renderBackend[Backend]
  .build

//TodoApp().renderIntoDOM(mountNode)
