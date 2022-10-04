package chiseltorch.nn.module

import chisel3._
import chisel3.stage.ChiselStage
import chiseltorch.tensor.{Ops, Tensor}

class ReLU()(input_shape: Seq[Int]) extends Module {
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

    override def param_input: Option[Data] = None
}

object ReLU {
    def apply()(input_shape: Seq[Int]): ReLU = Module(new ReLU()(input_shape))
}

class RelUWrapModule extends chisel3.Module {
    val relu = ReLU()(Seq(4))
    val io = IO(new Bundle {
        val input = Input(relu.input.cloneType)
        val out = Output(relu.output.cloneType)
    })

    relu.input := io.input
    io.out := relu.output
}

object RelUBuild extends App {
    val t0 = System.nanoTime()
    (new ChiselStage).emitVerilog(new RelUWrapModule)
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / math.pow(10.0, 9.0) + "s")
}
