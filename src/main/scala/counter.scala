package unary
import chisel3._
import chisel3.util._

class Counter extends Module {
  val width = 3
  val io = IO(new Bundle {
    val out = Output(UInt(width.W))
  })
  val c = RegInit(3.U(width.W))
  c := c + 1.U
  io.out := c
}