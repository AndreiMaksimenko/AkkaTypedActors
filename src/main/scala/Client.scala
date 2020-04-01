import java.nio.charset.Charset

import Beans.{ PhotosJsonSupport, Version, Versions }
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{ ModeledCustomHeader, ModeledCustomHeaderCompanion }
import akka.stream.ActorMaterializer
import akka.util.ByteString
import spray.json._

import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.util.{ Failure, Success, Try }

final class ApiTokenHeader(token: String) extends ModeledCustomHeader[ApiTokenHeader] {
  override def renderInRequests = true
  override def renderInResponses = true
  override val companion = ApiTokenHeader
  override def value: String = token
}
object ApiTokenHeader extends ModeledCustomHeaderCompanion[ApiTokenHeader] {
  override val name = "PRIVATE-TOKEN"
  override def parse(value: String) = Try(new ApiTokenHeader(value))
}

class Client()(
  implicit val system: ActorSystem,
  implicit val materializer: ActorMaterializer,
  implicit val executionContext: ExecutionContextExecutor
) extends PhotosJsonSupport {
  val gitLabURL =
    "https://gitlab.com/api/v4/projects/AndreiMaksimenko%2FPath1/repository/tagshttps://gitlab.com/api/v4/projects/AndreiMaksimenko%2FPath1/repository/tags"
  val key = "-BrCsxHmHTs2EBAMiRTx"

  def responseFuture: Future[HttpResponse] =
    Http().singleRequest(HttpRequest(method = GET, uri = s"$gitLabURL", headers = Seq(ApiTokenHeader(key))))

  def main(args: Array[String]): Unit =
//    getPhotos.onComplete {
//      case Success(value: Photos) => println(value)
//      case Failure(exception)     => println("Fail: " + exception)
//    }
//
//    getPhoto.onComplete {
//      case Success(value)     => value.foreach(x => println(x.asJsObject.fields("img_src").asInstanceOf[JsString].value))
//      case Failure(exception) => println("Fail: " + exception)
//    }
    getVersions.onComplete {
      case Success(value: List[Version]) => value.foreach(x => println(x.name + x.target + x.message))
      case Failure(exception)            => println("Fail" + exception)
    }

  def getVersions: Future[Versions] =
    responseFuture.flatMap { res: HttpResponse =>
      res.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map { body =>
        body.decodeString(Charset.defaultCharset()).parseJson.convertTo[Versions]
      }
    }

//  def get(url: String, params: Map[String, String] = Map(), headers: Map[String, String] = Map()): Future[Response] = {
//    val futureResponse: Future[HttpResponse] = Http().singleRequest(
//      HttpRequest(method = HttpMethods.GET, uri = Uri(url).withQuery(Query(params))).withHeaders(parseHeaders(headers))
//    )
//    responsify(futureResponse)
//  }
//
//  def getPhotos: Future[Photos] =
//    responseFuture.flatMap { res: HttpResponse =>
//      res.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map { body =>
//        body.decodeString(Charset.defaultCharset()).parseJson.convertTo[Photos]
//      }
//
//    }
//
//  def getPhoto: Future[Vector[JsValue]] =
//    responseFuture.flatMap { res: HttpResponse =>
//      res.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map { body =>
//        body.decodeString(Charset.defaultCharset()).parseJson.asJsObject.fields("photos").asInstanceOf[JsArray].elements
//      }
//    }
//
//  def getOnePhoto(photos: Vector[JsValue]): Future[String] =
//    Future(s"${photos.head.asJsObject.fields("img_src").asInstanceOf[JsString].value}")
}
