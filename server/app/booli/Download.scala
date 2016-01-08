package booli

import java.io._
import java.util.concurrent.Executors

import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.WS

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Random

object Download {

  def downloadBooliData(): Unit = {
    // Now load all pages and save raw text to disk.
    implicit val executionContext = ExecutionContext.fromExecutor(
      Executors.newFixedThreadPool(2))

    val totalCount = for {
      body ← get(offset = 0, limit = 1)
    } yield {
      println(body)
      val json = Json.parse(body)
      val count = (json \ "totalCount").get.toString.toInt
      println(s"About to fetch $count items.")
      count
    }

    totalCount.onFailure {
      case f ⇒
        println("Failed to get total count.")
        f.printStackTrace()
    }

    val task = for {
      count ← totalCount
    } yield {
      val offsets = 0 to (count / Configuration.pageSize)
      offsets.foreach {
        (i: Int) ⇒
          val path = f"${Configuration.outputPath}/response$i%04d.json"
          if (!new java.io.File(path).exists) {
            val offset = i * Configuration.pageSize
            // We need to be nice to the server we are calling.
            // If we do not await the result we will have *many*
            // simultaneous calls.
            val json = Await.result(
              get(offset, Configuration.pageSize), 10 seconds)
            println(s"Writing $path")
            val file = new File(path)
            val bw = new BufferedWriter(
              new OutputStreamWriter(
                new FileOutputStream(file), "UTF-8"))
            bw.write(json.toString)
            bw.close()
          }
      }
      println(offsets)
    }
  }

  def get(offset: Int, limit: Int): Future[String] = {
    import play.api.libs.concurrent.Execution.Implicits.defaultContext

    val r = Random.alphanumeric
    val unique = r.take(16).mkString
    val timestamp = System.currentTimeMillis / 1000
    val hash = {
      val md = java.security.MessageDigest.getInstance("SHA-1")
      val bytes = md.digest(
        (s"${Configuration.user}" +
          s"$timestamp" +
          s"${Configuration.token}" +
          s"$unique").getBytes("UTF-8"))
      bytes.map("%02x".format(_)).mkString
    }

    val request = WS.url(Configuration.url)
    val cmplxReq = request.withHeaders(
      "Accept" → "application/json",
      "User-Agent" → "bopriskarta/1.0",
      "Referrer" → sys.env("PRICEMAP_REFERRER"))
      .withQueryString(
        "bbox" → "55,10,70,25",
        "maxpages" → 0.toString,
        "callerId" → Configuration.user,
        "time" → timestamp.toString,
        "unique" → unique,
        "hash" → hash,
        "offset" → offset.toString,
        "limit" → limit.toString)

    val getResult = for {
      response ← cmplxReq.get()
    } yield {
      if (response.status != 200) {
        println(s"Got result with response ${response.status}")
      }
      response.body
    }

    getResult.onFailure {
      case f ⇒ f.printStackTrace()
    }

    getResult
  }
}
