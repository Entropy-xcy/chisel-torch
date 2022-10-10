package chiseltorch.nn.module

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import chiseltorch.tensor.Tensor
import firrtl.transforms.DontTouchAnnotation

import scala.:+
import scala.collection.mutable.ArrayBuffer
import chiseltorch.nn.module.Pipe

class Sequential(layers: Seq[Seq[Int] => chiseltorch.nn.module.Module])(input_shape: Seq[Int]) extends chiseltorch.nn.module.Module {
    val input_tensor = Tensor.Wire(Tensor.empty(input_shape, () => chiseltorch.dtypes.UInt(8.W)))

    // iterate through layers
    var last_output_tensor = input_tensor
    var index = 0
    val param_ports = ArrayBuffer.empty[chisel3.Data]
    val module_list = for (layer <- layers) yield {
        // Print Layer information
        val mod = layer(last_output_tensor.shape)
        println(s"${mod.getClass.getSimpleName}: ${last_output_tensor.shape.mkString("x")}")
        mod.input := last_output_tensor.toVec
        val output_tensor = Tensor.Wire(Tensor.empty(mod.out_shape, () => chiseltorch.dtypes.UInt(8.W)))
        output_tensor := mod.output
        last_output_tensor = output_tensor

        mod.param_input match {
            case Seq(param_input) =>
                val param_in_port = IO(Input(param_input.cloneType)).suggestName(s"param_in_$index")
                index += 1
                param_input := param_in_port
                param_ports += param_in_port
            case Seq() => ()
            case _ => throw new Exception("Only one parameter input is supported currently")
        }
        mod
    }

    val io = IO(new Bundle {
        val input = Input(input_tensor.asVecType)
        val output = Output(last_output_tensor.asVecType)
    })
    input_tensor := io.input
    io.output := last_output_tensor.toVec
    println("Finished Elaboration")

    override def input: Data = io.input

    override def output: Data = io.output

    override def in_shape: Seq[Int] = input_tensor.shape

    override def out_shape: Seq[Int] = last_output_tensor.shape

    override def param_input: Seq[Data] = param_ports.toSeq
}

object SequentialBuild extends App {
    val t0 = System.nanoTime()
    (new ChiselStage).emitChirrtl(new Sequential(
        Seq(
            Pipe(),
                Conv2D(3, 1, (3, 3), 1),
                ReLU(),
            Pipe(),
                MaxPool2D((3, 3), 1),
            Pipe(),
                Flatten(),
                Linear(784, 10),
                ReLU(),
            Pipe()
        )
    )
        (Seq(1, 3, 32, 32))
    )
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / math.pow(10.0, 9.0) + "s")
}
