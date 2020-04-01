import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.{ HttpApp, Route }
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor

object Server extends HttpApp {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val client = new Client()

  override protected def routes: Route = pathSingleSlash {
    get {
      complete(
        client.getVersions
          .map(x => x.verions.concat(""))
          .map(x => HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"<img src='$x'>"))

//          .flatMap(value => client.getOnePhoto(value))
//          .map(url => HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"<img src='$url'>"))
      )
    }
  }

  def main(args: Array[String]): Unit =
    Server.startServer("localhost", 8080)

}
