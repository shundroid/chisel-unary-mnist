package unary

import chisel3._
import chisel3.util._

class uMULFull(inWidth: Int) extends Module {
  val io = IO(new Bundle {
    val iA = Input(UInt(inWidth.W))
    val loadA = Input(Bool())
    val iB = Input(UInt(inWidth.W))
    val loadB = Input(Bool())
    val oC = Output(Bool())
  })

  val iABuf = RegInit(0.U(inWidth.W))
  when (io.loadA) {
    iABuf := io.iA
  }

  val sobolSeq = Wire(UInt(inWidth.W))
  val rng = Module(new SobolRNGDim1(inWidth))
  rng.io.en := 1.B
  sobolSeq := rng.io.sobolSeq

  val um = Module(new uMUL(inWidth))
  um.io.iA := iABuf > sobolSeq
  um.io.iB := io.iB
  um.io.loadB := io.loadB
  io.oC := um.io.oC
}