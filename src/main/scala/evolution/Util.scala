package evolution

import cats._
import cats.data.NonEmptyList
import cats.effect.std.Random
import cats.implicits._

object Util {

  implicit final class RandomNel[F[_]:Functor](r: Random[F]) {
    def shuffleNel[A](nel: NonEmptyList[A]): F[NonEmptyList[A]] =
      r.shuffleList(nel.toList).map(NonEmptyList.fromListUnsafe)
  }

  implicit final class OptionAugmented[A](o: Option[A]) {
    def getOrThrows(msg:String):A = o.getOrElse(throw new Exception(msg))
  }

}
