package gp.leval.components

import java.util.UUID
import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.component.builder.Lifecycle.RenderScope
import japgolly.scalajs.react.vdom.html_<^.*

val gameRoom = ScalaComponent
  .builder[UUID]
  .stateless
  .noBackend
  .render(($ : RenderScope[UUID, Unit, Unit]) =>
    <.div(s"Hello world GameRoom ", $.props.toString)
  )
  .build
