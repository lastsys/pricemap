package tables

import models.BooliObject
import slick.driver.JdbcProfile

trait BooliObjectTable {
  protected val driver: JdbcProfile
  import driver.api._

  class BooliObjects(tag: Tag) extends Table[BooliObject](tag,
    "booli_objects") {

    def booliId = column[Int]("booli_id", O.PrimaryKey)
    def longitude = column[Double]("longitude")
    def latitude = column[Double]("latitude")
    def x = column[Double]("x")
    def y = column[Double]("y")
    def oceanDistance = column[Option[Double]]("ocean_distance")
    def approximateLocation = column[Option[Boolean]]("approximate_location")
    def streetAddress = column[String]("street_address")
    def listPrice = column[Option[Double]]("list_price")
    def floor = column[Option[Double]]("floor")
    def livingArea = column[Option[Double]]("living_area")
    def additionalArea = column[Option[Double]]("additional_area")
    def rooms = column[Option[Double]]("rooms")
    def published = column[java.sql.Date]("published")
    def constructionYear = column[Option[Int]]("construction_year")
    def objectType = column[String]("object_type")
    def soldDate = column[java.sql.Date]("sold_date")
    def soldPrice = column[Double]("sold_price")
    def url = column[String]("url")
    def municipality = column[String]("municipality")

    def * = (booliId, longitude, latitude, x, y,
      oceanDistance, approximateLocation, streetAddress,
      listPrice, floor, livingArea, additionalArea, rooms, published,
      constructionYear, objectType, soldDate, soldPrice,
      url, municipality) <> (BooliObject.tupled, BooliObject.unapply)

    def idxPos = index("idx_pos", (x, y), unique = false)
  }
}
