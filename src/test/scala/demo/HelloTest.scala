package demo

import org.scalatest.FunSuite

/**
  * Created by remeeh on 9/26/16.
  */
class HelloTest extends FunSuite {
 test("Say hello test works correctly") {
   val hello = new Hello
   assert(hello.sayHello("Scala") == "Hello, Scala!")
 }
}
