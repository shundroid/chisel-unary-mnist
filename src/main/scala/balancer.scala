package unary

import chisel3._
import chisel3.util._

// under construction
class Balancer extends Module {
  val io = IO(new Bundle {
    val inA = Input(Bool())
    val inB = Input(Bool())
    val outB = Output(Bool())

    val state = Output(UInt(2.W))
    val grabbing = Output(Bool())
    val grabbed_bit = Output(Bool())
  })
  val state = RegInit(1.U(2.W))
  val current = Wire(Bool())
  val grabbing = RegInit(false.B)
  val grabbed_bit = RegInit(false.B)
  val flip = Wire(Bool())
  flip := false.B // default
  current := io.inA ^ io.inB

  io.state := state
  io.grabbing := grabbing
  io.grabbed_bit := grabbed_bit

  when (state === 1.U) {
    when (grabbing & (grabbed_bit =/= io.inB)) {
      flip := true.B
      grabbing := false.B
      when (current) {
        state := 0.U
      }.otherwise {
        state := 2.U
      }
    }.otherwise {
      when (current) {
        state := 2.U
      }.otherwise {
        state := 0.U
      }
    }
  }.elsewhen(state === 2.U) {
    when (current) {
      when (grabbing) {
        when (grabbed_bit === io.inB) {
          // keep state & grabbing. give up
        }.otherwise {
          flip := true.B
          state := 1.U
          grabbing := false.B
        }
      }.otherwise {
        flip := true.B
        state := 1.U
        grabbing := true.B
        grabbed_bit := io.inB
      }
    }.otherwise {
      state := 1.U
    }
  }.otherwise {
    when (!current) {
      when (grabbing) {
        when (grabbed_bit === io.inB) {
          // keep state & grabbing. give up
        }.otherwise {
          flip := true.B
          state := 1.U
          grabbing := false.B
        }
      }.otherwise {
        flip := true.B
        state := 1.U
        grabbing := true.B
        grabbed_bit := io.inB
      }
    }.otherwise {
      state := 1.U
    }
  }
  io.outB := flip ^ io.inB
}