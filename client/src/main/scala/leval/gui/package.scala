package leval

import scalafx.scene.image.{Image, ImageView}

/**
  * Created by lorilan on 8/28/16.
  */
package object gui {
  val logoWidth = 287d
  val logoUrl = this.getClass.getResource("/logoval.png").toExternalForm
  val logo = new Image(logoUrl)
  def logoImage(width : Double = logoWidth) =
    new ImageView(logo) {
      preserveRatio = true
      fitWidth = width
    }
}
