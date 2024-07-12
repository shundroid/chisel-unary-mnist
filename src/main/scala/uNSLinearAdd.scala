package unary

import chisel3._
import chisel3.util._

class uNSLinearAdd(n: Int, m: Int, l: Int, bipolar: Boolean) extends Module {
  val logN = log2Up(n)
  val parallelCounterWidth = m * logN + 2
  val accWidth = m * logN * l + 2 // todo: maybe overflow
  val io = IO(new Bundle {
    val in = Input(Vec(n, Bool()))
    val coeffs = Input(Vec(n, SInt(m.W)))
    val out = Output(Bool())
    val theoreticalAcc = Output(SInt(accWidth.W))
    val actualAcc = Output(SInt(accWidth.W))
  })

  val parallelCounter = Wire(SInt(parallelCounterWidth.W))
  val parallelCounterPartial = Wire(Vec(n, SInt(parallelCounterWidth.W)))
  parallelCounterPartial(0) := Mux(io.in(0), io.coeffs(0), 0.S)
  for (i <- 1 until n) {
    parallelCounterPartial(i) := parallelCounterPartial(i - 1) + Mux(io.in(i), io.coeffs(i), 0.S)
  }
  parallelCounter := parallelCounterPartial(n - 1)

  val theoreticalAcc = RegInit(0.S(accWidth.W))
  theoreticalAcc := theoreticalAcc + parallelCounter

  val actualAcc = RegInit(0.S(accWidth.W))
  actualAcc := actualAcc + io.out.zext

  io.out := theoreticalAcc > actualAcc
  io.theoreticalAcc := theoreticalAcc
  io.actualAcc := actualAcc
}

class uNSLinearAddFull(n: Int, bitWidth1: Int, bitWidth2: Int, l: Int) extends Module {
  val accWidth = bitWidth2 * log2Up(n) * l + 2 // todo: maybe overflow
  val io = IO(new Bundle {
    val in = Input(Vec(n, UInt(bitWidth1.W)))
    val coeffs = Input(Vec(n, SInt(bitWidth2.W)))
    val out = Output(Bool())
    val theoreticalAcc = Output(SInt(accWidth.W))
    val actualAcc = Output(SInt(accWidth.W))
  })
  val m = Module(new uNSLinearAdd(n, bitWidth2, l, false))
  for (i <- 0 until n) {
    val rng = Module(new SobolRNGDim1(bitWidth1))
    rng.io.en := 1.B
    m.io.in(i) := io.in(i) > rng.io.sobolSeq
    m.io.coeffs(i) := io.coeffs(i)
  }
  io.out := m.io.out
  io.theoreticalAcc := m.io.theoreticalAcc
  io.actualAcc := m.io.actualAcc
}