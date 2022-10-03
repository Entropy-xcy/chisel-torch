package chiseltorch.nn.module

import chisel3._
import chisel3.util._
import chiseltorch.tensor.Tensor

class Pipe()(input_shape: Seq[Int]) extends Module {
    val input_tensor = Tensor.Wire(Tensor.empty(input_shape, () => chiseltorch.dtypes.UInt(8.W)))
    val output_tensor = Tensor.Reg(Tensor.empty(input_shape, () => chiseltorch.dtypes.UInt(8.W)))

    val io = IO(new Bundle {
        val input = Input(input_tensor.asVecType)
        val out = Output(output_tensor.asVecType)
    })
    input_tensor := io.input
    io.out := output_tensor.toVec

    override def input: Data = io.input

    override def output: Data = io.out

    override def in_shape: Seq[Int] = input_shape

    override def out_shape: Seq[Int] = input_shape

    override def param_input: Option[Data] = None
}
