package chiseltorch.module

import chisel3._
import chisel3.stage.ChiselStage
import chiseltorch.tensor.Tensor

class MNIST extends chisel3.Module {
    val input_tensor = Tensor.Wire(Tensor.empty(Seq(1, 1, 12, 12), () => chiseltorch.dtypes.UInt(8.W)))
    val conv1_input_weight = Tensor.Wire(Tensor.empty(Seq(1, 1, 3, 3), () => chiseltorch.dtypes.UInt(8.W)))
    val conv1_result = chiseltorch.tensor.Ops.conv2d(input_tensor, conv1_input_weight, stride = 1)
    val conv1_output = chiseltorch.tensor.Ops.relu(conv1_result)
    val fc1_input = conv1_output.reshape(Seq(1, 1 * 10 * 10))
    val fc1_weight = Tensor.Wire(Tensor.empty(Seq(1 * 10 * 10, 10), () => chiseltorch.dtypes.UInt(8.W)))
    val fc1_result = chiseltorch.tensor.Ops.mm(fc1_input, fc1_weight)

    val io = IO(new Bundle {
        val input_tensor_in = Input(input_tensor.asVecType)
        val conv1_input_weight_in = Input(conv1_input_weight.asVecType)
        val fc1_weight_in = Input(fc1_weight.asVecType)
        val out = Output(fc1_result.asVecType)
    })

    input_tensor := io.input_tensor_in
    conv1_input_weight := io.conv1_input_weight_in
    fc1_weight := io.fc1_weight_in
    io.out := fc1_result.toVec

    println("MNIST Module Build Done")
}

object MNIST extends App {
    (new ChiselStage).emitVerilog(new MNIST)
}
