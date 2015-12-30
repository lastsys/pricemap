package controllers

import booli.{Configuration, Download}
import play.api.mvc._


class Application extends Controller {

  def index = Action {
    Ok(views.html.Application.main())
  }

  def admin() = Action {
    val key = Configuration.adminKey
    if (key == Configuration.adminKey) {
      Ok(views.html.Application.admin())
    } else NotFound
  }

  def adminDownload() = Action {
    val key = Configuration.adminKey
    if (key == Configuration.adminKey) {
      Download.downloadBooliData()
      Ok("Downloading Booli Data")
    } else NotFound
  }
}
