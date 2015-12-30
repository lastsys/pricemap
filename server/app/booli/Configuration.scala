package booli

import org.osgeo.proj4j.{CRSFactory, CoordinateTransformFactory}

object Configuration {
  val user = sys.env("pricemap_booli_user") //"lastsys"
  val token = sys.env("pricemap_booli_token")
  //wFtevfqYYMVXFyeqgLRsZHAdDkCIOlilHbnxhGVk"
  val url = "https://api.booli.se/sold"
  val pageSize = 500
//  val outputPath = "C:/Users/Stefan/Documents/Projects/booli2-data"
  val outputPath = sys.env("pricemap_output_path") //"booli2-data"
  val tileCachePath = s"$outputPath/tiles"
  val tileSize = 256

  val gaussianSpatialEll = 250.0
  val gaussianDateEll = 90.0
  val gaussianSpatialEll2 = gaussianSpatialEll * gaussianSpatialEll
  val radius = gaussianSpatialEll * 4.0
  val squaredRadius = radius * radius
  val pixelSize = 4
  val dataDensityLimit = 1.5
  val maxScaleSquareMeterPrice = 100000.0
  val maxOutlierSquareMeterPrice = 300000.0

  val adminKey = sys.env("pricemap_admin_key")
  //5dea4dfb-0ff0-4601-a16a-1659ed35be12"

  val proj = {
    val ctFactory = new CoordinateTransformFactory()
    val csFactory = new CRSFactory()
    val crs = csFactory.createFromName("EPSG:4326") // OSM
    val crt = csFactory.createFromName("EPSG:3006") // SWEREF99
    ctFactory.createTransform(crs, crt)
  }
}
