package unary

import chisel3._
import chisel3.util._

class uNSADD(n: Int, l: Int, bipolar: Boolean) extends Module {
  val io = IO(new Bundle {
    val in = Input(Vec(n, Bool()))
    val out = Output(Bool())
    val theoreticalAcc = Output(SInt((l*log2Up(n)+2).W))
    val actualAcc = Output(SInt((l*log2Up(n)+2).W))
    val parallelCounter = Output(UInt((log2Up(n)+1).W))
  })

  val logN = log2Up(n)
  val parallelCounter = Wire(UInt((logN+1).W))
  val parallelCounterPartial = Wire(Vec(n, UInt((logN+1).W)))
  parallelCounterPartial(0) := io.in(0)
  for (i <- 1 until n) {
    parallelCounterPartial(i) := parallelCounterPartial(i-1) + io.in(i)
  }
  parallelCounter := parallelCounterPartial(n-1)

  val theoreticalAcc = RegInit(0.S((l*log2Up(n)+2).W))
  val offset = if (bipolar) n - 1 else 0
  theoreticalAcc := theoreticalAcc + (parallelCounter.zext << 1.U) - offset.S

  val actualAcc = RegInit(0.S((l*log2Up(n)+2).W))
  actualAcc := actualAcc + (io.out.zext << 1.U)

  io.out := theoreticalAcc > actualAcc

  io.theoreticalAcc := theoreticalAcc
  io.actualAcc := actualAcc
  io.parallelCounter := parallelCounter
}

class uNSADDFull(n: Int, l: Int, logIter: Int, bipolar: Boolean) extends Module {
  val io = IO(new Bundle {
    val in = Input(Vec(n, SInt(l.W)))
    val out = Output(Bool())
    val theoreticalAcc = Output(SInt((logIter*log2Up(n)+2).W))
    val actualAcc = Output(SInt((logIter*log2Up(n)+2).W))
    val parallelCounter = Output(UInt((log2Up(n)+1).W))
  })

  val sad = Module(new uNSADD(n, logIter, bipolar))
  for (i <- 0 until n) {
    val rng = Module(new SobolRNGDim1(l))
    rng.io.en := 1.B
    if (bipolar) {
      sad.io.in(i) := io.in(i) > rng.io.sobolSeq.asSInt
    } else {
      sad.io.in(i) := io.in(i) > rng.io.sobolSeq.zext
    }
  }
  io.out := sad.io.out

  io.theoreticalAcc := sad.io.theoreticalAcc
  io.actualAcc := sad.io.actualAcc
  io.parallelCounter := sad.io.parallelCounter
}