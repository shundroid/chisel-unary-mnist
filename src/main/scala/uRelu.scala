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

  val balance = RegInit((1 << (inWidth - 1)).U(inWidth.W))
  when (io.o & (~balance.andR)) {
    balance := balance + 1.U
  }.elsewhen(~io.o & balance.orR) {
    balance := balance - 1.U
  }

  val blink = RegInit(0.B)
  blink := ~blink

  // io.o := Mux(overHalf, io.i, Mux(balance(inWidth - 1), 0.B, 1.B))
  io.o := Mux(overHalf, io.i, blink)
  io.overhalf := overHalf
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

class uReluFullTemporal(inWidth: Int) extends Module {
  val io = IO(new Bundle {
    val i = Input(SInt(inWidth.W))
    val o = Output(Bool())
    val overhalf = Output(Bool())
    val rngOut = Output(SInt(inWidth.W))
  })

  val rng = Module(new TemporalRNG(inWidth, true))
  rng.io.en := 1.B
  val relu = Module(new uRelu(inWidth))
  relu.io.i := io.i > rng.io.out.asSInt
  io.o := relu.io.o
  io.overhalf := relu.io.overhalf
  io.rngOut := rng.io.out.asSInt
}