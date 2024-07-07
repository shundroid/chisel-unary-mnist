package unary

import chisel3._
import chisel3.util._

class uMULBipolarLite(inWidth: Int) extends Module {
  val io = IO(new Bundle {
    val iA = Input(Bool())
    val iB = Input(SInt(inWidth.W))
    val loadB = Input(Bool())
    val sobolSeqP = Input(SInt(inWidth.W))
    val sobolSeqN = Input(SInt(inWidth.W))
    val oC = Output(Bool())
  })

  val iBBuf = RegInit(0.S(inWidth.W))
  when (io.loadB) {
    iBBuf := io.iB
  }

  io.oC := (io.iA & (iBBuf > io.sobolSeqP)) | (~io.iA & (iBBuf <= io.sobolSeqN))
}

class uMULBipolar(inWidth: Int) extends Module {
  val io = IO(new Bundle {
    val iA = Input(Bool())
    val iB = Input(SInt(inWidth.W))
    val loadB = Input(Bool())
    val oC = Output(Bool())
  })

  val rng1 = Module(new SobolRNGDim1(inWidth))
  rng1.io.en := io.iA

  val rng2 = Module(new SobolRNGDim1(inWidth))
  rng2.io.en := ~io.iA

  val lite = Module(new uMULBipolarLite(inWidth))
  lite.io.iA := io.iA
  lite.io.iB := io.iB
  lite.io.loadB := io.loadB
  lite.io.sobolSeqP := rng1.io.sobolSeq.asSInt
  lite.io.sobolSeqN := rng2.io.sobolSeq.asSInt
  io.oC := lite.io.oC
}

// for testing
class uMULBipolarFull(inWidth: Int) extends Module {
  val io = IO(new Bundle {
    val iA = Input(SInt(inWidth.W))
    val loadA = Input(Bool())
    val iB = Input(SInt(inWidth.W))
    val loadB = Input(Bool())
    val oC = Output(Bool())

    val test_iA = Output(Bool())
  })

  val iABuf = RegInit(0.S(inWidth.W))
  when (io.loadA) {
    iABuf := io.iA
  }

  val rng = Module(new SobolRNGDim1(inWidth))
  rng.io.en := 1.B

  val um = Module(new uMULBipolar(inWidth))
  um.io.iA := iABuf > rng.io.sobolSeq.asSInt
  um.io.iB := io.iB
  um.io.loadB := io.loadB
  io.oC := um.io.oC
  io.test_iA := um.io.iA
}
