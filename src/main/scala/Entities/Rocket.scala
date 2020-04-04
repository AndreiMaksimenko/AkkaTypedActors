package Entities

import play.api.libs.json.{ Json, OFormat, Reads }

case class Rocket(
  id: Double,
  active: Boolean,
  stages: Double,
  boosters: Double,
  cost_per_launch: Double,
  flickr_images: Seq[String],
  wikipedia: String,
  description: String,
  rocket_id: String,
  rocket_name: String,
  rocket_type: String
)
object Rocket {
  implicit val rocketPlayFormat: OFormat[Rocket] = Json.format[Rocket]
  implicit val rocketsPlayListFormat: Reads[Seq[Rocket]] = Reads.seq(rocketPlayFormat)
}
