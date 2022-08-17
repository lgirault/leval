package gp.leval
import org.scalajs.dom
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.js.annotation.JSExport
val HelloMessage = ScalaComponent.builder[String]
  .render($ => <.div("Hello ", $.props))
  .build



object Main {

  @JSExport
  def main(args: Array[String]): Unit = {
    HelloMessage("John").renderIntoDOM(dom.document.getElementById("root-container"))
  }

}
