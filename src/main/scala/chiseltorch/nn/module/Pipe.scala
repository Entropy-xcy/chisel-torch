package chiseltorch.nn.module

import chisel3._
import chisel3.util._
import chiseltorch.tensor.Tensor

class Pipe()(input_shape: Seq[Int]) extends chiseltorch.nn.module.Module {
    val input_tensor = Tensor.Wire(Tensor.empty(input_shape, () => chiseltorch.dtypes.UInt(8.W)))
    input_tensor := 0.U

    val io = IO(new Bundle {
        val input = Input(input_tensor.asVecType)
        val out = Output(input_tensor.asVecType)
    })
    io.out := RegNext(io.input)

    override def input: Data = io.input

    override def output: Data = io.out

    override def in_shape: Seq[Int] = input_shape

    override def out_shape: Seq[Int] = input_shape

    override def param_input: Seq[Data] = Seq.empty
}

object Pipe {
    def apply()(input_shape: Seq[Int]): Pipe = Module(new Pipe()(input_shape))
}
