import chisel3._
import chisel3.util._

class uMUL(inWidth: Int) extends Module {
  val io = IO(new Bundle {
    val iA = Input(Bool())
    val iB = Input(UInt(inWidth.W))
    val loadB = Input(Bool())
    val oC = Output(Bool())
  })

  val iBBuf = RegInit(0.U(inWidth.W))
  val sobolSeq = Wire(UInt(inWidth.W))

  val rng = Module(new SobolRNGDim1(inWidth))
  rng.io.en := io.iA
  sobolSeq := rng.io.sobolSeq

  when (io.loadB) {
    iBBuf := io.iB
  }

  io.oC := io.iA & (iBBuf > sobolSeq)
}