import TypedClient._
import TypedCounter._
import TypedServer.StartServer
import akka.actor
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior, Scheduler }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.util.Timeout

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object TypedServer {
  case object StartServer
  def apply(): Behavior[StartServer.type] =
    Behaviors.setup { context =>
      implicit val timeout: Timeout = 5.seconds
      implicit val typed: Scheduler = context.system.scheduler
      implicit val systemClassic: actor.ActorSystem = context.system.toClassic
      implicit val executionContext: ExecutionContextExecutor = context.executionContext
      val client: ActorRef[ClientCommand] = context.spawn(TypedClient(), "client")
      val counter: ActorRef[Command] = context.spawn(TypedCounter(), "counter")

      Behaviors.receiveMessage { _ =>
        val route: Route = {
          path("rockets") {
            get {
              counter ! ValidPage
              onComplete((client ? GetRockets).flatMap(ask => ask.rockets)) {
                case Success(r) =>
                  complete(
                    r.map(rocket => rocket.rocket_id).toString()
                  )
                case Failure(exc) =>
                  complete(StatusCodes.InternalServerError, exc.getMessage)
              }
            }
          } ~
            path("pictures" / Segment) { id =>
              get {
                counter ! ValidPage

                onComplete((client ? (GetRocketByID(id, _))).flatMap(rocket => rocket.rocket)) {
                  case Success(r) =>
                    complete(
                      r.flickr_images.toString()
                    )
                  case Failure(exc) =>
                    complete(StatusCodes.InternalServerError, exc.getMessage)
                }
              }
            } ~
            path("pictures") {
              get {
                counter ! ValidPage
                onComplete((client ? GetRandomImage).flatMap(ask => ask.image)) {
                  case Success(r) =>
                    complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<img src='${r}'>"))
                  case Failure(exc) =>
                    complete(StatusCodes.InternalServerError, exc.getMessage)
                }
              }
            } ~ path("statistic" / "succeed") {
            get {
              onComplete(counter ? GetSucceed) {
                case Success(CountValid(value)) =>
                  complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h2>Endpoint requests count: $value</h2>"))
                case Failure(exc) => complete(StatusCodes.InternalServerError, exc.getMessage)
              }
            }
          } ~ path("statistic" / "handled") {
            get {
              onComplete(counter ? GetHandled) {
                case Success(CountInvalid(value)) =>
                  complete(
                    HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h2>Wrong endpoint requests handled: $value</h2>")
                  )
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
                counter ! InvalidPage
                complete(
                  HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    s"<h2>404 Error. The path you requested [$p] does not exist.</h2>"
                  )
                )
              }
            }
            .result()
            .seal

        Http().bindAndHandle(route, "localhost", 8080)
        Behaviors.same
      }
    }

}

object Application extends App {
  private val server: ActorSystem[TypedServer.StartServer.type] = ActorSystem(TypedServer(), "Server")
  server ! StartServer
}
