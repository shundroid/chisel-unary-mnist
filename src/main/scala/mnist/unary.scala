package mnist

import chisel3._
import chisel3.util._
import unary.SobolRNGDim1
import unary.uMULBipolarLite
import unary.uNSADD
import unary.uRelu
import unary.TemporalEncoder
import unary.uMULBipolarSuperLite
import unary.TemporalRNG
import unary.UEncoder

class Unary(paramFile: String, temporal: Boolean) extends Module {
  val size = 16 // image width & height

  // load json
  val json = scala.io.Source.fromFile(paramFile).mkString
  // parse json
  val parsed = ujson.read(json)
  val hidden_dim = parsed("hidden_dim").num.toInt
  // val max_width_1 = parsed("max_width_1").num.toInt
  // val max_width_2 = parsed("max_width_2").num.toInt
  // note: don't need shift_amount

  val io = IO(new Bundle {
    val in = Input(Vec(size * size, Bool()))
    val out = Output(Vec(10, Bool()))
  })
  val mitigation1 = parsed("hidden_width").num.toInt - 8 - 8
  val mitigation2 = 0 // parsed("output_width").num.toInt - 8 - mitigation1 - 8 - 8
  println(s"mitigation1: $mitigation1")
  println(s"mitigation2: $mitigation2")

  val input_rng_positives = Wire(Vec(size * size, SInt((8 + mitigation1).W)))
  val input_rng_negatives = Wire(Vec(size * size, SInt((8 + mitigation1).W)))
  for (i <- 0 until size * size) {
      // val rng1 = Module(new TemporalRNG(8 + mitigation1, true))
      // rng1.io.en := io.in(i)
      // input_rng_positives(i) := rng1.io.out.asSInt
      // val rng2 = Module(new TemporalRNG(8 + mitigation1, true))
      // rng2.io.en := ~io.in(i)
      // input_rng_negatives(i) := rng2.io.out.asSInt
    val rng1 = Module(new SobolRNGDim1(8 + mitigation1))
    rng1.io.en := io.in(i)
    input_rng_positives(i) := rng1.io.sobolSeq.asSInt
    val rng2 = Module(new SobolRNGDim1(8 + mitigation1))
    rng2.io.en := ~io.in(i)
    input_rng_negatives(i) := rng2.io.sobolSeq.asSInt
  }

  val hidden_dim_wire = Wire(Vec(hidden_dim, Bool()))
  for (i <- 0 until hidden_dim) {
    val in = Wire(Vec(size * size + 1, Bool()))
    for (j <- 0 until size * size) {
      val mul = Module(new uMULBipolarLite(8 + mitigation1))
      mul.io.iA := io.in(j)
      mul.io.iB := parsed("w1q")(i)(j).num.toInt.S
      mul.io.loadB := 1.B
      mul.io.sobolSeqP := input_rng_positives(j)
      mul.io.sobolSeqN := input_rng_negatives(j)
      in(j) := mul.io.oC
    }
    // val biasEnc = Module(new UEncoder(8 + 8 + mitigation1, temporal, true))
    // biasEnc.io.en := 1.B
    // biasEnc.io.value := parsed("b1q")(i).num.toInt.S.asUInt
    // in(size * size) := biasEnc.io.out
    if (false) {
      val bias = Module(new TemporalRNG(8 + 8 + mitigation1, true))
      bias.io.en := 1.B
      in(size * size) := parsed("b1q")(i).num.toInt.S + 1.S > bias.io.out.asSInt
    } else {
      val bias = Module(new SobolRNGDim1(8 + 8 + mitigation1))
      bias.io.en := 1.B
      in(size * size) := parsed("b1q")(i).num.toInt.S > bias.io.sobolSeq.asSInt
    }
    val nsadd = Module(new uNSADD(size * size + 1, 10, true))
    nsadd.io.in := in
    val relu = Module(new uRelu(8 + 8 + mitigation1))
    relu.io.i := nsadd.io.out
    hidden_dim_wire(i) := relu.io.o
    // io.out(i) := relu.io.o
  }
  // io.out(8) := 0.B
  // io.out(9) := 0.B

