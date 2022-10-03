package chiseltorch.nn.module

import chiseltorch.tensor.Tensor
import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._

class BatchNorm2d(num_features: Int, mean: Double, variance: Double, epsilon: Double)(input_shape: Seq[Int]) extends Module {
    require(input_shape.length == 4, "BatchNorm2d input shape must be 4D")
    require(input_shape(0) == 1, "BatchNorm2d input batch size must be 1")
    require(input_shape(1) == num_features, "BatchNorm2d input channel size must be " + num_features)
    val w = input_shape(2)
    val h = input_shape(3)

    val input_tensor = Tensor.Wire(Tensor.empty(Seq(1, num_features, w, h), () => chiseltorch.dtypes.UInt(8.W)))
    val output_tensor = Tensor.Wire(Tensor.empty(Seq(1, num_features, w, h), () => chiseltorch.dtypes.UInt(8.W)))
    val param_tensor = Tensor.Wire(Tensor.empty(Seq(3), () => chiseltorch.dtypes.UInt(8.W)))

    output_tensor := chiseltorch.tensor.Ops.batch_norm(input_tensor, mean=param_tensor.data(0), variance=param_tensor.data(1), epsilon=param_tensor.data(2))

    val io = IO(new Bundle {
        val input = Input(input_tensor.asVecType)
        val param = Input(param_tensor.asVecType)
        val out = Output(input_tensor.asVecType)
    })

    input_tensor := io.input
    param_tensor := io.param
    io.out := output_tensor.toVec
}

object BatchNorm2dBuild extends App {
    val t0 = System.nanoTime()
    (new ChiselStage).emitVerilog(new BatchNorm2d(16, 1.0, 1.0, 0.5)(Seq(1, 16, 32, 32)))
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / math.pow(10.0, 9.0) + "s")
}
