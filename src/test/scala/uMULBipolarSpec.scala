package unary

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class uMULBipolarSpec extends AnyFreeSpec with Matchers {
  def runTestPattern(a: Int, b: Int, width: Int, iter: Int = 256): Unit = {
    simulate(new uMULBipolarFull(width)) { dut =>
      dut.reset.poke(true.B)
      dut.clock.step()
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.iA.poke(a.S)
      dut.io.iB.poke(b.S)
      dut.io.loadA.poke(true.B)
      dut.io.loadB.poke(true.B)
      dut.clock.step()

      dut.io.loadA.poke(false.B)
      dut.io.loadB.poke(false.B)

      var acc = 0
      for (i <- 0 until iter) {
        acc += dut.io.oC.peek().litValue.toInt
        dut.clock.step()
      }

      val output = acc / iter.toFloat * 2 - 1
      val expected = a.toFloat / (1 << (width - 1)) * b / (1 << (width - 1))
      output must be(expected)
    }
  }
  "uMUL should calculate 0x0" in {
    runTestPattern(0, 0, 4)
  }
  "uMUL should calculate 4/8 x 2/8" in {
    runTestPattern(4, 2, 4)
  }
  "uMUL should calculate -2/8 x 4/8" in {
    runTestPattern(-2, 4, 4)
  }
  "uMUL should calculate 2/8 x -4/8" in {
    runTestPattern(2, -4, 4)
  }
}
