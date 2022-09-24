package gp.leval.gamescreen


import gp.pixijs.{Container, DisplayObject, Graphics, Text, TextStyle}

def computeXCentered(containerWidth: Double,
    toCenterWidth: Double) = 
      (containerWidth - toCenterWidth) / 2

class Button(msg: String, style: TextStyle, 
      boxWidth: Int= 100,
      boxHeigth: Int = 40) extends Container:
  val text = new Text(msg, style)
  val graphics = new Graphics
  
  graphics.lineStyle(2, 0x0E06A5, 1)
    .beginFill(0xFFFFFF)
    .drawRect(0,0, boxWidth, boxHeigth)
    .endFill()

  text.x = computeXCentered(boxWidth, text.width)
  text.y = 5  
  this.addChild(graphics)
  this.addChild(text)


class DialogBox(title: String, message: String,
      boxWidth: Int= 294,
      boxHeigth: Int = 160):

  def view: DisplayObject =
    Container { root =>


      val style = new TextStyle
      style.fill = "black"
      style.wordWrap = true
      style.wordWrapWidth = boxWidth - 20

      val graphics = new Graphics
      val titleText = new Text(title, style)
      val messageText = new Text(message, style)

      titleText.x = computeXCentered(boxWidth, titleText.width)
      titleText.y = 5
      messageText.x = 10

      graphics.lineStyle(2, 0x0E06A5, 1)
        .beginFill(0xFFFFFF)
        .drawRoundedRect(0,0, boxWidth, boxHeigth, 10)
        .endFill()

      val titleLineY = titleText.height + 10

      graphics.moveTo(0,titleLineY)
        .lineTo(boxWidth,titleLineY)

      messageText.y = titleLineY + 5

      val ok = new Button("Ok", style)
      val cancel = new Button("Cancel", style)

      ok.x = computeXCentered(boxWidth / 2, ok.width)
      cancel.x = boxWidth / 2 + computeXCentered(boxWidth / 2, ok.width)

      ok.y = boxHeigth - ok.height - 10
      cancel.y = boxHeigth - cancel.height - 10

      root.addChild(graphics)
      root.addChild(titleText)
      root.addChild(messageText)
      root.addChild(ok)
      root.addChild(cancel)

    }