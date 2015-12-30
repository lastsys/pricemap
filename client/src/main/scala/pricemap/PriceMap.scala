package pricemap

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.Dynamic.{global ⇒ g, literal ⇒ L, newInstance ⇒ jsNew}

@JSExport
object PriceMap {
  @JSExport
  def init(): Unit = {
    val map = jsNew(g.ol.Map)(L(
      "target" → "price-map",
      "layers" → js.Array(
        jsNew(g.ol.layer.Tile)(L(
          "title" → "OpenStreetMap",
          "preload" → 2,
          "source" → jsNew(g.ol.source.OSM)(L(
            "url" → "http://otile{1-4}.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png"
          ))
        )),
        jsNew(g.ol.layer.Tile)(L(
          "title" → "PriceMap",
          "preload" → 0,
          "opacity" → 0.4,
          "source" → jsNew(g.ol.source.OSM)(L(
            "url" → "/tile/{z}/{x}/{y}"
          ))
        ))
      ),
      "view" → jsNew(g.ol.View)(L(
        "projection" → "EPSG:3857",
        "center" → js.Array(0, 0),
        "zoom" → 8,
        "minZoom" → 0,
        "maxZoom" → 15
      ))
    ))

    centerMap(map, 18.06, 59.32)
  }

  def centerMap(map: js.Dynamic, long: Double, lat: Double): Unit = {
    map.getView().setCenter(g.ol.proj.transform(js.Array(long, lat),
      "EPSG:4326", "EPSG:3857"))
  }
}
