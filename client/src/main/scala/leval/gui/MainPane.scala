package leval.gui

import scala.reflect.runtime.universe.typeOf
import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.Config
import leval.control.CreateGameControl
import leval.core.PlayerId

import leval.network.{ChangeLangListener, MenuActor}

import scalafx.Includes._
import scalafx.scene.control._
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.Node
import scalafx.scene.layout.{AnchorPane, Pane}
import scalafx.scene.text.Text
import scalafx.scene.web.{HTMLEditor, WebView}
import scalafxml.core.macros.sfxml

/**
  * Code adapted from <a href="https://github.com/frtj/javafx_examples">github.com/frtj/javafx_examples</a>
  *
  * @author Eric Zoerner <a href="mailto:eric.zoerner@gmail.com">eric.zoerner@gmail.com</a>
  */
object Browser {
  private val ContentId = "browser_content"
  def getHtml(content: String): String = {
    "<html><body>" + "<div id=\"" + ContentId + "\">" + content + "</div>" + "</body></html>"
  }
}
import Browser._
class Browser(content: String) extends Pane {
  val webView = new WebView()
  val webEngine = webView.engine

  webView.prefHeight = 5
  padding = Insets(20)

  width onChange {
    (observable, oldValue, newValue) ⇒
      val width = newValue.asInstanceOf[Double]
      webView.setPrefWidth(width)
      adjustHeight()

  }

//    webEngine.getLoadWorker.stateProperty.addListener(new ChangeListener[State] {
//      override def changed(arg0: ObservableValue[_ <: State], oldState: State, newState: State): Unit =
//        if (newState == State.SUCCEEDED)
//          adjustHeight()
//    })
  //
  //  // http://stackoverflow.com/questions/11206942/how-to-hide-scrollbars-in-the-javafx-webview
  //  webView.getChildrenUnmodifiable.addListener(new ListChangeListener[Node] {
  //    def onChanged(change: ListChangeListener.Change[_ <: Node]) = {
  //      val scrolls: util.Set[Node] = webView.lookupAll(".scroll-bar")
  //      for (scroll ← scrolls.asScala) {
  //        scroll.setVisible(false)
  //      }
  //    }
  //  })

  setContent(content)

  children = webView

  def setContent(content: String) = {
    Platform.runLater {
      webEngine.loadContent(getHtml(content))
      Platform.runLater(adjustHeight())
    }
  }
  //
  //  protected override def layoutChildren() = {
  //    val w: Double = getWidth
  //    val h: Double = getHeight
  //    layoutInArea(webView, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER)
  //  }

  private def adjustHeight(): Unit = {
    Platform.runLater {
      val result: Any = webEngine.executeScript("var e = document.getElementById('" + ContentId + "');" +
        "e ? e.offsetHeight : null")
      result match {
        case i: Integer ⇒
          var height = i.toDouble
          height = height + 20
          webView.setPrefHeight(height)
        case _ ⇒
      }
    }
  }
}

/**
  * Created by lorilan on 9/26/16.
  */
@sfxml
class ChatController(private val onlineTitledPane: TitledPane,
                     private val accordion: Accordion,
                     private val actorSystem: ActorSystem,
                     private val usernameText: Text,
                     private val username: String,
                     private val onlineListView: ListView[String],
                     private val chatEditor: HTMLEditor,
                     private val sendChat: Button,
                     private val webViewParent: AnchorPane,
                     private val chatScrollPane: ScrollPane,
                     private val sendOnEnter: CheckBox){
  println("creating chat controller")
}

//class ChatPane
//( network : MenuController
//) extends SplitPane {
//  orientation = Orientation.Vertical
//  dividerPositions = 0.80
//
//  val reg1 = new BorderPane {
//    center = new Browser("dadada<br/>dodod")
//  }
//
//  items ++= Seq(reg1)
//
//}
@sfxml
class TabViewController
(private val actor : ActorRef,
 private val thisPlayer : PlayerId,
 private val mainChatTab : Tab,
 private val mainChatPaneController : ChatController,
 private val createGameTab : Tab,
 private var createGamePaneController : CreateGameControl)
  extends ChangeLangListener {

  def langChanged(config: Config) : Unit = {
    import leval.LevalConfig.ConfigOps
    val texts = config.lang()
    mainChatTab.text = texts.solarSystem
    createGameTab.text = texts.game
    val loader = MenuActor.loadView(
      Map(typeOf[ActorRef] → actor,
      typeOf[Config] → config,
      typeOf[PlayerId] → thisPlayer), config, "CreateGameView")
    createGameTab.content = loader.getRoot[Node]
    createGamePaneController = loader.getController()
  }
}
