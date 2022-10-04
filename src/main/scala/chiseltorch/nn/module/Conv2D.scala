package chiseltorch.nn.module

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import chiseltorch.tensor.Tensor

private class Conv2DOne(in_channels: Int, in_size: (Int, Int), kernel_size: Int, stride: Int) extends chisel3.Module {
    // Print the build parameters
    val out_channels = 1
    val input_tensor = Tensor.Wire(Tensor.empty(Seq(1, in_channels, in_size._1, in_size._2), () => chiseltorch.dtypes.UInt(8.W)))
    val conv1_input_weight = Tensor.Wire(Tensor.empty(Seq(out_channels, in_channels, kernel_size, kernel_size), () => chiseltorch.dtypes.UInt(8.W)))
    val conv1_result = chiseltorch.tensor.Ops.conv2d(input_tensor, conv1_input_weight, stride = stride)
    val conv1_output = chiseltorch.tensor.Ops.relu(conv1_result)

    val io = IO(new Bundle {
        val input = Input(input_tensor.asVecType)
        val weight = Input(conv1_input_weight.asVecType)
        val out = Output(conv1_output.asVecType)
    })

    input_tensor := io.input
    conv1_input_weight := io.weight
    io.out := conv1_output.toVec
}

class Conv2D(in_channels: Int, out_channels: Int, kernel_size: Int, stride: Int)(in_size: Seq[Int]) extends Module {
    require(in_size.length == 4, "Conv2D input shape must be 4D")
    require(in_size(0) == 1, "Conv2D input batch size must be 1")
    require(in_size(1) == in_channels, "Conv2D input channel size must be " + in_channels)
    val w = in_size(2)
    val h = in_size(3)
    val input_tensor = Tensor.Wire(Tensor.empty(Seq(1, in_channels, w, h), () => chiseltorch.dtypes.UInt(8.W)))
    val conv1_input_weight = Tensor.Wire(Tensor.empty(Seq(out_channels, in_channels, kernel_size, kernel_size), () => chiseltorch.dtypes.UInt(8.W)))

    val ow = (w - kernel_size) / stride + 1
    val oh = (h - kernel_size) / stride + 1

    val conv1_output = Tensor.Wire(Tensor.empty(Seq(1, out_channels, ow, oh), () => chiseltorch.dtypes.UInt(8.W)))

    for (i <- 0 until out_channels) {
        // print progress
//        println(s"Conv2D: $i / $out_channels")
        val convone = Module(new Conv2DOne(in_channels, (w, h), kernel_size, stride))
        convone.io.input := input_tensor.toVec
        convone.io.weight := conv1_input_weight(i).toVec

        conv1_output(0)(i) := convone.io.out
    }

    val io = IO(new Bundle {
        val input = Input(input_tensor.asVecType)
        val weight = Input(conv1_input_weight.asVecType)
        val out = Output(conv1_output.asVecType)
    })

    input_tensor := io.input
    conv1_input_weight := io.weight
    io.out := conv1_output.toVec

    override def input: Data = io.input

    override def output: Data = io.out

    override def in_shape: Seq[Int] = in_size

    override def out_shape: Seq[Int] = conv1_output.shape

    override def param_input: Option[Data] = Some(io.weight)
}

object Conv2D {
    def apply(in_channels: Int, out_channels: Int, kernel_size: Int, stride: Int)(in_size: Seq[Int]): Conv2D = {
        Module(new Conv2D(in_channels, out_channels, kernel_size, stride)(in_size))
    }
}

// Chisel Stage Build This Module
object Conv2DBuild extends App {
    val t0 = System.nanoTime()
    (new ChiselStage).emitVerilog(new Conv2D(3, 8, 3, 1)(Seq(1, 3, 32, 32)))
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / math.pow(10.0, 9.0) + "s")
}
