package pricemap

import org.scalajs.dom

import scala.scalajs.js

object Boot extends js.JSApp {
  var state = ApplicationState(
    objectType = Apartment,
    currentMap = 0,
    lastMap = 0,
    currentDate = new js.Date(2015, 11))

  def initialize(): Unit = {
    dom.document.body.addEventListener("contextmenu",
      (e: dom.Event) ⇒ e.preventDefault())
  }

  def main(): Unit = {
    initialize()
  }
}
