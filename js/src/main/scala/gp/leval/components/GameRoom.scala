package gp.leval.components

import gp.pixijs.Application

import java.util.UUID
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.builder.Lifecycle.RenderScope
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.vdom.{Attr, TopNode}
import org.scalajs.dom.{Element}
import scala.compiletime.uninitialized

object GameRoom:
  // case class State(id: UUID, app: Application)

  case class Props(id: UUID, app: Application)

  // class Backend($ : BackendScope[Props, State]):
  //   var pixiContainer = uninitialized

  def updatePixiContainer(P: Props)(element: TopNode): Unit = {
    element.appendChild(P.app.view)
  }

  val component = ScalaComponent
    .builder[Props]
    .render_P(P =>
      <.div(
        s"Hello world GameRoom ",
        P.id.toString,
        <.div(Attr.UntypedRef(updatePixiContainer(P)))
      )
    )
    .build

  def apply(
      id: UUID,
      app: Application = Application(1024, 768)
  ) =
    component(Props(id, app))
