package unary

import chisel3._
import chisel3.util._

class uSADD(inWidth: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(inWidth.W))
    val out = Output(Bool())
  })

  val logWidth = log2Up(inWidth)
  val parallelCounter = Wire(UInt((logWidth+1).W))
  val parallelCounterPartial = Wire(Vec(inWidth, UInt((logWidth+1).W)))
  parallelCounterPartial(0) := io.in(0)
  for (i <- 1 until inWidth) {
    parallelCounterPartial(i) := parallelCounterPartial(i-1) + io.in(i)
  }
  parallelCounter := parallelCounterPartial(inWidth-1)

  val acc = RegInit(0.U(logWidth.W))
  val accNext = Wire(UInt((logWidth+1).W))
  accNext := acc + parallelCounter
  acc := accNext(logWidth-1, 0)
  io.out := accNext(logWidth)
}
