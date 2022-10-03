package chiseltorch.nn.module

import chisel3._
import chiseltorch.tensor.{Ops, Tensor}

class ReLU()(input_shape: Seq[Int]) extends chisel3.Module {
    val input_tensor = Tensor.Wire(Tensor.empty(input_shape, () => chiseltorch.dtypes.UInt(8.W)))
    val output_tensor = Tensor.Wire(Tensor.empty(input_shape, () => chiseltorch.dtypes.UInt(8.W)))

    output_tensor := Ops.relu(input_tensor)

    val io = IO(new Bundle {
        val input = Input(input_tensor.asVecType)
        val out = Output(output_tensor.asVecType)
    })

    input_tensor := io.input
    io.out := output_tensor.toVec
}
