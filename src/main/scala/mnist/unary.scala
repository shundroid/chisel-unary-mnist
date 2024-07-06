package mnist

import chisel3._
import chisel3.util._
import unary.SobolRNGDim1
import unary.uMULBipolar
import unary.uNSADD
import unary.uRelu

class Unary(paramFile: String) extends Module {
  val size = 16 // image width & height

  // load json
  val json = scala.io.Source.fromFile(paramFile).mkString
  // parse json
  val parsed = ujson.read(json)
  val hidden_dim = parsed("hidden_dim").num.toInt
  val max_width_1 = parsed("max_width_1").num.toInt
  val max_width_2 = parsed("max_width_2").num.toInt
  // note: don't need shift_amount

  val io = IO(new Bundle {
    val in = Input(Vec(size * size, Bool()))
    val out = Output(Vec(10, Bool()))
  })
  val mitigation1 = 2
  val mitigation2 = 0

  val hidden_dim_wire = Wire(Vec(hidden_dim, Bool()))
  for (i <- 0 until hidden_dim) {
    val in = Wire(Vec(size * size + 1, Bool()))
    for (j <- 0 until size * size) {
      val mul = Module(new uMULBipolar(8 + mitigation1))
      mul.io.iA := io.in(j)
      mul.io.iB := parsed("w1q")(i)(j).num.toInt.S
      mul.io.loadB := 1.B
      in(j) := mul.io.oC
    }
    val bias = Module(new SobolRNGDim1(8 + 8 + mitigation1))
    bias.io.en := 1.B
    in(size * size) := parsed("b1q")(i).num.toInt.S > bias.io.sobolSeq.asSInt
    val nsadd = Module(new uNSADD(size * size + 1, 10, true))
    nsadd.io.in := in
    val relu = Module(new uRelu(8 + 8 + mitigation1))
    relu.io.i := nsadd.io.out
    hidden_dim_wire(i) := relu.io.o
  }

  for (i <- 0 until 10) {
    val in = Wire(Vec(hidden_dim + 1, Bool()))
    for (j <- 0 until hidden_dim) {
      val mul = Module(new uMULBipolar(8 + mitigation2))
      mul.io.iA := hidden_dim_wire(j)
      mul.io.iB := parsed("w2q")(i)(j).num.toInt.S
      mul.io.loadB := 1.B
      in(j) := mul.io.oC
    }
    val bias = Module(new SobolRNGDim1(8 + 8 + mitigation1 + 8 + mitigation2))
    bias.io.en := 1.B
    in(hidden_dim) := parsed("b2q")(i).num.toInt.S > bias.io.sobolSeq.asSInt
    val nsadd = Module(new uNSADD(hidden_dim + 1, 10, true))
    nsadd.io.in := in
    io.out(i) := nsadd.io.out
  }
}

class UnaryWithInput(paramFile: String, inputFile: String) extends Module {
  val io = IO(new Bundle {
    val out = Output(Vec(10, Bool()))
  })

  val unary = Module(new Unary(paramFile))
  val json = scala.io.Source.fromFile("test_image.json").mkString
  val parsed = ujson.read(json)
  val img = parsed("img").arr(0).arr.map(_.num.toInt)
  img.zipWithIndex.foreach { case (color, i) =>
    val rng = Module(new SobolRNGDim1(8))
    rng.io.en := 1.B
    unary.io.in(i) := color.S > rng.io.sobolSeq.asSInt
  }

  io.out := unary.io.out
}