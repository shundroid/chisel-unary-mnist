package unary

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class BalancerSpec extends AnyFreeSpec with Matchers {
  def runTestPattern(genA: Int => Boolean, genB: Int => Boolean, len: Int = 12): Unit = {
    simulate(new Balancer) { dut =>
      dut.reset.poke(true.B)
      dut.clock.step()
      dut.reset.poke(false.B)

      var aList: List[Int] = Nil
      var bList: List[Int] = Nil
      var outList: List[Int] = Nil
      for (i <- 0 until len) {
        dut.io.inA.poke(genA(i))
        dut.io.inB.poke(genB(i))
        val a = dut.io.inA.peek().litValue.toInt
        val b = dut.io.inB.peek().litValue.toInt
        val out = dut.io.outB.peek().litValue.toInt
        aList = a :: aList
        bList = b :: bList
        outList = out :: outList
        println(s"inA: $a")
        println(s"inB: $b")
        println(s"outB: $out")
        println(s"state: ${dut.io.state.peek().litValue}")
        println(s"grabbing: ${dut.io.grabbing.peek().litValue}")
        println(s"grabbed_bit: ${dut.io.grabbed_bit.peek().litValue}")
        dut.clock.step()
      }
      println(s"outB: ${dut.io.outB.peek().litValue}")
      println(s"state: ${dut.io.state.peek().litValue}")
      println(s"grabbing: ${dut.io.grabbing.peek().litValue}")
      println(s"grabbed_bit: ${dut.io.grabbed_bit.peek().litValue}")
      println(s"aList: $aList")
      println(s"bList: $bList")
      println(s"outList: $outList")

      // assert the count of 1 between bList and outList is the same
      val bCount = bList.count(_ == 1)
      val outCount = outList.count(_ == 1)
      bCount must be(outCount)

      // assert the xor of aList and outList is balanced
      val xorList = aList.zip(outList).map { case (a, b) => a ^ b }
      val xorCount = xorList.count(_ == 1)
      xorCount must be(len / 2)

      val prevXorList = aList.zip(bList).map { case (a, b) => a ^ b }
      val prevXorCount = prevXorList.count(_ == 1)
      println(s"before: ${prevXorCount.floatValue / len}, after: ${xorCount.floatValue / len}")
    }
  }
  "balancer should handle 0101 and 1010" in {
    runTestPattern(i => i % 2 == 0, i => i % 2 == 1)
  }
  "balancer should handle 1010 and 1010" in {
    runTestPattern(i => i % 2 == 0, i => i % 2 == 0)
  }
  "balancer should handle 1100 and 1100" in {
    runTestPattern(i => i % 4 < 2, i => i % 4 < 2)
  }
  "how about 1100 and 1000" in {
    runTestPattern(i => i % 4 == 0, i => i % 2 == 0)
  }
}