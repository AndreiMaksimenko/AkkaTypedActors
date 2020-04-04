import Entities.Rocket
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ ActorRef, Behavior }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import play.api.libs.json.{ JsResult, Json }

import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.concurrent.duration._

object TypedClient {
  sealed trait ClientCommand
  case class GetRockets(sender: ActorRef[Rockets]) extends ClientCommand
  case class GetRocketByID(id: String, sender: ActorRef[RocketByID]) extends ClientCommand
  case class GetRandomImage(sender: ActorRef[Image]) extends ClientCommand
  case class Rockets(rockets: Future[Seq[Rocket]])
  case class RocketByID(rocket: Future[Rocket])
  case class Image(image: Future[String])

  def apply(): Behavior[ClientCommand] =
    Behaviors.receive { (context, message) =>
      implicit val actorSystem: ActorSystem = context.system.toClassic
      implicit val executionContext: ExecutionContextExecutor = context.executionContext
      val Url = "https://api.spacexdata.com/v3/rockets/"
      val expirationTime: FiniteDuration = 5000.millis

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

      def parseHttpResponse(futureResponse: Future[HttpResponse]): Future[Response] =
        futureResponse.flatMap { response =>
          response.entity.toStrict(expirationTime).map { body =>
            Response(response.status.intValue(), body.data.utf8String)
          }
        }

      def randomImageFormAllRockets(rockets: Array[Rocket]) = {
        def random(max: Int) = scala.util.Random.nextInt(max)
        val randomRocket: Rocket = rockets(random(rockets.length))
        val images = randomRocket.flickr_images
        images(random(images.size))
      }

      message match {
        case GetRockets(sender) =>
          val rockets: Future[Seq[Rocket]] = getAllRockets
          sender ! Rockets(rockets)
          Behaviors.same
        case GetRocketByID(id, sender) =>
          val rocket: Future[Rocket] = getOneRocketById(id)
          sender ! RocketByID(rocket)
          Behaviors.same
        case GetRandomImage(sender) =>
          val image: Future[String] = getRandomImage
          sender ! Image(image)
          Behaviors.same
      }

    }
}

case class Response(status: Int, body: String)
