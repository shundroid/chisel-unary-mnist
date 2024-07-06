package unary

import chisel3._
import chisel3.util._

class uMUL(inWidth: Int) extends Module {
  val io = IO(new Bundle {
    val iA = Input(Bool())
    val iB = Input(UInt(inWidth.W))
    val loadB = Input(Bool())
    val oC = Output(Bool())
  })

  val iBBuf = RegInit(0.U(inWidth.W))
  val sobolSeq = Wire(UInt(inWidth.W))

  val rng = Module(new SobolRNGDim1(inWidth))
  rng.io.en := io.iA
  sobolSeq := rng.io.sobolSeq

  when (io.loadB) {
    iBBuf := io.iB
  }

  io.oC := io.iA & (iBBuf > sobolSeq)
}

class uMUL_10 extends Module {
  val inWidth = 10
  val io = IO(new Bundle {
    val iA = Input(Bool())
    val iB = Input(UInt((inWidth-2).W))
    val loadB = Input(Bool())
    val oC = Output(Bool())
  })

  val iBBuf = RegInit(0.U((inWidth-2).W))
  val rnd = Module(new SobolRNGDim1_10)
  rnd.io.en := io.iA
  rnd.io.threshold := iBBuf

  when (io.loadB) {
    iBBuf := io.iB
  }

  io.oC := io.iA & rnd.io.value
}

class uMUL_1_256 extends Module {
  val io = IO(new Bundle {
    val iA = Input(Bool())
    val oC = Output(Bool())
  })

  val cnt = RegInit(0.U(8.W))
  when (io.iA) {
    cnt := cnt + 1.U
  }

  io.oC := io.iA & (cnt === 255.U)
}