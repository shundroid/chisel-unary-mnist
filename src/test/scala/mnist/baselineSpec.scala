package mnist

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class BaselineSpec extends AnyFreeSpec with Matchers {
  "baseline should handle mnist" in {
    simulate(new Baseline("basic_tf_quant_2146.json")) { dut =>
      // val json = scala.io.Source.fromFile("test_image.json").mkString
      // val parsed = ujson.read(json)
      // val img = parsed("img").arr(0).arr.map(_.num.toInt)
      // img.zipWithIndex.foreach { case (color, i) =>
      //   dut.io.in(i).poke(color.S)
      // }
      // dut.clock.step()
      // for (i <- 0 until 8) {
      //   val hidden_dim = dut.io.hidden_dim_debug(i).peek().litValue
      //   val correct_hidden_dim = parsed("a1").arr(0).arr(i).num.toInt
      //   // assert to be equal
      //   println(s"hidden_dim($i): $hidden_dim, correct: $correct_hidden_dim")
      //   // hidden_dim must be(correct_hidden_dim)
      // }
      // dut.io.out.zipWithIndex.foreach { case (out, i) =>
      //   println(s"out($i): ${out.peek().litValue}")
      // }
    }
  }
}