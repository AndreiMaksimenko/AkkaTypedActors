package Beans

import cats.data._
import cats.implicits._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

case class User(id: Long, name: String)

sealed trait Error
object Error {
  final case class UserNotFound(userId: Long) extends Error
  final case class ConnectionError(message: String) extends Error
}
object UserRepo {
  def followers(userId: Long)(implicit ec: ExecutionContext): EitherT[Future, Error, List[User]] =
    userId match {
      case 0L =>
        EitherT.right(Future { List(User(1, "Michael")) })
      case 1L =>
        EitherT.right(Future { List(User(0, "Vito")) })
      case x =>
        println("not found")
        EitherT.left(Future.successful { Error.UserNotFound(x) })
    }
}

object PlayGround extends App {
  import UserRepo.followers
  implicit val ec = scala.concurrent.ExecutionContext.global
//  def isFriends0(user1: Long, user2: Long): Either[Error, Boolean] =
//    followers(user1).right
//      .flatMap(a => followers(user2).right.map(b => a.exists(_.id == user2) && b.exists(_.id == user1)))
//
//  def isFriends1(user1: Long, user2: Long)(implicit ec: ExecutionContext): Future[Either[Error, Boolean]] =
//    followers(user1).flatMap(
//      a =>
//        followers(user2).map(
//          b => a.right.flatMap(x => b.right.map(y => x.exists(_.id == user2) && y.exists(_.id == user1)))
//        )
//    )
//
//  def isFriends2(user1: Long, user2: Long)(implicit ec: ExecutionContext): Future[Either[Error, Boolean]] =
//    followers(user1).flatMap {
//      case Right(a) =>
//        followers(user2).map {
//          case Right(b) => Right(a.exists(_.id == user2) && b.exists(_.id == user1))
//          case Left(e)  => Left(e)
//        }
//      case Left(e) => Future.successful(Left(e))
//    }

  def isFriends3(user1: Long, user2: Long)(implicit ec: ExecutionContext): EitherT[Future, Error, Boolean] =
    for {
      a <- followers(user1)
      b <- followers(user2)
    } yield a.exists(_.id == user2) && b.exists(_.id == user1)

  Await.result(isFriends3(0, 1).value, Duration.fromNanos(1000))

  def dummyFunction1(n: Int)(implicit ec: ExecutionContext): Future[Either[String, Int]] =
    if (n == 0) {
      Future.failed(new ArithmeticException("n must not be zero"))
    } else {
      Future.successful(
        if (n % 2 == 0)
          Right(n / 2)
        else
          Left("An odd number")
      )
    }

}
