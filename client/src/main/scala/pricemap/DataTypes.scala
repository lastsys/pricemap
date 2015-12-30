package pricemap

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global ⇒ g, literal ⇒ L, newInstance ⇒ jsNew}
import scala.scalajs.js.timers.SetTimeoutHandle
import scalaz.State, State._
import org.scalajs.jquery.jQuery

sealed trait ObjectType
case object House extends ObjectType         // 0
case object TownHouse extends ObjectType     // 1
case object Apartment extends ObjectType     // 2
case object HolidayHouse extends ObjectType  // 3
case object Farm extends ObjectType          // 4
case object Land extends ObjectType          // 5

object Util {
  def objectTypeNumber(objectType: ObjectType): Int = objectType match {
    case House ⇒ 0
    case TownHouse ⇒ 1
    case Apartment ⇒ 2
    case HolidayHouse ⇒ 3
    case Farm ⇒ 4
    case Land ⇒ 5
  }
}

case class ApplicationState(objectType: ObjectType,
                            currentMap: Int,
                            lastMap: Int,
                            currentDate: js.Date,
                            canvas: Option[dom.html.Canvas] = None,
                            canvasDelta: Option[js.Dynamic] = None,
                            loadCount: Int = 0,
                            loadTimer: Option[SetTimeoutHandle] = None)
