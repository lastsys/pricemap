package pricemap

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.scalajs.dom.ext.Ajax

@JSExport
object Admin {
  @JSExport
  def download(): Unit = {
    println("Download")
    for {
      xhr ← Ajax(method = "POST",
        url = "/admin/download",
        data = null,
        timeout = 0,
        headers = Map[String, String](),
        withCredentials = false,
        responseType = ""
      )
    } yield {
      println(xhr.responseText)
    }
  }

  @JSExport
  def rebuildDb(): Unit = {
    println("Rebuild Database")
    for {
      xhr ← Ajax(method = "POST",
        url = "/admin/rebuilddb",
        data = null,
        timeout = 0,
        headers = Map[String, String](),
        withCredentials = false,
        responseType = ""
      )
    } yield {
      println(xhr.responseText)
    }
  }

  @JSExport
  def tryQuery(): Unit = {
    println("Trying query")
    for {
      xhr ← Ajax(method = "POST",
        url = "/admin/tryquery",
        data = null,
        timeout = 0,
        headers = Map[String, String](),
        withCredentials = false,
        responseType = ""
      )
    } yield {
      println(xhr.responseText)
    }
  }
}
