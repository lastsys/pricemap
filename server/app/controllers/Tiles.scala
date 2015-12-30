package controllers

import java.awt.Color
import java.awt.image.BufferedImage
import java.io._
import java.nio.ByteBuffer
import java.nio.file.{Paths, Files}
import javax.imageio.ImageIO

import booli.Configuration
import org.osgeo.proj4j.ProjCoordinate
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import play.core.parsers.Multipart.FileInfo
import slick.driver.JdbcProfile
import tables.BooliObjectTable

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class Tiles extends Controller with BooliObjectTable with HasDatabaseConfig[JdbcProfile] {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  import driver.api._

  val BooliObjects = TableQuery[BooliObjects]

  def tile(tileZoom: Int, tileX: Int, tileY: Int) = Action.async {
    val tileBytes = {
      val path = s"${Configuration.tileCachePath}/$tileZoom-$tileX-$tileY.png"
      val f = new File(path)
      if (f.exists && !f.isDirectory) {
        println(s"Load cached tile $tileZoom $tileX $tileY")
        val is = new FileInputStream(path)
        val p = Paths.get(path)
        val bytes = Files.readAllBytes(p)
        bytes
      } else {
        println(s"Generating tile $tileZoom $tileX $tileY")
        val bytes = createTile(tileZoom, tileX, tileY)
        val os = new FileOutputStream(path)
        os.write(bytes)
        bytes
      }
    }
    Future { Ok(tileBytes).as("image/png") }
  }

  private def tileExists(tileZoom: Int, tileX: Int, tileY: Int): Boolean = ???

  private def createTile(tileZoom: Int, tileX: Int, tileY: Int): Array[Byte] = {
    val img = new BufferedImage(Configuration.tileSize,
      Configuration.tileSize, BufferedImage.TYPE_INT_ARGB)

    val g2 = img.createGraphics()

    // Compiles to while loop.
    @tailrec def writePixel(x: Int, y: Int): Unit = {
      val price = priceForPosition(tileZoom, tileX, tileY, x, y)
      if (price > 10.0) {
        val intensity = Math.min(price / Configuration.maxScaleSquareMeterPrice, 1.0)
        val color = Color.getHSBColor(
          ((1.0 - intensity) * (240.0 / 360.0)).toFloat, 1.0f, 1.0f)
        g2.setColor(color)
        g2.fillRect(x, y, Configuration.pixelSize, Configuration.pixelSize)
        //        img.setRGB(x, y, c << 16)
      }
      val (nx, ny) = if (x >= Configuration.tileSize - 1)
        (0, y + Configuration.pixelSize) else (x + Configuration.pixelSize, y)
      if (ny >= Configuration.tileSize) return
      writePixel(nx, ny)
    }

    writePixel(0, 0)

    g2.setColor(Color.black)
    val fm = g2.getFontMetrics
    g2.drawString(s"$tileZoom/$tileX/$tileY", 2, fm.getHeight)

    val (lon, lat) = tileLonLatCoordinate(tileZoom, tileX, tileY)
    g2.drawString(s"$lon, $lat", 2, fm.getHeight * 3)

    g2.drawRect(0, 0,
      Configuration.tileSize, Configuration.tileSize)

    g2.dispose()

    val baos = new ByteArrayOutputStream()
    ImageIO.write(img, "png", baos)

    baos.toByteArray
  }

  private def tileLonLatCoordinate(tileZoom: Int,
                                   tileX: Double,
                                   tileY: Double): (Double, Double) = {
    // from http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Scala
    import scala.math._
    val lat = toDegrees(atan(sinh(Pi *
      (1.0 - 2.0 * tileY / (1 << tileZoom)))))
    val lon = tileX / (1 << tileZoom) * 360.0 - 180.0
    (lon, lat)
  }

  private def priceForPosition(tileZoom: Int,
                               tileX: Int,
                               tileY: Int,
                               pixelX: Int,
                               pixelY: Int): Double = {
    // Add fraction in interval [0,1) to upper left corner.
    val (lon, lat) = tileLonLatCoordinate(tileZoom,
      tileX + pixelX.toDouble / Configuration.tileSize.toDouble,
      tileY + pixelY.toDouble / Configuration.tileSize.toDouble)

    val p1 = new ProjCoordinate()
    p1.x = lon
    p1.y = lat
    val p2 = new ProjCoordinate()
    Configuration.proj.transform(p1, p2)
    val (xp, yp) = (p2.x, p2.y)

    val query = BooliObjects.filter { r ⇒
      r.x >= (xp - Configuration.radius) &&
      r.x <= (xp + Configuration.radius) &&
      r.y >= (yp - Configuration.radius) &&
      r.y <= (yp + Configuration.radius)
    }.map { r ⇒
      val dx = r.x - xp
      val dy = r.y - yp
      (r.soldPrice, r.livingArea, r.additionalArea, dx * dx + dy * dy)
    }

    val rows: Seq[(Double, Option[Double], Option[Double], Double)] =
      Await.result(dbConfig.db.run(query.result), 120 seconds)

    if (rows.nonEmpty) {
      val (wxSum: Double, wSum: Double) =
        rows.foldLeft((0.0, 0.0)) {
          (sum: (Double, Double),
           row: (Double, Option[Double], Option[Double], Double)) ⇒
            row._2 match {
              case Some(a1) ⇒
                val k = gaussianKernel(row._4)
                val area = row._3 match {
                  case Some(a2) ⇒ a1 + a2
                  case None ⇒ a1
                }
                val squareMeterPrice = row._1 / area
                if (squareMeterPrice <= Configuration
                  .maxOutlierSquareMeterPrice) {
                  (row._1 / area * k + sum._1,
                    k + sum._2)
                } else sum
              case _ ⇒ sum
            }
        }
      if (wSum >= Configuration.dataDensityLimit) {
        wxSum / wSum
      } else 0.0
    } else 0.0
  }

  private def gaussianKernel(r: Double): Double = {
    val k = Math.exp(-0.5 * r / Configuration.gaussianSpatialEll2)
    k
  }
}
