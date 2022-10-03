package chiseltorch.nn.module

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import chiseltorch.tensor.Tensor

class Sequential(layers: Seq[Seq[Int] => Module])(input_shape: Seq[Int]) extends chisel3.Module {
    val input_tensor = Tensor.Wire(Tensor.empty(input_shape, () => chiseltorch.dtypes.UInt(8.W)))

    val modules = layers.zipWithIndex.map { case (layer, i) =>
        val module = layer(input_tensor.shape)
        val module_tensor = Tensor.Wire(Tensor.empty(module.out_shape, () => chiseltorch.dtypes.UInt(8.W)))
        module_tensor := module.output
        module.input := input_tensor.toVec
        input_tensor := module_tensor
        module
    }

    val io = IO(new Bundle {
        val input = Input(input_tensor.asVecType)
        val out = Output(input_tensor.asVecType)
    })
    io.out := io.input
}

object SequentialBuild extends App {
    val t0 = System.nanoTime()
    (new ChiselStage).emitVerilog(new Sequential(
        Seq(
            Conv2D(3, 1, 1, stride = 1),
            ReLU(),
            Linear(16, 16),
            ReLU(),
            Linear(16, 16),
            ReLU(),
        )
    )
    (Seq(1, 3, 32, 32))
    )
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / math.pow(10.0, 9.0) + "s")
}
