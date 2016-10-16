package leval.network

import java.io.IOException

import scala.reflect.runtime.universe.{typeOf, Type}
import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.config.Config
import leval.LevalConfig
import leval.LevalConfig._
import leval.core.{PlayerId, Rules}
import leval.gui.{TabViewController, WaitingRoom}
import leval.gui.gameScreen.{GameScreenControl, ObservableGame}
import leval.gui.text.ValText

import scalafx.scene.Scene
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafxml.core.{DependenciesByType, FXMLLoader}

case class CreateRequest(rules: Rules)
case object StartScreen
object ConfigChange {
  def apply(key : String, value : String) : ConfigChange =
    new ConfigChange(List((key,value)))
}
case class ConfigChange(keyValues : List[(String, String)])

trait LangChanger {
  private [this] var changeLangListeners = List[ChangeLangListener]()

  def suscribe(changeLangListener: ChangeLangListener): Unit =
    changeLangListeners ::= changeLangListener

  def unsuscribe(changeLangListener: ChangeLangListener): Unit =
    changeLangListeners = changeLangListeners filterNot (_ eq changeLangListener)

  def updateLangChange(config: Config) =
    changeLangListeners foreach (_.langChanged(config))
}


trait ChangeLangListener {
  def langChanged(changconfig: Config) : Unit
}

object MenuActor {
  def props(scene : Scene,
            serverRef : ActorRef) =
    Props(new MenuActor(scene, serverRef))
      .withDispatcher("javafx-dispatcher")

  def loadView(deps: Map[Type, Any],
               config : Config,
               view : String) : FXMLLoader = {
    val resource = getClass.getResource(s"/fxml/$view.fxml")
    if (resource == null) {
      throw new IOException(s"Cannot load resource: $view.fxml")
    }

    val dependencies = new DependenciesByType(deps)


    val loader = new FXMLLoader(resource, dependencies)
    loader setResources config.lang().resources
    loader
  }
}

class MenuActor private
( scene : Scene,
  serverRef : ActorRef)
  extends Actor
    with ListingActor
    with InGameActor
    with Drafting
    with WaitinPlayers
    with LangChanger {


  var config0 = LevalConfig.load()
  def config = config0
  def config_=(cfg : Config) = {
    config0 = cfg
    LevalConfig.save(cfg)
  }



  implicit def texts : ValText = config.lang()


  def loadView(view : String) : FXMLLoader = {
    val loader = MenuActor.loadView(
      Map(typeOf[ActorRef] → self,
      typeOf[Config] → config,
      typeOf[PlayerId] → thisPlayer),
      config, view)
    scene.delegate setRoot loader.load()
    loader
  }


  loadView("LoginView")

  private var tabController : Option[TabViewController] = None
  def displayStartScreen() : Unit = {
    val loader = loadView("TabView")
    tabController = Some(loader.getController())
  }


  def waitingOtherPlayerScreen
  ( maker : PlayerId, rules : Rules ) : WaitingRoom = {
    val wr =  new WaitingRoom(this, maker.name, rules)
    scene.root = wr
    wr
  }

  def gameScreen(game : ObservableGame) : GameScreenControl = {
    val pidx = game.stars.indexWhere(_.id == thisPlayer)
    new GameScreenControl(scene, game, pidx, self, ???/*config*/)
  }

  def gameListScreen()  = {
    val loader = loadView("GameListView")
    this suscribe loader.getController[ListingListener]()
  }

  def connectError(msg : String) : Unit = {
    new Alert(AlertType.Confirmation) {
      delegate.initOwner(scene.getWindow)
      title = "Erreur de connection"
      headerText = msg
    }.showAndWait()
    exit()
  }



  var thisPlayer : PlayerId = _


  def changeConfig(kvs : List[(String, String)]) = {
    val newCfg = kvs.foldLeft(config){
      case (cfg, (key, value)) =>
        cfg.withAnyRefValue(key, value)
    }
    val changeLang = newCfg.lang() != config.lang()
    config = newCfg

    if(changeLang)
      updateLangChange(newCfg)

  }

  def exit() : Unit = {
    if(thisPlayer != null)
      serverRef ! Disconnect(thisPlayer)

    println("Shutting down !!")
    context.system.terminate()
    println("Bye bye !!")
    System.exit(0)
  }

  def receive : Actor.Receive = {

    case ConnectAck(pid) ⇒
      thisPlayer = pid
      leval.ignore(displayStartScreen())

    case ConnectNack(msg) ⇒
      leval.ignore(connectError(msg))

    case ListGame ⇒
      context.become( listing(serverRef) )
      serverRef ! ListGame

    case CreateRequest(rules) ⇒
      serverRef ! CreateGame(GameDescription(thisPlayer, rules))

    case CreateGameAck(GameDescription(creator, rules)) ⇒
      val waitingScreen = waitingOtherPlayerScreen(creator, rules)
      context.become(waitingPlayers( sender(), waitingScreen, creator ) )

    case StartScreen ⇒ ()
    case ConfigChange(kvs) ⇒ changeConfig(kvs)

    case req : Message ⇒ serverRef ! req

  }

}


