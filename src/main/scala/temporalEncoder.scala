package unary

import chisel3._
import chisel3.util._
import scala.math.pow

class TemporalRNG(width: Int, bipolar: Boolean) extends Module {
  val io = IO(new Bundle {
    val en = Input(Bool())
    val out = Output(UInt(width.W))
  })
  if (bipolar) {
    val counter = RegInit((-(1 << (width - 1))).S(width.W))
    when(io.en) {
      counter := counter + 1.S;
    }
    io.out := counter.asUInt
  } else {
    val counter = RegInit(0.U(width.W))
    when(io.en) {
      counter := counter + 1.U;
    }
    io.out := counter
  }
}

class TemporalEncoder(width: Int, bipolar: Boolean) extends Module {
  val io = IO(new Bundle {
    val value = Input(UInt(width.W))
    val en = Input(Bool())
    val out = Output(Bool())
  })
  val counter = RegInit(0.U(width.W))
  when (io.en) {
    counter := counter + 1.U;
  }
  val threshold = Wire(UInt(width.W))
  if (bipolar) {
    threshold := (io.value.asSInt + pow(2, width - 1).toInt.S).asUInt
  } else {
    threshold := io.value
  }
  io.out := counter < threshold
}

class RateEncoder(width: Int, bipolar: Boolean) extends Module {
  val io = IO(new Bundle {
    val value = Input(UInt(width.W))
    val en = Input(Bool())
    val out = Output(Bool())
  })
  val rng = Module(new SobolRNGDim1(width))
  rng.io.en := io.en
  if (bipolar) {
    io.out := io.value.asSInt > rng.io.sobolSeq.asSInt
  } else {
    io.out := io.value > rng.io.sobolSeq
  }
}

class UEncoder(width: Int, temporal: Boolean, bipolar: Boolean) extends Module {
  val io = IO(new Bundle {
    val value = Input(UInt(width.W))
    val en = Input(Bool())
    val out = Output(Bool())
  })
  if (temporal) {
    val enc = Module(new TemporalEncoder(width, bipolar))
    enc.io.value := io.value
    enc.io.en := io.en
    io.out := enc.io.out
  } else {
    val enc = Module(new RateEncoder(width, bipolar))
    enc.io.value := io.value
    enc.io.en := io.en
    io.out := enc.io.out
  }
}
