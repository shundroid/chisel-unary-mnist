//> using scala "2.13.12"
//> using dep "org.chipsalliance::chisel:6.4.0"
//> using plugin "org.chipsalliance:::chisel-plugin:6.4.0"
//> using options "-unchecked", "-deprecation", "-language:reflectiveCalls", "-feature", "-Xcheckinit", "-Xfatal-warnings", "-Ywarn-dead-code", "-Ywarn-unused", "-Ymacro-annotations"

import chisel3._
import chisel3.util._
// _root_ disambiguates from package chisel3.util.circt if user imports chisel3.util._
import _root_.circt.stage.ChiselStage

class SobolRNG(inWidth: Int) extends Module {
  val io = IO(new Bundle {
    val en = Input(Bool())
    val vecIdx = Input(UInt(log2Up(inWidth).W))
    val dirVec = Input(Vec(inWidth, UInt(inWidth.W)))
    val out = Output(UInt(inWidth.W))
  })

  val reg = RegInit(0.U(inWidth.W))
  io.out := reg

  when(io.en) {
    reg := io.dirVec(io.vecIdx) ^ reg
  }
}

class LSZ(inWidth: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(inWidth.W))
    val lszIdx = Output(UInt(log2Up(inWidth).W))
  })

  // val inacc = Wire(Vec(inWidth, Bool()))
  // inacc(0) :- ~io.in(0)
  // for (i <- 1 until inWidth) {
  //   inacc(i) :- inacc(i - 1) | ~io.in(i)
  // }

  // val outoh = Wire(Vec(inWidth, Bool()))
  // outoh(0) :- inacc(0)
  // for (i <- 1 until inWidth) {
  //   outoh(i) :- inacc(i - 1) ^ inacc(i)
  // }

  // val outohInt = 
  inWidth match {
    case 3 => io.lszIdx := Mux(io.in(0), Mux(io.in(1), 2.U, 1.U), 0.U)
  }
}

class SobolRNGDim1(inWidth: Int) extends Module {
  val io = IO(new Bundle {
    val en = Input(Bool())
    val sobolSeq = Output(UInt(inWidth.W))
    val cnt = Output(UInt(inWidth.W))
    val idx = Output(UInt(log2Up(inWidth).W))
  })

  val cnt = RegInit(0.U(inWidth.W))
  when(io.en) {
    cnt := cnt + 1.U
  }

  val lsz = Module(new LSZ(inWidth))
  lsz.io.in := cnt

  val sobolRNG = Module(new SobolRNG(inWidth))
  sobolRNG.io.en := io.en
  inWidth match {
    case 3 => sobolRNG.io.dirVec := VecInit(Seq(4.U, 6.U, 7.U))
    // case 4 => VecInit(Seq(8.U, 4.U, 2.U, 1.U))
  }
  sobolRNG.io.vecIdx := lsz.io.lszIdx
  io.sobolSeq := sobolRNG.io.out

  io.cnt := cnt
  io.idx := lsz.io.lszIdx
}

// object Main extends App {
//   println(
//     ChiselStage.emitSystemVerilog(
//       gen = new SobolRNGDim1(3),
//       firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
//     )
//   )
// }
