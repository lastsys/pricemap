package controllers

import booli.{Configuration, Download}
import play.api.mvc._


class Application extends Controller {

  def index = Action {
    println("Environment")
    println(sys.env.mkString("\n"))
    Ok(views.html.Application.main())
  }

  def admin(key: String) = Action {
    println(Configuration.adminKey)
    if (key == Configuration.adminKey) {
      Ok(views.html.Application.admin())
    } else NotFound
  }

  def adminDownload(key: String) = Action {
    if (key == Configuration.adminKey) {
      Download.downloadBooliData()
      Ok("Downloading Booli Data")
    } else NotFound
  }
}
