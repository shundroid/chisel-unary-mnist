import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class SobolRNGSpec extends AnyFreeSpec with Matchers {
  "SobolRNG should generate random numbers" in {
    simulate(new SobolRNGDim1(3)) { dut =>
      dut.reset.poke(true.B)
      dut.clock.step()
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.en.poke(true.B)

      for (i <- 0 until 10) {
        println(s"cnt: ${dut.io.cnt.peek().litValue}, vecIdx: ${dut.io.idx.peek().litValue}, sobolSeq: ${dut.io.sobolSeq.peek().litValue}")
        dut.clock.step()
      }
    }
  }
}