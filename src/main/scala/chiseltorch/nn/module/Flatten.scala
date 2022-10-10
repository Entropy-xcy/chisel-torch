package chiseltorch.nn.module

import chiseltorch.tensor.Tensor

import scala.collection.mutable.Seq
import chisel3._
import chisel3.util._

import scala.collection.{immutable, mutable}

class Flatten()(input_shape: scala.collection.immutable.Seq[Int]) extends chiseltorch.nn.module.Module {
    val input_tensor = Tensor.Wire(Tensor.empty(input_shape, () => chiseltorch.dtypes.UInt(8.W)))
    val output_tensor = Tensor.Wire(Tensor.empty(scala.collection.immutable.Seq(1, input_shape.product), () => chiseltorch.dtypes.UInt(8.W)))

    output_tensor.data.zip(input_tensor.data).foreach { case (d0, d1) => d0 := d1 }

    val io = IO(new Bundle {
        val input = Input(input_tensor.asVecType)
        val out = Output(output_tensor.asVecType)
    })

    input_tensor := io.input
    io.out := output_tensor.toVec

    override def input: Data = io.input

    override def output: Data = io.out

    override def in_shape: immutable.Seq[Int] = input_shape

    override def out_shape: immutable.Seq[Int] = output_tensor.shape

    override def param_input: immutable.Seq[Data] = immutable.Seq.empty
}

object Flatten {
    def apply()(input_shape: immutable.Seq[Int]): Flatten = Module(new Flatten()(input_shape))
}
