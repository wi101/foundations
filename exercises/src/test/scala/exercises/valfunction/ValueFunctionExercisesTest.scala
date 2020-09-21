package exercises.valfunction

import exercises.valfunction.ValueFunctionExercises._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class ValueFunctionExercisesTest extends AnyFunSuite with ScalaCheckDrivenPropertyChecks {

  /////////////////////////////////////////////////////
  // Exercise 1: String API with higher-order functions
  /////////////////////////////////////////////////////

  // replace `ignore` by `test` to enable the test
  ignore("selectDigits examples") {
    assert(selectDigits("hello4world-80") == "480")
    assert(selectDigits("welcome") == "")
  }

  // replace `ignore` by `test` to enable the test
  ignore("selectDigits length is smaller") {
    forAll { (text: String) =>
      assert(selectDigits(text).length <= text.length)
    }
  }

  test("selectDigits only returns number") {
    forAll { (text: String) =>
      selectDigits(text).foreach(c => assert(c.isDigit))
    }
  }

  test("secret examples") {
    assert(secret("password") == "********")
    assert(secret("") == "")
  }

  test("secret should only return stars") {
    forAll { (text: String) =>
      secret(text).foreach(c => assert(c == '*'))
    }
  }

  test("secret should be idempotent, calling secrets many times is the same as calling it once ") {
    forAll { (text: String) =>
      val once  = secret(text)
      val twice = secret(secret(text))
      assert(once == twice)
    // we can safely retry the function
    }
  }

  test("isValidUsername should return true if the name contains digit, letters, -, or _ ") {
    assert(isValidUsername("wiem14-_"))
  }
  test("isValidUsername should return false if the name is invalid") {
    assert(!isValidUsername("wiem-14$"))
  }

  test("isValidUsername should return the same result for a reversed userName") {
    forAll { (usrName: String) =>
      assert(isValidUsername(usrName.reverse) == isValidUsername(usrName))
    }
  }
  ///////////////////////
  // Exercise 2: Point
  ///////////////////////

  test("isPositive max 0") {
    forAll { (x: Int, y: Int, z: Int) =>
      assert(Point(x.max(0), y.max(0), z.max(0)).isPositive)
    }
  }
  test("isEven") {
    forAll { (x: Int, y: Int, z: Int) =>
      assert(Point(x * 2, y * 2, z * 2).isEven)
    }
  }

  test("forAll") {
    forAll { (x: Int, y: Int, z: Int) =>
      assert(Point(x, y, z).forAll(_ => true))
    }
  }

  // oracle technique
  test("forAll using oracle technique") {
    forAll { (x: Int, y: Int, z: Int, predicate: Int => Boolean) =>
      assert(Point(x, y, z).forAll(predicate) == List(x, y, z).forall(predicate))
    }
  }
}
