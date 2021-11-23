
### To run

```
sbt:EvolutionGame> reStart
```

Ignore SLF4J messages.

On the Shell
```
$ curl -X POST http://localhost:8080/game/single
Game is single 
$ curl -X POST http://localhost:8080/game/hands
Player1 hand: NonEmptyList(Card(8, C)), Player2 hand: NonEmptyList(Card(9, H)) 
$ curl -X POST "http://localhost:8080/game/moves?player1=fold&player2=fold"
There was no winner.. Player1 has -3 tokens, player2 has -3. 
$ curl http://localhost:8080/game/inspect
ServerState(Player(Player 1,-3),Player(Player 2,-3),None,None)
```

### About the source code

* [Data.scala](src/main/scala/evolution/Data.scala) - models the domain of the game. In a few places it uses toString 
instead of something cleverer to deal with user presentation
* [Logic.scala](src/main/scala/evolution/Logic.scala) - the game logic. It doesn't deal well with the flow of the game.
* [Server.scala](src/main/scala/evolution/Server.scala) - An Http server. It uses a hacky Ref to manage state, 
unfortunately it only allows one client at a time.  
* [Test.scala](src/test/scala/evolution/Test.scala) - unit tests.

### Unit tests test

Currently, only tests the logic.

```
sbt:EvolutionGame> test
```
