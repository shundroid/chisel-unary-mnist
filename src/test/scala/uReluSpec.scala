package unary

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class uReluSpec extends AnyFreeSpec with Matchers {
  def runTestPattern(a: Int, width: Int, iter: Int = 1024): Unit = {
    simulate(new uReluFull(width)) { dut =>
      dut.reset.poke(true.B)
      dut.clock.step()
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.i.poke(a.S)
      dut.clock.step()

      var acc = 0
      for (i <- 0 until iter) {
        acc += dut.io.o.peek().litValue.toInt
        // println(s"overhalf: ${dut.io.overhalf.peek().litValue}")
        // println(s"v: ${dut.io.o.peek().litValue}")
        dut.clock.step()
      }

      val output = acc / iter.toFloat * 2 - 1
      val expected = if (a > 0) a.toFloat / (1 << (width - 1)) else 0
      println(s"output: $output")
      output must be (expected)
    }
  }
  def runTestPatternTemporal(a: Int, width: Int, iter: Int = 1024): Unit = {
    simulate(new uReluFullTemporal(width)) { dut =>
      dut.io.i.poke(a.S)

      dut.reset.poke(true.B)
      dut.clock.step()
      dut.reset.poke(false.B)

      var acc = 0
      for (i <- 0 until iter) {
        acc += dut.io.o.peek().litValue.toInt
        dut.clock.step()
      }

      val output = acc / iter.toFloat * 2 - 1
      val expected = if (a > 0) a.toFloat / (1 << (width - 1)) else 0
      println(s"output: $output")
      output must be (expected)
    }
  }
  "uRelu should calculate 1" in {
    runTestPattern(1, 4)
  }
  "uRelu should calculate -1" in {
    runTestPattern(-1, 4)
  }
  "uRelu should calculate 0" in {
    runTestPattern(0, 4)
  }
  "uRelu should calculate -1 temporal" in {
    runTestPatternTemporal(-1, 4)
  }
}