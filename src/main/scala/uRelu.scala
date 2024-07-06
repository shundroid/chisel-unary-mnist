package unary

import chisel3._
import chisel3.util._

class uRelu(inWidth: Int) extends Module {
  val io = IO(new Bundle {
    val i = Input(Bool())
    val o = Output(Bool())
    val overhalf = Output(Bool())
  })

  val cnt = RegInit((1 << (inWidth - 1)).U(inWidth.W))
  when (io.i & (~cnt.andR)) {
    cnt := cnt + 1.U
  }.elsewhen (~io.i & cnt.orR) {
    cnt := cnt - 1.U
  }

  val overHalf = Wire(Bool())
  overHalf := cnt(inWidth - 1)
  io.overhalf := overHalf

  val blink = RegInit(0.B)
  blink := ~blink

  io.o := Mux(overHalf, io.i, blink)
}

// for testing
class uReluFull(inWidth: Int) extends Module {
  val io = IO(new Bundle {
    val i = Input(SInt(inWidth.W))
    val o = Output(Bool())
    val overhalf = Output(Bool())
  })

  val rng = Module(new SobolRNGDim1(inWidth))
  rng.io.en := 1.B
  val relu = Module(new uRelu(inWidth))
  relu.io.i := io.i > rng.io.sobolSeq.asSInt
  io.o := relu.io.o
  io.overhalf := relu.io.overhalf
}