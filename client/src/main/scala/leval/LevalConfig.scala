package leval

import java.io.{File, FileWriter}

import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions, ConfigValueFactory}
import leval.core.{Antares, Helios, Rules, Sinnlos}
import leval.gui.text.{Eng, Fr, ValText}

/**
  * Created by lorilan on 9/10/16.
  */
object LevalConfig {

  def file : File = {
    val rootPath = System getProperty "user.home"
    val dirPath = rootPath + File.separator + "leval"

    val dir = new File( dirPath )
    if(!dir.exists())
        dir.mkdirs()

    new File( dirPath + File.separator + "config.txt")
  }

  private val renderOpts =
    ConfigRenderOptions.defaults()
      .setOriginComments(false)
      .setComments(false)
      .setJson(false)

  def load() : Config =
    if(!file.exists()){
      save(default)
      default
    }
    else
      ConfigFactory parseFile file withFallback default


  def save(cfg : Config) = {
    val fw = new FileWriter(file)
    fw write cfg.root().render(renderOpts)
    fw.close()
  }
  object Keys{
    val screenRatio = "leval.client.graphics.screenRatio"
    val lang = "leval.client.lang"
    val defaultRules = "leval.client.rules.default"
    val minorVersion = "leval.client.version.minor"
    val majorVersion = "leval.client.version.major"
    val login = "leval.client.login"
  }

  import ConfigValueFactory.fromAnyRef
  val default =
    ConfigFactory.empty()
      .withValue(Keys.screenRatio, fromAnyRef("16:9"))
      .withValue(Keys.lang, fromAnyRef("fr"))
      .withValue(Keys.defaultRules, fromAnyRef("sinnlos"))
      .withValue(Keys.login, fromAnyRef(""))


  implicit class ConfigOps(val cfg : Config) extends AnyVal {
    def lang() : ValText =
      cfg getString Keys.lang match {
        case "fr" => Fr
        case "eng" => Eng
        case _ => leval.error("unknown language")
      }
    def screenRatio() : (Double, Double)= {
      val strRatio = cfg getString Keys.screenRatio
      val arr = strRatio.split(':')
      (arr(0).toDouble, arr(1).toDouble)
    }

    def rules() : Rules =
      cfg getString Keys.defaultRules match {
        case "sinnlos" => Sinnlos
        case "antares" => Antares
        case "helios" => Helios
        case _ => leval.error("unknown rules")
      }

    def withAnyRefValue(path : String, o : Object) =
      cfg.withValue(path, ConfigValueFactory.fromAnyRef(o))
  }

}
