package evolution

import cats._
import cats.data.Kleisli
import cats.syntax.all._
import org.http4s.blaze.server._
import cats.effect.unsafe.implicits.global
import cats.effect._
import cats.effect.std._
import org.http4s.{HttpRoutes, QueryParamDecoder, Request, Response}
import org.http4s.dsl.{Http4sDsl, Http4sDsl2}
import org.http4s.implicits._
import org.http4s.server.Router
import evolution.Data._
import evolution.Logic._

object Server extends IOApp {
  val dsl = new Http4sDsl[IO]{}
  import dsl._

  case class ServerState(
    p1: Player,
    p2: Player,
    game:  Option[Game],
    hands: Option[(Hand, Hand)]
  ) {
    def setGame(g: Game):Option[ServerState] =
      game match {
        case Some(_) => None
        case None    => Option(ServerState.initial.copy(game = g.some))
      }
    def setHands: IO[Option[ServerState]] =
      (for {
        g <- game
        hands = this.hands match {
          case Some(_) => IO(None)
          case None    => GameState.hand[IO](GameState(g, p1, p2)).map {
            h => Option( this.copy(hands = Option(h)) )
          }
        }
      } yield hands).sequence.map(_.flatten)

  }
  object ServerState {
    val initial = ServerState(Player.one, Player.two, none, none)
  }

  val state: Ref[IO, ServerState] = Ref[IO].of(ServerState.initial).unsafeRunSync

  def err: IO[Response[IO]] = InternalServerError("Game in invalid state")

  implicit val moveDecoder: QueryParamDecoder[Move] =
    QueryParamDecoder[String].map(_.toLowerCase).map{ case "fold" => Fold; case "play" => Play }

  object Move1 extends QueryParamDecoderMatcher[Move]("player1")
  object Move2 extends QueryParamDecoderMatcher[Move]("player2")

  def helloWorldService(state:Ref[IO, ServerState]): Kleisli[IO, Request[IO], Response[IO]] = HttpRoutes.of[IO] {
    case GET  -> Root / "game" / "inspect" => state.get.flatMap(s => Ok(s.toString))
    case POST -> Root / "game" / "single" =>
      for {
        s   <- state.get
        r   <- s.setGame(Single).fold(err)(s => Ok(s"Game is single") <* state.set(s))
      } yield r
    case POST -> Root / "game" / "double" =>
      for {
        s   <- state.get
        r   <- s.setGame(Double).fold(err)(s => Ok(s"Game is double") <* state.set(s))
      } yield r
    case POST -> Root / "game" / "hands" =>
      def hands(h:(Hand, Hand)): String = h match {
        case (Hand(_, cs1), Hand(_, cs2)) => s"Player1 hand: ${cs1}, Player2 hand: ${cs2}"
      }
      for {
        s   <- state.get
        h   <- s.setHands
        r   <- h.flatMap(s => s.hands.map( h => (s, h))).fold(err) {
          case (s, h) => Ok(hands(h)) <* state.set(s)
        }
      } yield r
    case POST -> Root / "game" / "moves" :? Move1(move1) +& Move1(move2) =>
      for {
        // I regret some bad design decisions, taking a shortcut
        currState@ServerState(p1, p2, Some(game), Some((hand1, hand2))) <- state.get
        gameResult = play(hand1, move1, hand2, move2)
        GameState(_, player1, player2) = Result.applyResult(game, p1, p2, gameResult)
        r <- if(gameResult == Tie) {
          // In case of a Tie we keep the same game
          Ok("Tie") <* state.set(currState.copy(p1 = player1, p2 = player2, game = Option(game), hands = none))
        } else {
          val winner = Result.winner(gameResult).fold("There was no winner.")(w => s"Winner is ${w.label}.")
          Ok(s"$winner. Player1 has ${player1.tokens} tokens, player2 has ${player2.tokens}.") <*
            state.set(currState.copy(p1 = player1, p2 = player2, game = none, hands = none))
        }
      }  yield r
  }.orNotFound

  def server: IO[ExitCode] = BlazeServerBuilder[IO]
        .bindHttp(8080, "localhost")
        .withHttpApp(helloWorldService(state))
        .serve
        .compile
        .drain
        .as(ExitCode.Success)

  def run(args: List[String]): IO[ExitCode] = server
}
