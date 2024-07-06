package unary

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class uMULSpec extends AnyFreeSpec with Matchers {
  "uMUL should calculate 4/8 x 4/8" in {
    simulate(new uMULFull(3)) { dut =>
      dut.reset.poke(true.B)
      dut.clock.step()
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.iA.poke(3.U)
      dut.io.iB.poke(4.U)
      dut.io.loadA.poke(true.B)
      dut.io.loadB.poke(true.B)
      dut.clock.step()

      dut.io.loadA.poke(false.B)
      dut.io.loadB.poke(false.B)

      for (i <- 0 until 32) {
        println(s"oC: ${dut.io.oC.peek().litValue}")
        dut.clock.step()
      }
    }
  }
}
