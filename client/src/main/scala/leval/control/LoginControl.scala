package leval.control

import akka.actor.ActorRef
import com.typesafe.config.{Config, ConfigFactory}
import leval._
import leval.gui.text.ValText
import leval.network.{ConfigChange, GuestConnect}

import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, TextField}
import scalafxml.core.macros.sfxml
import leval.LevalConfig._
/**
  * Created by lorilan on 10/1/16.
  */
@sfxml
class LoginControl
( private val loginTextField : TextField,
  private val actor: ActorRef,
  private val config : Config) {

  val conf = ConfigFactory load "client"
  import LevalConfig.Keys
  val majorVersion = conf getInt Keys.majorVersion
  val minorVersion = conf getInt Keys.minorVersion

  val texts : ValText = config.lang()
  loginTextField.text = config getString Keys.login

  def login = loginTextField.text.value
  def start() =
    if(login.isEmpty)
      ignore(new Alert(AlertType.Information){
        delegate.initOwner(loginTextField.scene().getWindow)
        headerText = texts.empty_login
      }.showAndWait())
    else {
      actor ! ConfigChange(Keys.login, login)
      actor ! GuestConnect(s"$majorVersion.$minorVersion", login)
      //actor ! Connect(s"$majorVersion.$minorVersion", login, passWord)
    }
}
