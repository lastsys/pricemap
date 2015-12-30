package models

case class BooliObject(booliId: Int,
                      longitude: Double,
                      latitude: Double,
                      x: Double,
                      y: Double,
                      oceanDistance: Option[Double],
                      approximateLocation: Option[Boolean],
                      streetAddress: String,
                      listPrice: Option[Double],
                      floor: Option[Double],
                      livingArea: Option[Double],
                      additionalArea: Option[Double],
                      rooms: Option[Double],
                      published: java.sql.Date,
                      constructionYear: Option[Int],
                      objectType: String,
                      soldDate: java.sql.Date,
                      soldPrice: Double,
                      url: String,
                      municipality: String)
