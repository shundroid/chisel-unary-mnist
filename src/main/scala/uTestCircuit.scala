import chisel3._
import chisel3.util._

class uTestCircuit extends Module {
  val width = 10
  val io = IO(new Bundle {
    val in = Input(Bool())
    val sobolSeq = Input(UInt(width.W))
    val out = Output(Bool())
  })
  val mul1 = Module(new uMUL(width))
  mul1.io.iA := io.in
  mul1.io.iB := 198.U
  mul1.io.loadB := 1.B
  val add1 = Module(new uSADD(2))
  add1.io.in := Cat(3.U > io.sobolSeq, mul1.io.oC)
  val mul2 = Module(new uMUL(width))
  mul2.io.iA := add1.io.out
  mul2.io.iB := 4.U
  mul2.io.loadB := 1.B
  io.out := mul2.io.oC
}

class uTestCircuitRepeat(repeat: Int) extends Module {
  val width = 10
  val io = IO(new Bundle {
    val in = Input(Bool())
    val out = Output(Bool())
  })
  val rng = Module(new SobolRNGDim1(width))
  rng.io.en := 1.B
  val wires = Wire(Vec(repeat + 1, Bool()))
  wires(0) := io.in
  for (i <- 0 until repeat) {
    val baseline = Module(new uTestCircuit)
    baseline.io.in := wires(i)
    baseline.io.sobolSeq := rng.io.sobolSeq
    wires(i + 1) := baseline.io.out
  }
  io.out := wires(repeat)
}

// class uTestCircuitFull extends Module {
//   val width = 3
//   val io = IO(new Bundle {
//     val out = Output(Bool())
//   })
//   val rng = Module(new SobolRNGDim1(width))
//   rng.io.en := 1.B
//   val c = Module(new uTestCircuit(width))
//   c.io.in := 6.U > rng.io.sobolSeq
//   io.out := c.io.out
// }
