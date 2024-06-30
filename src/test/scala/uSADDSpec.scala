import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class uSADDSpec extends AnyFreeSpec with Matchers {
  "uSADD should calculate 4/8 + 4/8" in {
    simulate(new uSADDFull(3)) { dut =>
      dut.reset.poke(true.B)
      dut.clock.step()
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.iA.poke(2.U)
      dut.io.iB.poke(4.U)
      dut.io.loadA.poke(true.B)
      dut.io.loadB.poke(true.B)
      dut.clock.step()
      dut.io.loadA.poke(false.B)
      dut.io.loadB.poke(false.B)

      var acc = 0
      for (i <- 0 until 16) {
        acc += dut.io.oC.peek().litValue.toInt
        println(s"oC: ${dut.io.oC.peek().litValue}")
        dut.clock.step()
      }
      println(s"acc: $acc")
    }
  }
}

// class uSADDSpec extends AnyFlatSpec {
//   behavior of "uSADDFull"
//   it should "do something" in {
//     test(new uSADDFull(3)) { c =>
//     }
//   }
    // simulate(new uSADDFull(3)) { dut =>
    //   dut.reset.poke(true.B)
    //   dut.clock.step()
    //   dut.reset.poke(false.B)
    //   dut.clock.step()

    //   dut.io.iA.poke(4.U)
    //   dut.io.iB.poke(8.U)
    //   dut.io.loadA.poke(true.B)
    //   dut.io.loadB.poke(true.B)
    //   dut.clock.step()
    //   dut.io.loadA.poke(false.B)
    //   dut.io.loadB.poke(false.B)

    //   for (i <- 0 until 32) {
    //     println(s"oC: ${dut.io.oC.peek().litValue}")
    //     dut.clock.step()
    //   }
    // }
// }