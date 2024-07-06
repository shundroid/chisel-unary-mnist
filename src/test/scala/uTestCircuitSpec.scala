import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class uTestCircuitSpec extends AnyFreeSpec with Matchers {
  // "uTestCircuit should do something" in {
  //   simulate(new uTestCircuitFull) { dut =>
  //     dut.reset.poke(true.B)
  //     dut.clock.step()
  //     dut.reset.poke(false.B)
  //     dut.clock.step()

  //     var acc = 0
  //     var lst: List[Int] = Nil
  //     for (i <- 0 until 256) {
  //       lst = (dut.io.out.peek().litValue.toInt) :: lst
  //       // acc += dut.io.out.peek().litValue.toInt
  //       // println(s"out: ${dut.io.out.peek().litValue}")
  //       dut.clock.step()
  //     }
  //     println(s"acc: $lst")
  //   }
  // }
}