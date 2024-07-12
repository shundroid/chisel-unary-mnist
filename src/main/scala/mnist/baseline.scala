package mnist

import chisel3._
import chisel3.util._

class Baseline(paramFile: String) extends Module {
  val size = 16
  // load json
  val json = scala.io.Source.fromFile(paramFile).mkString
  // parse json
  val parsed = ujson.read(json)
  val hidden_dim = parsed("hidden_dim").num.toInt
  val hidden_width = parsed("hidden_width").num.toInt
  val output_width = parsed("output_width").num.toInt

  val io = IO(new Bundle {
    val in = Input(Vec(size * size, SInt(8.W)))
    val out = Output(Vec(10, SInt(output_width.W)))
  })

  val hidden_dim_wire_ = Wire(Vec(hidden_dim, SInt(hidden_width.W)))
  val hidden_dim_wire = Wire(Vec(hidden_dim, SInt(hidden_width.W)))
  for (i <- 0 until hidden_dim) {
    var imm = 0.S(hidden_width.W)
    for (j <- 0 until size * size) {
      imm = imm + io.in(j) * parsed("w1q")(i)(j).num.toInt.S
    }
    imm = imm + parsed("b1q")(i).num.toInt.S
    imm = Mux(imm > 0.S, imm, 0.S) // Relu
    hidden_dim_wire_(i) := imm
    hidden_dim_wire(i) := hidden_dim_wire_(i)
    // io.hidden_dim_debug(i) := hidden_dim_wire(i)
  }
  val hidden_dim_2_wire = Wire(Vec(10, SInt(output_width.W)))
  for (i <- 0 until 10) {
    var imm = 0.S(output_width.W)
    for (j <- 0 until hidden_dim) {
      imm = imm + hidden_dim_wire(j) * parsed("w2q")(i)(j).num.toInt.S
    }
    imm = imm + parsed("b2q")(i).num.toInt.S
    hidden_dim_2_wire(i) := imm
    io.out(i) := hidden_dim_2_wire(i)
  }
}

class BaselineUnified(paramFile: String) extends Module {
  val size = 16
  val output_width = 34
  val io = IO(new Bundle {
    val in = Input(SInt((size * size * 8).W))
    val out = Output(SInt((10 * output_width).W))
  })
  val baseline = Module(new Baseline(paramFile))
  baseline.io.in := io.in.asTypeOf(Vec(size * size, SInt(8.W)))
  io.out := baseline.io.out.asTypeOf(SInt((10 * output_width).W))
}
