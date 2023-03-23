package chiseltorch.nn.module

import chiseltorch.tensor.{Ops, Tensor}
import chiseltorch.tensor.{Ops, Tensor}
import chisel3._
import chisel3.util._

object SampleFunctions {
    def double(input: Int): Int = {
        // Define your input function here
        input * 2 // Example: multiply by 2
    }

    def exp(input: Int): Int = {
        // Define your input function here
        Math.exp(input).toInt // Example: exponential
    }


    def tanh(input: Int): Int = {
        // Define your input function here
        Math.tanh(input).toInt // Example: exponential
    }
}

class ActLUT(w: Int, func: Int => Int) extends Module {
    val io = IO(new Bundle {
        val input  = Input(UInt(w.W))
        val output = Output(UInt(w.W))
    })

    // wpow = 2^w
    val wpow = 1 << w
    val lut = VecInit(Seq.tabulate(wpow)(i => (func(i) % wpow).U(w.W)))

    io.output := lut(io.input)
}


class Act(func: Int => Int)(input_shape: Seq[Int]) extends chiseltorch.nn.module.Module {
    val input_tensor = Tensor.Wire(Tensor.empty(input_shape, () => chiseltorch.dtypes.UInt(8.W)))
    val output_tensor = Tensor.Wire(Tensor.empty(input_shape, () => chiseltorch.dtypes.UInt(8.W)))

    output_tensor := Ops.relu(input_tensor)

    val io = IO(new Bundle {
        val input = Input(input_tensor.asVecType)
        val out = Output(output_tensor.asVecType)
    })

    input_tensor := io.input
    io.out := output_tensor.toVec


    override def input: Data = io.input

    override def output: Data = io.out

    override def in_shape: Seq[Int] = input_shape

    override def out_shape: Seq[Int] = output_tensor.shape

    override def param_input: Seq[Data] = Seq.empty
}

object Act {
    def apply(func: Int => Int)(input_shape: Seq[Int]): Act = Module(new Act(func)(input_shape))
}


object ActBuild extends App {
    (new chisel3.stage.ChiselStage).emitVerilog(new ActLUT(8, SampleFunctions.double))
}
