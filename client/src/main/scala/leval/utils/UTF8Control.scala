package leval.utils

import java.io.{InputStream, InputStreamReader}
import java.util.ResourceBundle.Control
import java.util.{Locale, PropertyResourceBundle, ResourceBundle}

/**
  * Created by lorilan on 10/1/16.
  */
object UTF8Control extends Control {
  override def newBundle(baseName: String,
                         locale: Locale,
                         format: String,
                         loader: ClassLoader,
                         reload: Boolean): ResourceBundle = {
      // The below is a copy of the default implementation.
    val bundleName = toBundleName(baseName, locale)
    val resourceName = toResourceName(bundleName, "properties")

    var stream : InputStream = null
    if (reload) {
      Option(loader getResource resourceName) foreach {
        url ⇒
          Option(url.openConnection()) foreach {
            connection ⇒
              connection.setUseCaches(false)
              stream = connection.getInputStream()
          }
      }
    } else {
      stream = loader.getResourceAsStream(resourceName)
    }
    Option(stream) match {
      case None ⇒ null
      case Some(_) ⇒
      try {
        // Only this line is changed to make it to read properties files as UTF-8.
        new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
      } finally {
        stream.close()
      }
    }
  }
}
