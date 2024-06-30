//> using scala "2.13.12"
//> using dep "org.chipsalliance::chisel:6.4.0"
//> using plugin "org.chipsalliance:::chisel-plugin:6.4.0"
//> using options "-unchecked", "-deprecation", "-language:reflectiveCalls", "-feature", "-Xcheckinit", "-Xfatal-warnings", "-Ywarn-dead-code", "-Ywarn-unused", "-Ymacro-annotations"

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class uSADDFull(inWidth: Int) extends Module {
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
  val iBBuf = RegInit(0.U(inWidth.W))
  when (io.loadB) {
    iBBuf := io.iB
  }
  val rng = Module(new SobolRNGDim1(inWidth))
  val sobolSeq = Wire(UInt(inWidth.W))
  rng.io.en := 1.B
  sobolSeq := rng.io.sobolSeq
  val sadd = Module(new uSADD(2))
  val vec = Wire(Vec(2, Bool()))
  vec(0) := iABuf > sobolSeq
  vec(1) := iBBuf > sobolSeq
  sadd.io.in := vec.asUInt
  io.oC := sadd.io.out
}

// object Main extends App {
//   println(
//     ChiselStage.emitSystemVerilog(
//       gen = new uSADDFull(3),
//       firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
//     )
//   )
// }
