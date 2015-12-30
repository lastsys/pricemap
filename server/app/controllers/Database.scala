package controllers

import java.io._

import booli.{BooliJson, Configuration}
import models.BooliObject
import org.osgeo.proj4j.ProjCoordinate
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._
import slick.driver.JdbcProfile
import tables.BooliObjectTable

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class Database extends Controller with BooliObjectTable with HasDatabaseConfig[JdbcProfile] {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._

  val BooliObjects = TableQuery[BooliObjects]

  def rebuildDatabase() = Action {
    val key = Configuration.adminKey
    if (key != Configuration.adminKey) {
      NotFound
    } else {
      val initResult = dbConfig.db.run(DBIO.seq(
        BooliObjects.schema.drop,
        BooliObjects.schema.create)).map(_ ⇒ ())

      initResult.onFailure {
        case f ⇒ f.printStackTrace()
      }

      BooliObjects.schema.create.statements.foreach(println)

      jsonFiles.foreach { f ⇒
        val page = processJsonFile(f)
        page.sold.foreach { s ⇒
          val (x, y) = {
            val p1 = new ProjCoordinate()
            p1.x = s.location.position.longitude
            p1.y = s.location.position.latitude
            val p2 = new ProjCoordinate()
            Configuration.proj.transform(p1, p2)
            (p2.x, p2.y)
          }

          val dbObj = BooliObject(s.booliId,
            s.location.position.longitude,
            s.location.position.latitude,
            x, y,
            s.location.distance match {
              case Some(d) ⇒ Some(d.ocean)
              case None ⇒ None
            },
            s.location.position.isApproximate,
            s.location.address.streetAddress,
            s.listPrice,
            s.floor,
            s.livingArea,
            s.additionalArea,
            s.rooms,
            java.sql.Date.valueOf(s.published.toLocalDate),
            s.constructionYear match {
              case Some(c) ⇒ Some(c.getValue)
              case None ⇒ None
            },
            s.objectType,
            java.sql.Date.valueOf(s.soldDate),
              s.soldPrice,
            s.url,
            s.location.region.municipalityName
          )

          val result = dbConfig.db.run(BooliObjects += dbObj)
          result.onFailure {
            case f ⇒ f.printStackTrace()
          }
          Await.ready(result, 10 minutes)
        }
      }
      Ok("Rebuilding Database")
    }
  }

  def tryQuery() = Action.async {
    val key = Configuration.adminKey
    if (key != Configuration.adminKey) {
      Future(NotFound)
    } else {
      val qx = 676418.467701669
      val qy = 6582622.20140253
      val radius = 2000.0
      val qxMin = qx - radius
      val qxMax = qx + radius
      val qyMin = qy - radius
      val qyMax = qy + radius

      val query = BooliObjects
        .filter(row ⇒
          row.x <= qxMax && row.x >= qxMin &&
            row.y <= qyMax && row.y >= qxMax)
        .map(r ⇒ (r.x, r.y, r.soldPrice))

      println("Run query")
      for {
        rows ← dbConfig.db.run(query.result)
      } yield {
        println("Process rows")
        val calcRows = rows.map { row ⇒
          (distance(qx, qy, row._1, row._2), row._3)
        }.filter {
          case row ⇒ row._1 < radius
        }
        println("Ready")
        Ok(s"Hello ${calcRows.size}")
      }
    }
  }

  private def distance(x1: Double, y1: Double,
                       x2: Double, y2: Double): Double =
    Math.sqrt(Math.pow(x1 - x2, 2.0) + Math.pow(y1 - y2, 2.0))

  private def processJsonFile(filename: String): BooliJson.Page = {
    import BooliJson._

    val fullPath = s"${Configuration.outputPath}/$filename"
    println(s"Loading $fullPath")
    val text = scala.io.Source.fromFile(fullPath, "utf-8").getLines.mkString
    Json.parse(text).as[BooliJson.Page]
  }

  private def jsonFiles: Seq[String] = {
    val dir = new File(Configuration.outputPath)
    dir.listFiles.filter {
      case f ⇒ f.getName.endsWith(".json")
    }.map { f ⇒ f.getName }
  }
}
