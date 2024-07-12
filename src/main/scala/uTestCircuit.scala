package unary

import chisel3._
import chisel3.util._

class uTestCircuit extends Module {
  val width = 10
  val io = IO(new Bundle {
    val in = Input(Bool())
    val out = Output(Bool())
  })
  val mul1 = Module(new uMUL_10)
  mul1.io.iA := io.in
  mul1.io.iB := (256-99).U
  mul1.io.loadB := 1.B
  val rng = Module(new SobolRNGDim1_10)
  rng.io.en := 1.B
  rng.io.threshold := (256-3).U
  val add1 = Module(new uSADD(2))
  add1.io.in := Cat(rng.io.value, mul1.io.oC)
  val mul2 = Module(new uMUL_1_256)
  mul2.io.iA := add1.io.out
  io.out := mul2.io.oC
}

class uTestCircuitRepeat(repeat: Int) extends Module {
  val width = 10
  val io = IO(new Bundle {
    val in = Input(Bool())
    val out = Output(Bool())
  })
  val wires = Wire(Vec(repeat + 1, Bool()))
  wires(0) := io.in
  for (i <- 0 until repeat) {
    val baseline = Module(new uTestCircuit)
    baseline.io.in := wires(i)
    wires(i + 1) := baseline.io.out
  }
  io.out := wires(repeat)
}
