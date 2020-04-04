import java.util.concurrent.TimeUnit

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RejectionHandler
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContextExecutor
import scala.util.{ Failure, Success }

object Server extends App {
  import CounterActor._

  implicit val timeout: Timeout = Timeout(10, TimeUnit.SECONDS)
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  val counterActor: ActorRef = system.actorOf(Props[CounterActor], "counterActor")
  val client = new Client()

  val route = {
    path("pictures" / Segment) { id =>
      get {
        counterActor ! ValidPage
        onComplete(client.getOneRocketById(id)) {
          case Success(r) =>
            complete(
              r.flickr_images.toString()
            )
          case Failure(exc) =>
            complete(StatusCodes.InternalServerError, exc.getMessage)
        }
      }
    } ~ path("pictures") {
      get {
        counterActor ! ValidPage
        onComplete(client.getRandomImage) {
          case Success(r) =>
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<img src='$r'>"))
          case Failure(exc) =>
            complete(StatusCodes.InternalServerError, exc.getMessage)
        }
      }
    } ~ path("rockets") {
      get {
        counterActor ! ValidPage
        onComplete(client.getAllRockets) {
          case Success(r) =>
            complete(
              r.map(rocket => rocket.rocket_id).toString()
            )
          case Failure(exc) =>
            complete(StatusCodes.InternalServerError, exc.getMessage)
        }

      }
    } ~ path("statistic" / "succeed") {
      get {
        onComplete(counterActor ? GetSucceed) {
          case Success(value) =>
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h2>Endpoint requests count: ${value}</h2>"))
          case Failure(exc) => complete(StatusCodes.InternalServerError, exc.getMessage)
        }
      }
    } ~ path("statistic" / "handled") {
      get {
        onComplete(counterActor ? GetHandled) {
          case Success(value) =>
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h2>Wrong endpoint requests handled: ${value}</h2>"))
          case Failure(exc) => complete(StatusCodes.InternalServerError, exc.getMessage)
        }
      }
    }
  }

  implicit val wrongPageRejectionHandler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handleNotFound {
        extractUnmatchedPath { p =>
          counterActor ! InvalidPage
          complete(
            HttpEntity(
              ContentTypes.`text/html(UTF-8)`,
              s"<h2>404 Error. The path you requested [${p}] does not exist.</h2>"
            )
          )
        }
      }
      .result()

  Http().bindAndHandle(route, "localhost", 8080)

}
