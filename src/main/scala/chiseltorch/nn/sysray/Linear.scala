package chiseltorch.nn.sysray

import chisel3._
import chisel3.util._
import chiseltorch.tensor.Tensor
import chisel3.stage.ChiselStage
import chiseltorch.hw.SystolicArray

class Linear(input_dim: Int, output_dim: Int)(input_shape: Seq[Int]) extends chiseltorch.nn.sysray.Module {
    require(input_shape.length == 2, s"Linear input shape must be 2D, got $input_shape")
    require(input_shape(1) == input_dim, "Linear input dim must be " + input_dim + " got " + input_shape(1))
  
    //Input ouput tensor
    val input_tensor = Tensor.Wire(Tensor.empty(Seq(1, input_dim), () => chiseltorch.dtypes.UInt(8.W)))
    val weight_tensor = Tensor.Wire(Tensor.empty(Seq(input_dim, output_dim), () => chiseltorch.dtypes.UInt(8.W)))
    val output_tensor = Tensor.Wire(Tensor.empty(Seq(1, output_dim), () => chiseltorch.dtypes.UInt(8.W)))
    
    //** instantiate systolic array
    val sysray = Module(new SystolicArrayWeightStationary(input_tensor.shape, weight_tensor.shape, () => chiseltorch.dtypes.UInt(8.W)))
    sysray.io.input := input_tensor.toVec
    sysray.io.weight := weight_tensor.toVec
    output := sysray.io.output

    //IO interface
    val io = IO(new Bundle {
        val in = Input(Vec(input_dim, SInt(8.W)))
        val out = Output(Vec(output_dim, SInt(8.W)))
    })

    //Connect input and output
    override def input: Data = io.input
    override def output: Data = io.out
    override def in_shape: Seq[Int] = input_shape
    override def out_shape: Seq[Int] = output_tensor.shape
    override def param_input: Seq[Data] = Seq(io.weight)
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


