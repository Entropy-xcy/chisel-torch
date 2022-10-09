package chiseltorch.nn.module

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import chiseltorch.tensor.Tensor
import firrtl.transforms.DontTouchAnnotation

import scala.:+
import scala.collection.mutable.ArrayBuffer
import chiseltorch.nn.module.Pipe

class Sequential(layers: Seq[Seq[Int] => chiseltorch.nn.module.Module])(input_shape: Seq[Int]) extends chisel3.Module {
    val input_tensor = Tensor.Wire(Tensor.empty(input_shape, () => chiseltorch.dtypes.UInt(8.W)))
    val io = IO(new Bundle {
        val input = Input(input_tensor.asVecType)
    })
    input_tensor := io.input

    // iterate through layers
    var last_output_tensor = input_tensor
    var index = 0
    val module_list = for (layer <- layers) yield {
        // Print Layer information
        val mod = layer(last_output_tensor.shape)
        println(s"${mod.getClass.getSimpleName}: ${last_output_tensor.shape.mkString("x")}")
        mod.input := last_output_tensor.toVec
        val output_tensor = Tensor.Wire(Tensor.empty(mod.out_shape, () => chiseltorch.dtypes.UInt(8.W)))
        output_tensor := mod.output
        last_output_tensor = output_tensor

        mod.param_input match {
            case Some(param_input) =>
                val param_in_port = IO(Input(param_input.cloneType)).suggestName(s"param_in_$index")
                index += 1
                param_input := param_in_port
            case None => ()
        }
        mod
    }

    val output = IO(Output(last_output_tensor.asVecType))
    output := last_output_tensor.toVec
}

object SequentialBuild extends App {
    val t0 = System.nanoTime()
    (new ChiselStage).emitVerilog(new Sequential(
        Seq(
            Pipe(),
                Conv2D(3, 64, (11, 11), 4),
                ReLU(),
                MaxPool2D((3, 3), 2),
            Pipe(),
                Conv2D(64, 192, (5, 5), 2),
                ReLU(),
                MaxPool2D((3, 3), 2),
            Pipe(),
                Conv2D(192, 384, (3, 3), 1),
                ReLU(),
            Pipe(),
                Conv2D(384, 256, (3, 3), 1),
                ReLU(),
            Pipe(),
                Conv2D(256, 256, (3, 3), 1),
                ReLU(),
                MaxPool2D((3, 3), 2),
            Pipe(),
        )
    )
        (Seq(1, 3, 32, 32))
    )
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / math.pow(10.0, 9.0) + "s")
}
