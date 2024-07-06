import chisel3._
import chisel3.util._

class uBaseline extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(10.W))
    val out = Output(UInt(10.W))
  })
  io.out := ((io.in * 198.U + 3.U * 256.U) * 2.U)
  val test = Wire(UInt(10.W))
  val test2 = Wire(UInt(3.W))
  test := test2
  test2 := test
}

// class uBaselineRepeat(repeat: Int) extends Module {
//   val io = IO(new Bundle {
//     val in = Input(UInt(3.W))
//     val out = Output(UInt(3.W))
//   })
//   val wires = Wire(Vec(repeat + 1, UInt(3.W)))
//   wires(0) := io.in
//   for (i <- 0 until repeat) {
//     val baseline = Module(new uBaseline)
//     baseline.io.in := wires(i)
//     wires(i + 1) := baseline.io.out
//   }
//   io.out := wires(repeat)
// }
