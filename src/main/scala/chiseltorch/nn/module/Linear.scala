package chiseltorch.nn.module

import chiseltorch.tensor.Tensor
import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._

class Linear(input_dim: Int, output_dim: Int)(input_shape: Seq[Int]) extends chisel3.Module {

    require(input_shape.length == 2, "Linear input shape must be 2D")
    require(input_shape(1) == input_dim, "Linear input dim must be " + input_dim)
    val input_tensor = Tensor.Wire(Tensor.empty(Seq(1, input_dim), () => chiseltorch.dtypes.UInt(8.W)))
    val weight_tensor = Tensor.Wire(Tensor.empty(Seq(input_dim, output_dim), () => chiseltorch.dtypes.UInt(8.W)))
    val output_tensor = Tensor.Wire(Tensor.empty(Seq(1, output_dim), () => chiseltorch.dtypes.UInt(8.W)))
    output_tensor := chiseltorch.tensor.Ops.mm(input_tensor, weight_tensor)

    val io = IO(new Bundle {
        val input = Input(input_tensor.asVecType)
        val weight = Input(weight_tensor.asVecType)
        val out = Output(output_tensor.asVecType)
    })

    input_tensor := io.input
    weight_tensor := io.weight
    io.out := output_tensor.toVec
}


object LinearBuild extends App {
    val t0 = System.nanoTime()
    (new ChiselStage).emitVerilog(new Linear(16, 16)(Seq(1, 16)))
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / math.pow(10.0, 9.0) + "s")
}
