package booli

import java.time.format.DateTimeFormatter
import java.time.{Year, LocalDateTime, LocalDate}

import play.api.libs.json._

object BooliJson {
  case class Page(totalCount: Int,
                  count: Int,
                  limit: Int,
                  offset: Int,
                  sold: Seq[Sold])

  case class Sold(location: Location,
                  listPrice: Option[Double],
                  livingArea: Option[Double],
                  additionalArea: Option[Double],
                  plotArea: Option[Double],
                  source: Source,
                  rooms: Option[Double],
                  published: LocalDateTime,
                  constructionYear: Option[Year],
                  objectType: String,
                  booliId: Int,
                  soldDate: LocalDate,
                  soldPrice: Double,
                  url: String,
                  floor: Option[Double])

  case class Location(address: Address,
                      position: Position,
                      namedAreas: Option[Seq[String]],
                      region: Region,
                      distance: Option[Distance])

  case class Address(streetAddress: String)

  case class Position(latitude: Double,
                      longitude: Double,
                      isApproximate: Option[Boolean])

  case class Region(municipalityName: String,
                    countyName: String)

  case class Source(name: String,
                    id: Int,
                    `type`: String,
                    url: String)

  case class Distance(ocean: Int)

  implicit val yearFormat = new Format[Year] {
    def reads(json: JsValue): JsResult[Year] =
      JsSuccess(java.time.Year.of(json.as[Int]))

    def writes(year: java.time.Year): JsValue =
      JsNumber(year.getValue)
  }
  implicit val dateTimeFormat = new Format[LocalDateTime] {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    def reads(json: JsValue): JsResult[LocalDateTime] =
      JsSuccess(LocalDateTime.parse(json.as[String], formatter))

    def writes(dateTime: LocalDateTime): JsValue =
      JsString(dateTime.format(formatter))
  }
  implicit val dateFormat = new Format[LocalDate] {
    def reads(json: JsValue): JsResult[LocalDate] =
      JsSuccess(LocalDate.parse(json.as[String]))

    def writes(date: LocalDate): JsValue =
      JsString(date.toString)
  }

  implicit val distanceFormat: Format[Distance] = Json.format[Distance]
  implicit val sourceFormat: Format[Source] = Json.format[Source]
  implicit val regionFormat: Format[Region] = Json.format[Region]
  implicit val positionFormat: Format[Position] = Json.format[Position]
  implicit val addressFormat: Format[Address] = Json.format[Address]
  implicit val locationFormat: Format[Location] = Json.format[Location]
  implicit val soldFormat: Format[Sold] = Json.format[Sold]
  implicit val pageFormat: Format[Page] = Json.format[Page]
}
