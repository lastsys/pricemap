package booli

import org.osgeo.proj4j.{CRSFactory, CoordinateTransformFactory}

object Configuration {
  val user = sys.env("PRICEMAP_BOOLI_USER")
  val token = sys.env("PRICEMAP_BOOLI_TOKEN")
  val url = "https://api.booli.se/sold"
  val pageSize = 500
  val outputPath = sys.env("PRICEMAP_OUTPUT_PATH")
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

  val adminKey = sys.env("PRICEMAP_ADMIN_KEY")

  val proj = {
    val ctFactory = new CoordinateTransformFactory()
    val csFactory = new CRSFactory()
    val crs = csFactory.createFromName("EPSG:4326") // OSM
    val crt = csFactory.createFromName("EPSG:3006") // SWEREF99
    ctFactory.createTransform(crs, crt)
  }
}
