package mnist

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class UnarySpec extends AnyFreeSpec with Matchers {
  "unary should handle mnist" in {
    simulate(new UnaryWithInput("basic_tf_quant_2146.json", "test_image.json")) { dut =>
      println("test started")

      dut.reset.poke(true.B)
      dut.clock.step()
      dut.reset.poke(false.B)
      dut.clock.step()

      // create 10 lists
      val outputs = Array.fill(10)(List.empty[Int])
      for (i <- 0 until 1024) {
        for (j <- 0 until 10) {
          outputs(j) = dut.io.out(j).peek().litValue.toInt :: outputs(j)
        }
      }
      println("{")
      for (j <- 0 until 10) {
        println(s"\"$j\": [${outputs(j).mkString(", ")}]")
      }
      println("}")
    }
  }
}