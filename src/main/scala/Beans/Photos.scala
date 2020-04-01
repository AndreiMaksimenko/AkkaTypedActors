package Beans

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class Version(
  name: String,
  message: String,
  target: String
)
case class Photos(
  photos: List[Photo]
)
case class Versions(
  verions: List[Version]
)
case class Photo(
  earth_date: String,
  img_src: String
)
case class Camera(
  full_name: String,
  id: Int,
  name: String,
  rover_id: Int
)
case class Rover(
  id: Int,
  landing_date: String,
  launch_date: String,
  max_date: String,
  max_sol: Int,
  name: String,
  status: String,
  total_photos: Int
)

trait PhotosJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val versionFormat = jsonFormat3(Version)
  implicit val cameraFormat = jsonFormat4(Camera)
  implicit val roverFormat = jsonFormat8(Rover)
  implicit val photiFormat = jsonFormat2(Photo)
  implicit val photosFormat = jsonFormat1(Photos)
  implicit val versionsFormat = jsonFormat1(Versions)

}
