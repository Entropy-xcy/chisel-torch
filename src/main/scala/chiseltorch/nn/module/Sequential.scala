package chiseltorch.nn.module

import chisel3._
import chisel3.util._

class Sequential extends chisel3.Module {
    val io = IO(new Bundle {
        val input = Input(Vec(1, UInt(8.W)))
        val out = Output(Vec(1, UInt(8.W)))
    })
    io.out := io.input
}
