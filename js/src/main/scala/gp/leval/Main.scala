package gp.leval

//import gp.leval.components.*
import gp.leval.routes.AppRouter

import org.scalajs.dom
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

import scala.scalajs.js.annotation.JSExport
val HelloMessage = ScalaComponent
  .builder[String]
  .render($ => <.div("Hello ", $.props))
  .build

object Main {

  @JSExport
  def main(args: Array[String]): Unit = {

    // HelloMessage("John").renderIntoDOM(dom.document.getElementById("root-container"))
    // HomeMenu(HomeMenuText()).renderIntoDOM(dom.document.getElementById("root-container"))
    AppRouter().renderIntoDOM(dom.document.getElementById("root-container"))
  }

}
