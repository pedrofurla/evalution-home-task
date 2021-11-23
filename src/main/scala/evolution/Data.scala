package evolution

import cats._
import cats.data.NonEmptyList
import cats.implicits._

object Data {

  sealed trait Number { val value:Int }
  object Number {
    implicit val NumberOrder:Order[Number] = Order.by( _.value )
    val enum:NonEmptyList[Number] = NonEmptyList.of(Ace, King, Queen, Jack, Ten, Nine, Eight, Seven, Six, Five, Four, Three, Two).reverse
    val NumberEq:Eq[Number] = Eq.fromUniversalEquals
  }

  object Ace    extends Number { val value = 14}
  object King   extends Number { val value = 13}
  object Queen  extends Number { val value = 12}
  object Jack   extends Number { val value = 11}
  object Ten    extends Number { val value = 10}
  object Nine   extends Number { val value = 9}
  object Eight  extends Number { val value = 8}
  object Seven  extends Number { val value = 7}
  object Six    extends Number { val value = 6}
  object Five   extends Number { val value = 5}
  object Four   extends Number { val value = 4}
  object Three  extends Number { val value = 3}
  object Two    extends Number { val value = 2}


  sealed trait Suit { val symbol:String }
  object Suit {
    val enum:NonEmptyList[Suit] = NonEmptyList.of(Spade, Hearts, Diamonds, Clubs).reverse
    val SuitEq:Eq[Suit] = Eq.fromUniversalEquals
  }

  object Spade    extends Suit { val symbol:String = "S" }
  object Hearts   extends Suit { val symbol:String = "H" }
  object Diamonds extends Suit { val symbol:String = "D" }
  object Clubs    extends Suit { val symbol:String = "C" }

  final case class Card(number: Number, suit: Suit) {
    override def toString: String = s"Card(${number.value}, ${suit.symbol})"
  }
  object Card {
    val prefix = "Card.fromString("
    implicit val CardOrder:Order[Card] = Order.by(_.number)
    val deck: NonEmptyList[Card] = Applicative[NonEmptyList].product(Number.`enum`, Suit.`enum`) map { case (n, s) => Card(n,s) }
    def fromString(s:String): Card = ???
  }

  final case class Player(label:String, tokens:Int)
  object Player {
    implicit val PlayerEq:Eq[Player] = Eq.by(x => (x.label, x.tokens))

    val one = Player("Player 1", 0)
    val two = Player("Player 2", 0)

    val enum:NonEmptyList[Player] = NonEmptyList.of(one, two)
  }

  /** Game represents the type of game being played. `count` is the of cards to be drawn for each player */
  final case class Game(count:Int, foldStake:Int) {
    assert(count > 0, "`count` greater than than zero")
    assert(count < Card.deck.length/2, "Size of `count` should be smaller than half of a full deck")
  }
  object Game {

  }
  val Single = Game(1, 3)
  val Double = Game(2, 5)

  trait Move
  object Move {}
  object Play extends Move
  object Fold extends Move

  /** Represents a player ready to play. Having cards being a list leaves the
   * possibility of a N cards game open */
  final case class Hand(player:Player, cards: NonEmptyList[Card])
  object Hand {
    implicit val HandOrder:Order[Hand] = Order.by{ case Hand(_, c) => c }
  }


}
