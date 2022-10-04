package chiseltorch.nn.module

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import chiseltorch.tensor.Tensor
import firrtl.transforms.DontTouchAnnotation

import scala.collection.mutable.ArrayBuffer

class Sequential(layers: Seq[Seq[Int] => Module])(input_shape: Seq[Int]) extends chisel3.Module {
    val input_tensor = Tensor.Wire(Tensor.empty(input_shape, () => chiseltorch.dtypes.UInt(8.W)))

    // Chain the layers
    val output_tensor = layers.foldLeft(input_tensor) { (input, layer) =>
        // print created layer information
        val input_shape = input.shape
        println(s"Sequential: ${layer(input_shape).getClass.getSimpleName} ${input_shape} -> ${layer(input_shape).out_shape}")
        val layer_module = layer(input_shape)
//        layer_module.input := input.toVec
        val output_tensor = Tensor.Wire(Tensor.empty(layer_module.out_shape, () => chiseltorch.dtypes.UInt(8.W)))
        output_tensor := layer_module.output

        // Adding Param Inputs to Module
        layer_module.param_input match {
            case Some(pi) => {
                val pi_port = IO(Input(pi.cloneType)).suggestName(s"param_input")
                println(pi_port)
                println(pi.toNamed)
                pi := DontCare
            }
            case None => ()
        }

        output_tensor
    }

    val io = IO(new Bundle {
        val input = Input(input_tensor.asVecType)
        val out = Output(output_tensor.asVecType)
    })
    input_tensor := io.input
    io.out := output_tensor.toVec
}

object SequentialBuild extends App {
    val t0 = System.nanoTime()
    (new ChiselStage).emitVerilog(new Sequential(
        Seq(
            Linear(10, 10),
        )
    )
        (Seq(1, 10))
    )
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / math.pow(10.0, 9.0) + "s")
}
