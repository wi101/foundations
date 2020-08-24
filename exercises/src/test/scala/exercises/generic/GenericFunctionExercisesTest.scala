package exercises.generic

import exercises.generic.GenericFunctionExercises.Predicate.True
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import exercises.generic.GenericFunctionExercises._
import exercises.generic.GenericFunctionExercises.Predicate._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scala.util.Try

class GenericFunctionExercisesTest extends AnyFunSuite with ScalaCheckDrivenPropertyChecks {

  ////////////////////
  // Exercise 1: Pair
  ////////////////////

  test("Pair swap") {
    assert(Pair(0, 1).swap == Pair(1, 0))
  }

  test("Pair map") {
    assert(Pair(4, 2).map(identity) == Pair(4, 2))
  }

  test("Pair decoded") {
    assert(decoded == Pair("Functional", "Programming"))
  }

  test("Pair zipWith") {
    assert(Pair(0, 2).zipWith(Pair(3, 4))((x, y) => x + y) == Pair(3, 6))
  }

  test("Pair productNames") {
    assert(products == Pair(Product("Coffee", 2.5), Product("Plane ticket", 329.99)))
  }

  ////////////////////////////
  // Exercise 2: Predicate
  ////////////////////////////

  test("Predicate &&") {
    assert((isEven && isPositive)(12))
    assert(!(isEven && isPositive)(11))
    assert(!(isEven && isPositive)(-4))
    assert(!(isEven && isPositive)(-7))
  }

  test("Predicate && PBT") {
    forAll { (eval1: Int => Boolean, value: Int) =>
      val p1 = Predicate(eval1)
      assert(!(p1 && False)(value))
      assert((p1 && True)(value) == p1(value))
    }
  }

  test("Predicate || PBT") {
    forAll { (eval1: Int => Boolean, value: Int) =>
      val p1 = Predicate(eval1)
      assert((p1 || False)(value) == p1(value))
      assert((p1 || True)(value))
    }
  }

  test("Predicate ||") {
    assert((isEven || isPositive)(12) == true)
    assert((isEven || isPositive)(11) == true)
    assert((isEven || isPositive)(-4) == true)
    assert((isEven || isPositive)(-7) == false)
  }

  test("Predicate flip") {
    forAll { (eval: Int => Boolean, value: Int) =>
      assert(Predicate(eval).flip(value) == !eval(value))
    }
  }

  test("validate user should work correctly") {
    assert(isValidUser(User("John", 20)) == true)
    assert(isValidUser(User("John", 17)) == false) // user is not an adult
    assert(isValidUser(User("john", 20)) == false) // name is not capitalized
    assert(isValidUser(User("x", 23)) == false)    // name is too small
  }

  ////////////////////////////
  // Exercise 3: JsonDecoder
  ////////////////////////////

  test("JsonDecoder UserId") {}

  test("JsonDecoder LocalDate") {}

  test("JsonDecoder weirdLocalDateDecoder") {}

}
