package unary

import chisel3._
import chisel3.util._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class uNSLinearAddSpec extends AnyFreeSpec with Matchers {
  def runTestPattern(v1: Int, v2: Int, w1: Int, w2: Int, iter: Int = 1024): Unit = {
    simulate(new uNSLinearAddFull(2, 5, 5, log2Up(iter))) { dut =>
      dut.reset.poke(true.B)
      dut.clock.step()
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.in(0).poke(v1.U)
      dut.io.in(1).poke(v2.U)
      dut.io.coeffs(0).poke(w1.S)
      dut.io.coeffs(1).poke(w2.S)
      dut.clock.step()

      var acc = 0
      var v_list: List[Int] = Nil
      for (i <- 0 until iter) {
        acc += dut.io.out.peek().litValue.toInt
        println(s"theoretical: ${dut.io.theoreticalAcc.peek().litValue}")
        println(s"actual: ${dut.io.actualAcc.peek().litValue}")
        // println(s"parallelCounter: ${dut.io.parallelCounter.peek().litValue}")
        println(s"out: ${dut.io.out.peek().litValue}")
        v_list = dut.io.out.peek().litValue.toInt :: v_list
        dut.clock.step()
      }
      println("last")
      println(s"theoretical: ${dut.io.theoreticalAcc.peek().litValue}")
      println(s"actual: ${dut.io.actualAcc.peek().litValue}")
      // println(s"parallelCounter: ${dut.io.parallelCounter.peek().litValue}")
      println(s"acc: $acc, iter: $iter")
      // println(s"v_list: $v_list")

      // val output = acc / iter.toFloat * 2 - 1
      // val expected = (a + b).toFloat / (1 << 3)
      // output must be (expected)
    }
  }
  "uNSADD should calculate 2*3+1*2" in {
    runTestPattern(2, 1, 3, 2, 128)
  }
  // "uNSADD should calculate bipolar 3+3" in {
  //   runTestPattern(3, 3)
  // }
  // "uNSADD should calculate bipolar 3-3" in {
  //   runTestPattern(3, -3)
  // }
  // "uNSADD should calculate bipolar -1-1" in {
  //   runTestPattern(-1, -1)
  // }
  // "uNSADD should calculate bipolar -3-3" in {
  //   runTestPattern(-3, -3)
  // }
}