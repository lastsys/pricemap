package pricemap

import org.scalajs.dom
import org.scalajs.jquery._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global ⇒ g, literal ⇒ L, newInstance ⇒ jsNew}
import scalaz.State._
import scalaz._

object Controller {
  def initCanvas: State[ApplicationState, dom.html.Canvas] = for {
    _ ← modify((s: ApplicationState) ⇒ s.canvas match {
      case None ⇒ s
      case _ ⇒ s
    })
    s ← get
  } yield s.canvas.get

  def increaseLoadCount: State[ApplicationState, Int] = for {
    _ ← modify {
      (s: ApplicationState) ⇒ s.copy(loadCount = s.loadCount + 1)
    }
    _ ← modify {
      (s: ApplicationState) ⇒ if (s.loadCount == 1) {
        val timer = js.timers.setTimeout(Configuration.loadTimeout) {
          jQuery("#loading").removeClass("hidden")
        }
        s.copy(loadTimer = Some(timer))
      } else s
    }
    s ← get
  } yield s.loadCount

  def decreaseLoadCount: State[ApplicationState, Int] = for {
    _ ← modify {
      (s: ApplicationState) ⇒
        s.copy(loadCount = s.loadCount - 1)
    }
    _ ← modify {
      (s: ApplicationState) ⇒
        if (s.loadCount == 0) {
          jQuery("#loading").addClass("hidden")
          s.copy(loadTimer = s.loadTimer match {
            case Some(timer) ⇒
              js.timers.clearTimeout(timer)
              None
            case _ ⇒ None
          })
        } else s
    }
    s ← get
  } yield s.loadCount

  def drawCanvas: State[ApplicationState, Unit] = for {
    _ ← modify {
      (s: ApplicationState) ⇒
        val limit = 5.0
        val pixelRatio = g.window.devicePixelRatio

        s
    }
  } yield ()

  def loadData: State[ApplicationState, Unit] = for {
    url ← State[ApplicationState, String] {
      (s: ApplicationState) ⇒
        val url = s"/data/grid?location=${s.currentMap}&" +
          s"date=${s.currentDate.getFullYear()}-" +
          s"${s.currentDate.getMonth()}-" +
          s"${s.currentDate.getDay()}&" +
          s"type=${Util.objectTypeNumber(s.objectType)}"
        (s, url)
    }
  } yield ()

}
