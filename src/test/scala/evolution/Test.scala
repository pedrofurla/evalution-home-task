package evolution

import cats.data.NonEmptyList
import cats.effect._
import cats.effect.std.Random
import cats.implicits._
import munit.CatsEffectSuite

import evolution.Util._
import evolution.Data._
import evolution.Logic._

class Test extends CatsEffectSuite {

  test("make sure IO computes the right result") {
    IO.pure(1).map(_ + 2) flatMap { result =>
      IO(assertEquals(result, 3))
    }
  }

  test("dealing the default deck") {
    val (state0, hand0) = GameState.dealer(2).run(Card.deck).value
    assertEquals(state0.take(2), List(Card(Two,Hearts), Card(Two, Spade)))
    assertEquals(hand0, NonEmptyList.of(Card(Two, Diamonds), Card(Two, Clubs)))

    val (state1, hand1) = GameState.dealer(2).run(state0).value
    assertEquals(state1.take(2), List(Card(Three,Clubs), Card(Three, Diamonds)))
    assertEquals(hand1, NonEmptyList.of(Card(Two, Spade), Card(Two, Hearts)))
  }

  test("Game count hands the right number of cards") {
    GameState.hand[IO](GameState.initial(Single)).map { case (Hand(_, p1), Hand(_, p2)) =>
      assertEquals(p1.length, 1)
      assertEquals(p2.length, 1)
    }

    GameState.hand[IO](GameState.initial(Double)).map { case (Hand(_, p1), Hand(_, p2)) =>
      assertEquals(p1.length, 2)
      assertEquals(p2.length, 2)
    }
  }

  test("general game play") {
    val state@GameState(_, p1, p2) = GameState.initial(Single)
    val h1 = Hand(p1, NonEmptyList.one(Card(Jack, Clubs)) )
    val h2 = Hand(p2, NonEmptyList.one(Card(Ten, Hearts)) )
    assertEquals(
      Result.applyResult(state.game, p1, p2, play(h1, Fold, h2, Play)),
      state.copy(p1 = p1.copy(tokens = -3), p2 = p2.copy(tokens = 3))
    )
    assertEquals(
      Result.applyResult(state.game, p1, p2, play(h1, Play, h2, Fold)),
      state.copy(p1 = p1.copy(tokens = 3), p2 = p2.copy(tokens = -3))
    )
    assertEquals(
      Result.applyResult(state.game, p1, p2, play(h1, Play, h2, Play)),
      state.copy(p1 = p1.copy(tokens = 20), p2 = p2.copy(tokens = -20))
    )
    assertEquals(
      Result.applyResult(state.game, p1, p2, play(Hand(p1, NonEmptyList.one(Card(Ten, Clubs)) ), Play, h2, Play)),
      state.copy(p1 = p1.copy(tokens = 0), p2 = p2.copy(tokens = 0))
    )
  }

}
