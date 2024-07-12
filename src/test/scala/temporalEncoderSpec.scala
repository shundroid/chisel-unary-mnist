package unary

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class TemporalEncoderSpec extends AnyFreeSpec with Matchers {
  "can encode unipolar 2" in {
    simulate(new TemporalEncoder(3, false)) { dut =>
      dut.io.value.poke(2.U)

      dut.reset.poke(true.B)
      dut.clock.step()
      dut.reset.poke(false.B)

      for (i <- 0 until 16) {
        println(s"out: ${dut.io.out.peek().litValue.toInt}")
        dut.clock.step()
      }
    }
  }
  "can encode bipolar 2" in {
    simulate(new TemporalEncoder(3, true)) { dut =>
      dut.io.value.poke(2.U)

      dut.reset.poke(true.B)
      dut.clock.step()
      dut.reset.poke(false.B)

      for (i <- 0 until 16) {
        println(s"out: ${dut.io.out.peek().litValue.toInt}")
        dut.clock.step()
      }
    }
  }
  "can encode bipolar -3" in {
    simulate(new TemporalEncoder(3, true)) { dut =>
      dut.io.value.poke(5.U) // 0b101

      dut.reset.poke(true.B)
      dut.clock.step()
      dut.reset.poke(false.B)

      for (i <- 0 until 16) {
        println(s"out: ${dut.io.out.peek().litValue.toInt}")
        dut.clock.step()
      }
    }
  }
}