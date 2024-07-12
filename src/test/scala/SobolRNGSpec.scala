package unary

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class SobolRNGSpec extends AnyFreeSpec with Matchers {
  "SobolRNG should generate random numbers" in {
    simulate(new SobolRNGDim1(8)) { dut =>
      dut.reset.poke(true.B)
      dut.clock.step()
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.en.poke(true.B)

      var vs: List[Int] = Nil
      for (i <- 0 until 1024) {
        println(s"cnt: ${dut.io.cnt.peek().litValue}, vecIdx: ${dut.io.idx.peek().litValue}, sobolSeq: ${dut.io.sobolSeq.peek().litValue}")
        vs = dut.io.sobolSeq.peek().litValue.toInt :: vs
        dut.clock.step()
      }
      println(s"vs: [${vs.reverse.mkString(", ")}]")
    }
  }
}