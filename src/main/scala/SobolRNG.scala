package unary

import chisel3._
import chisel3.util._

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

  var mux = (inWidth - 1).U(log2Up(inWidth).W)
  for (i <- (inWidth - 2) to 0 by -1) {
    mux = Mux(io.in(i), mux, i.U)
  }
  io.lszIdx := mux
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
  // var ms = List(1, 3, 7)
  // for (i <- 0 until inWidth - 3) {
  //   ms = ms.appended((4 * ms(i + 1)) ^ (8 * ms(i)) ^ ms(i))
  // }
  val seq = ((inWidth - 1) to 0 by -1).map(i => Math.pow(2, i).toInt.U)
  // val seq = ms.zipWithIndex.map({
  //   case (item, index) =>
  //     val shift = inWidth - 1 - index
  //     (item << shift).U
  // })
  // val seq = (0 until inWidth).map({ i =>
  //   val shift = inWidth - 1 - i
  //   ms(i) << shift
  // })
  inWidth match {
    // case 3 => sobolRNG.io.dirVec := VecInit(Seq(4.U, 6.U, 7.U))
    case default => sobolRNG.io.dirVec := VecInit(seq)
  }
  sobolRNG.io.vecIdx := lsz.io.lszIdx
  io.sobolSeq := sobolRNG.io.out

  io.cnt := cnt
  io.idx := lsz.io.lszIdx
}

class SobolRNGDim1_10 extends Module {
  val width = 10
  val io = IO(new Bundle {
    val en = Input(Bool())
    val threshold = Input(UInt((width-2).W))
    val value = Output(Bool())
  })
  val cnt = RegInit(0.U(2.W))
  when(io.en) {
    cnt := cnt + 1.U
  }
  val active = Wire(Bool())
  active := cnt === 3.U
  val rng = Module(new SobolRNGDim1(width-2))
  rng.io.en := active & io.en
  io.value := Mux(active, rng.io.sobolSeq > io.threshold, 0.U)
}
