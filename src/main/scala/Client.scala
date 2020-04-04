import Entities.Rocket
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import play.api.libs.json.{ JsResult, Json }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContextExecutor, Future }

class Client()(
  implicit val executionContext: ExecutionContextExecutor
) {
  implicit val actorSystem = ActorSystem()
  val Url = "https://api.spacexdata.com/v3/rockets/"
  private val expirationTime: FiniteDuration = 500.millis

  def getAllRockets: Future[Seq[Rocket]] = {
    val responseFuture = parseHttpResponse(Http().singleRequest(HttpRequest(uri = Url)))
    responseFuture.map(response => {
      val bodyJsResult: JsResult[Seq[Rocket]] = Json.parse(response.body).validate[Seq[Rocket]]
      bodyJsResult.get
    })

  }
  def getOneRocketById(rocketID: String): Future[Rocket] = {
    val responseFuture = parseHttpResponse(Http().singleRequest(HttpRequest(uri = Url + rocketID)))
    responseFuture.map(response => {
      val bodyJsResult: JsResult[Rocket] = Json.parse(response.body).validate[Rocket]
      bodyJsResult.get
    })
  }

  def getRandomImage: Future[String] =
    getAllRockets.map(rockets => randomImageFormAllRockets(rockets.toArray))

  private def parseHttpResponse(futureResponse: Future[HttpResponse]): Future[Response] =
    futureResponse.flatMap { response =>
      response.entity.toStrict(expirationTime).map { body =>
        Response(response.status.intValue(), body.data.utf8String)
      }
    }

  private def randomImageFormAllRockets(rockets: Array[Rocket]) = {
    def random(max: Int) = scala.util.Random.nextInt(max)
    val randomRocket = rockets(random(rockets.length))
    val images = randomRocket.flickr_images
    images(random(images.size))
  }
}

case class Response(status: Int, body: String)
