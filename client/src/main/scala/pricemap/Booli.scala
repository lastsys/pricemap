package pricemap

import Types.{latitude, longitude}

case class BoundingBox(minLat: latitude,
                       maxLat: latitude,
                       minLon: longitude,
                       maxLon: longitude)

case class BooliParameters(callerId: String,
                           privateKey: String,
                           uri: String,
                           maxPages: Int)

object Booli {

}
