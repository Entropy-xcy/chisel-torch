package chiseltorch.nn.module

import chiseltorch.tensor.Tensor
import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._

class Linear(input_dim: Int, output_dim: Int)(input_shape: Seq[Int]) extends Module {
    require(input_shape.length == 2, s"Linear input shape must be 2D, got $input_shape")
    require(input_shape(1) == input_dim, "Linear input dim must be " + input_dim + " got " + input_shape(1))
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

    override def input: Data = io.input

    override def output: Data = io.out

    override def in_shape: Seq[Int] = input_shape

    override def out_shape: Seq[Int] = output_tensor.shape

    override def param_input: Option[Data] = Some(io.weight)
}

object Linear {
    def apply(input_dim: Int, output_dim: Int)(input_shape: Seq[Int]): Linear = {
        Module(new Linear(input_dim, output_dim)(input_shape))
    }
}


object LinearBuild extends App {
    val t0 = System.nanoTime()
    (new ChiselStage).emitVerilog(new Linear(16, 16)(Seq(1, 16)))
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / math.pow(10.0, 9.0) + "s")
}
