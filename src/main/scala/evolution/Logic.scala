package evolution

import cats.Eval
import cats.data.{NonEmptyList, State, StateT}
import cats.effect._
import cats.effect.std.Random
import cats.implicits._
import evolution.Util._

object Logic {

  import Data._

  case class GameState(game:Game, p1:Player, p2:Player)
  object GameState {
    def dealer(count: Int): State[NonEmptyList[Card],NonEmptyList[Card]] =
      State(draw).map(NonEmptyList.one).flatMap(x =>
        if(count == 1) State.pure[NonEmptyList[Card],NonEmptyList[Card]](x)
        else dealer(count-1).map{ _ concatNel x }
      )

    def hand[F[_] : Sync](gameState: GameState): F[(Hand, Hand)] =
      for {
        shuffled0 <- shuffle[F](Card.deck)
        (shuffled1, hand0) = dealer(gameState.game.count).run(shuffled0).value
        (_, hand1) = dealer(gameState.game.count).run(shuffled1).value
      } yield (Hand(gameState.p1, hand0), Hand(gameState.p2, hand1))

    def initial(game: Game): GameState = GameState(game, Player.one, Player.two)

    /** Assumes it's a full deck or missing at most Game#count cards */
    def draw(deck:NonEmptyList[Card]): (NonEmptyList[Card], Card) =
      (deck.tail.toNel.getOrThrows("`draw` assumption was broken, check the documentation."), deck.head)

    def shuffle[F[_]:Sync](deck:NonEmptyList[Card]): F[NonEmptyList[Card]] =
      Random.scalaUtilRandom[F].flatMap(_.shuffleNel(deck))
  }

  trait Result
  object Result {
    def applyResult(game: Game, p1: Player, p2:Player, result: Result): GameState = {
      val (p1Tokens, p2Tokens) =  result match {
        case DoubleFold => (-game.foldStake, -game.foldStake)
        case OneFoldOnePlay(winner) =>
          (
            if (p1 === winner) game.foldStake else -game.foldStake,
            if (p2 === winner) game.foldStake else -game.foldStake
          )
        case Tie => (0, 0)
        case Played(winner) =>
          val stake = 20
          (
            if (p1 === winner) stake else -stake,
            if (p2 === winner) stake else -stake
          )
      }
      GameState(
        game,
        p1.copy(tokens = p1.tokens + p1Tokens),
        p2.copy(tokens = p2.tokens + p2Tokens)
      )
    }

    def winner(r: Result): Option[Player] = r match {
      case OneFoldOnePlay(winner) => Option(winner)
      case Played(winner) => Option(winner)
      case _              => None
    }
  }
  case object DoubleFold extends Result
  case class OneFoldOnePlay(winner:Player) extends Result
  case object Tie extends Result
  case class Played(winner:Player) extends Result

  def play(p1: Hand, p1Move: Move, p2: Hand, p2Move: Move): Result =
    (p1Move, p2Move) match {
      case (Fold, Fold) => DoubleFold
      case (Fold, Play) => OneFoldOnePlay(p2.player)
      case (Play, Fold) => OneFoldOnePlay(p1.player)
      case (Play, Play) =>
        import cats.kernel.Comparison._
        p1.cards.comparison(p2.cards) match {
          case GreaterThan => Played(p1.player)
          case LessThan    => Played(p2.player)
          case EqualTo     => Tie
        }
    }

}