  val hidden_rng_positives = Wire(Vec(hidden_dim, SInt((8 + mitigation2).W)))
  val hidden_rng_negatives = Wire(Vec(hidden_dim, SInt((8 + mitigation2).W)))
  for (i <- 0 until hidden_dim) {
    val rng1 = Module(new SobolRNGDim1(8 + mitigation2))
    rng1.io.en := hidden_dim_wire(i)
    hidden_rng_positives(i) := rng1.io.sobolSeq.asSInt
    val rng2 = Module(new SobolRNGDim1(8 + mitigation2))
    rng2.io.en := ~hidden_dim_wire(i)
    hidden_rng_negatives(i) := rng2.io.sobolSeq.asSInt
  }

  for (i <- 0 until 10) {
    val in = Wire(Vec(hidden_dim + 1, Bool()))
    for (j <- 0 until hidden_dim) {
      val mul = Module(new uMULBipolarLite(8 + mitigation2))
      mul.io.iA := hidden_dim_wire(j)
      mul.io.iB := parsed("w2q")(i)(j).num.toInt.S
      mul.io.loadB := 1.B
      mul.io.sobolSeqP := hidden_rng_positives(j)
      mul.io.sobolSeqN := hidden_rng_negatives(j)
      in(j) := mul.io.oC
    }
    val bias = Module(new SobolRNGDim1(8 + 8 + mitigation1 + 8 + mitigation2))
    bias.io.en := 1.B
    in(hidden_dim) := parsed("b2q")(i).num.toInt.S > bias.io.sobolSeq.asSInt
    val nsadd = Module(new uNSADD(hidden_dim + 1, 10, true))
    // val nsadd = Module(new uNSADD(hidden_dim + 1, 20, true))
    nsadd.io.in := in
    io.out(i) := nsadd.io.out
  }
}

class UnaryWithInput(paramFile: String, temporal: Boolean) extends Module {
  val size=  16
  val io = IO(new Bundle {
    val in = Input(Vec(size * size, SInt(8.W)))
    val out = Output(Vec(10, Bool()))
  })

  val unary = Module(new Unary(paramFile, temporal))
  println(s"temporal: $temporal")
  if (temporal) {
    val rng = Module(new TemporalRNG(8, true))
    rng.io.en := 1.B
    for (i <- 0 until size * size) {
      unary.io.in(i) := io.in(i) > rng.io.out.asSInt
    }
  } else {
    val rng = Module(new SobolRNGDim1(8))
    rng.io.en := 1.B
    for (i <- 0 until size * size) {
      unary.io.in(i) := io.in(i) > rng.io.sobolSeq.asSInt
    }
  }
  // val json = scala.io.Source.fromFile(inputFile).mkString
  // val parsed = ujson.read(json)
  // val img = parsed("img").arr(0).arr.map(_.num.toInt)
  // val rng = Module(new SobolRNGDim1(8))
  // rng.io.en := 1.B
  // img.zipWithIndex.foreach { case (color, i) =>
  //   unary.io.in(i) := color.S > rng.io.sobolSeq.asSInt
  // }

  io.out := unary.io.out
}

// class UnaryUnified(paramFile: String) extends Module {
//   val size = 16
//   val io = IO(new Bundle {
//     val in = Input(UInt((size * size).W))
//     val out = Output(UInt(10.W))
//   })

//   val unary = Module(new Unary(paramFile))
//   unary.io.in := io.in.asBools
//   io.out := unary.io.out.asUInt
// }

class UnaryWithInputUnified(paramFile: String, temporal: Boolean) extends Module {
  val size = 16
  val io = IO(new Bundle {
    val in = Input(UInt((size * size * 8).W))
    val out = Output(UInt(10.W))
  })

  val unary = Module(new UnaryWithInput(paramFile, temporal))
  for (i <- 0 until size * size) {
    unary.io.in(i) := io.in(i * 8 + 7, i * 8).asSInt
  }
  io.out := unary.io.out.asUInt
}