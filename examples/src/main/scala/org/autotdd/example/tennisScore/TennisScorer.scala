package org.autotdd.example.tennisScore

import org.autotdd.engine.Engine
import org.junit.runner.RunWith
import javax.ws.rs._
import org.autotdd.engine.tests._

case class Score(item: String)
object Score {
  val love = Score("love")
  val s15 = Score("fifteen")
  val s30 = Score("thirty")
  val s40 = Score("forty")
  val deuce = Score("deuce")
  val advantage = Score("advantage")
  val noScore = Score("noScore")
  val error = Score("error")
  val won = Score("won")
  val lost = Score("lost")
}

@RunWith(classOf[AutoTddJunitRunner])
object TennisScorer {
  import Score._
  val lookup = Map(0 -> love, 1 -> s15, 2 -> s30, 3 -> s40)

  val leftWon = "left won"
  val rightWon = "right won"

  val scorer = Engine[Int, Int, String]().
    withDescription("Tennis Kata specified by http://codingdojo.org/cgi-bin/wiki.pl?KataTennis").
    withDefaultCode((l: Int, r: Int) => "error").
    useCase("A game is won by the first player to have won at least four points in total and at least two points more than the opponent.").
    scenario(4, 0).expected(leftWon).because((l: Int, r: Int) => (l - r) >= 2 && l >= 4).
    scenario(4, 1).expected(leftWon).
    scenario(4, 2).expected(leftWon).
    scenario(5, 3).expected(leftWon).

    scenario(0, 4).expected(rightWon).because((l: Int, r: Int) => (r - l) >= 2 && r >= 4).
    scenario(1, 4).expected(rightWon).
    scenario(2, 4).expected(rightWon).
    scenario(3, 5).expected(rightWon).
    scenario(40, 42).expected(rightWon).

    useCase("The running score of each game is described in a manner peculiar to tennis: scores from zero to three points are described as 'love', 'fifteen', 'thirty', and 'forty' respectively.").
    scenario(2, 3).expected("thirty, forty").because((l: Int, r: Int) => l < 4 && r < 4).code((l: Int, r: Int) => s"${lookup(l).item}, ${lookup(r).item}").
    scenario(2, 1).expected("thirty, fifteen").

    useCase("The running score, if both scores are the same, is called xx all").
    scenario(0, 0).expected("love all").because((l: Int, r: Int) => l == r && l < 3).code((l: Int, r: Int) => s"${lookup(l).item} all").
    scenario(2, 2).expected("thirty all").

    useCase("If at least three points have been scored by each player, and the scores are equal, the score is 'deuce'.").
    scenario(3, 3).expected("deuce").because((l: Int, r: Int) => l >= 3 && r >= 3 && l == r).
    scenario(4, 4).expected("deuce").because((l: Int, r: Int) => l >= 3 && r >= 3 && l == r).
    scenario(6, 6).expected("deuce").

    useCase("If at least three points have been scored by each side and a player has one more point than his opponent, the score of the game is 'advantage' for the player in the lead.").
    scenario(5, 4).expected("advantage left").because((l: Int, r: Int) => l >= 3 && r >= 3 && l == r + 1).
    scenario(6, 5).expected("advantage left").
    scenario(4, 3).expected("advantage left").

    scenario(4, 5).expected("advantage right").because((l: Int, r: Int) => l >= 3 && r >= 3 && r == l + 1).
    scenario(5, 6).expected("advantage right").
    scenario(3, 4).expected("advantage right").

    build
}

object TennisResource {
  final val path = "/tennis"
  final val formUrlEncoded = "application/x-www-form-urlencoded"
}

@Path(TennisResource.path)
class TennisResource() {

  @GET
  def start() = html(0, 0)

  @POST @Consumes(Array(TennisResource.formUrlEncoded))
  def continue(@FormParam("leftScore") leftScore: Int, @FormParam("rightScore") rightScore: Int, @FormParam("score") score: String) = {
    score match {
      case "Left" => html(leftScore + 1, rightScore)
      case "Right" => html(leftScore, rightScore + 1)
    }
    //    println(s"Left: ${leftScore}/${score} Right ${rightScore}")
  }

  def html(leftScore: Int, rightScore: Int) =
    <html>
      <body>
        <h1>Tennis Game</h1>
        <p>Score:{ TennisScorer.scorer(leftScore, rightScore) }</p>
        <form method='post' action={ TennisResource.path }>
          <input type='hidden' name='leftScore' value={ leftScore.toString }/>
          <input type='hidden' name='rightScore' value={ rightScore.toString }/>
          <input type='submit' name='score' value='Left'/>
          <input type='submit' name='score' value='Right'/>
        </form>
      </body>
    </html>.toString

}