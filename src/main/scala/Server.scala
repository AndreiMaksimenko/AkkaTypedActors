import akka.actor
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ ActorRef, Scheduler }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ RejectionHandler, Route }
import akka.util.Timeout

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object Server extends App {
  import TypedCounter._
  implicit val timeout: Timeout = 5.seconds
  implicit val system: actor.ActorSystem = akka.actor.ActorSystem()
  implicit val typed: Scheduler = ClassicSchedulerOps(system.scheduler).toTyped
  val typedCounter: ActorRef[Command] = system.spawn(TypedCounter(), "Typed")
  implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  val client = new Client()

  val route: Route = {
    path("pictures" / Segment) { id =>
      get {
        typedCounter ! ValidPage
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
        typedCounter ! ValidPage
        onComplete(client.getRandomImage) {
          case Success(r) =>
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<img src='$r'>"))
          case Failure(exc) =>
            complete(StatusCodes.InternalServerError, exc.getMessage)
        }
      }
    } ~ path("rockets") {
      get {
        typedCounter ! ValidPage
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
        onComplete(typedCounter ? GetSucceed) {
          case Success(CountValid(value)) =>
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h2>Endpoint requests count: $value</h2>"))
          case Failure(exc) => complete(StatusCodes.InternalServerError, exc.getMessage)
        }
      }
    } ~ path("statistic" / "handled") {
      get {
        onComplete(typedCounter ? GetHandled) {
          case Success(CountInvalid(value)) =>
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h2>Wrong endpoint requests handled: $value</h2>"))
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
          typedCounter ! InvalidPage
          complete(
            HttpEntity(
              ContentTypes.`text/html(UTF-8)`,
              s"<h2>404 Error. The path you requested [$p] does not exist.</h2>"
            )
          )
        }
      }
      .result()

  Http().bindAndHandle(route, "localhost", 8080)
}
