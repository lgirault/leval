package leval

import java.io.IOException
import javafx.{scene => jfxs}

import akka.actor.ActorSystem

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafxml.core._

/**
  * Created by lorilan on 9/27/16.
  */

import java.net.URL
import javafx.{fxml => jfxf}
import javafx.{util => jfxu}

class FXMLLoader(fxml: URL, dependencies: ControllerDependencyResolver)
  extends jfxf.FXMLLoader(
    fxml,
    null,
    new jfxf.JavaFXBuilderFactory(),
    new jfxu.Callback[Class[_], Object] {
      override def call(cls: Class[_]): Object = {
        println("call " + cls.getName)
        FxmlProxyGenerator(cls, dependencies)
      }
    }) {

  override def getController[T](): T = super.getController[ControllerAccessor].as[T]
}

object TestChatPane extends JFXApp  {

  implicit val system = ActorSystem()
  //  val _ = system.actorOf(ControlerMockup.props(game))



  val resource = getClass.getResource("/fxml/TabView.fxml")
  if (resource == null) {
    throw new IOException("Cannot load resource: TabView.fxml")
  }

  val loader =
    new FXMLLoader(resource,
      new ControllerDependencyResolver() {
    override def get(paramName: String, dependencyType: scala.reflect.runtime.universe.Type): Option[Any] = {
      println("get " + paramName)
      None
    }

  })

  loader.load()

//  val control : MenuController = loader.getController()
//  control.system = system
//  control.majorVersion = 42
//  control.minorVersion = 42

  val stageScene : Scene = new Scene(800, 600) {
    root = loader.getRoot[jfxs.Parent]()
  }


  val config = LevalConfig.default
  import LevalConfig._
  stage = new JFXApp.PrimaryStage {
    title = "Test"

    implicit val txt = config.lang()
    scene = stageScene


  }



}