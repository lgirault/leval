package leval.gui

import javafx.beans.value.{ChangeListener, ObservableValue}

import scalafx.scene.Scene
import scalafx.scene.layout.Pane

/**
  * Created by lorilan on 8/17/16.
  */
trait SceneSizeChangeListener extends ChangeListener[Number] {

  def scene: Scene
  def pane: Pane
  val widthRatio: Double
  val heightRatio: Double

  lazy val scaleFactor = widthRatio / heightRatio
  lazy val reversedScaleFactor = heightRatio / widthRatio
//  val scale: Scale = new Scale(scaleFactor, scaleFactor)
//  scale.setPivotX(0)
//  scale.setPivotY(0)

  def contentPaneDimention() : (Double, Double) = {
    val w : Double = scene.width()
    val h : Double = scene.height()
    //print(s"sceneWidth $w, sceneHeight $h -> ")

    val newWidth = h * scaleFactor
    val newHeight = w * reversedScaleFactor
    //println(s"newWidth = $newWidth, newHeight = $newHeight")
    if(newWidth > w)//too wide
      (w, newHeight)
    else
      (newWidth, h)
  }

  def changed(observableValue: ObservableValue[_ <: Number],
              oldValue: Number, newValue: Number) : Unit =  {
    val (newWidth, newHeight) = contentPaneDimention()
    println(s"newWidth = $newWidth, newHeight = $newHeight")

    //    scene.getRoot.getTransforms.setAll(scale)
    pane.setPrefWidth(newWidth)
    pane.setPrefHeight(newHeight)
  }
}
