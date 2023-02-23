package chiseltorch.hw

import chisel3._
import chisel3.experimental.hierarchy.{Definition, Instance, instantiable, public}
import chisel3.stage.ChiselStage
import chiseltorch.dtypes.DType
import chiseltorch.tensor.Tensor
import chiseltorch.common.ProgressBar


class ProcessingElementWS[T <: DType[T]](width: Int, output: () => T) extends chisel3.Module {

    @public 
    val io = IO(new Bundle {
        val input = Input(new T(width.W))
        val weight = Input(new T(width.W))
        val prev_accu = Input(new T(width.W))
        val setWeight = Input(chisel3.Bool())
        val reset = Input(chisel3.Bool())
        val output = Output(new T(width.W))
    })
    val accumulator = RegInit(0.T(W.W))
    val weight = RegInit(0.T(W.W))

    when (io.setWeight) {
        weight := io.weight
    }
    when (io.reset) {
        accumulator := 0.T
        weight := 0.T
    }

    accumulator := accumulator + prev_accu + io.input * weight
    io.output := accumulator
}


@instantiable
class SystolicArrayWeightStationary[T <: DType[T]](shape_a: Seq[Int], shape_b: Seq[Int], dtype_constructor: () => T) extends chisel3.Module {
    require(shape_a.length == 2, "shape_a must be 2-dimensional")
    require(shape_b.length == 2, "shape_b must be 2-dimensional")
    require(shape_a(1) == shape_b(0), "shape_a(1) must be equal to shape_b(0)")

    val shape_c = Seq(shape_a(0), shape_b(1))
    val tensor_a = Tensor.Wire(Tensor.empty(shape_a, dtype_constructor))
    val tensor_b = Tensor.Wire(Tensor.empty(shape_b, dtype_constructor))
    val tensor_c = Tensor.Wire(Tensor.empty(shape_c, dtype_constructor))

    val pbar = new ProgressBar(shape_a(0) * shape_b(1))
    val neurons = for (i <- 0 until shape_a(0); j <- 0 until shape_b(1)) yield {
        val neuron = Module(new Neuron(8, () => chiseltorch.dtypes.UInt(8.W)))
        neuron.io.input := tensor_a(i).toVec
        neuron.io.weights := tensor_b.indexDim(1, j).toVec
        tensor_c(i, j) := neuron.io.output
        pbar.update(1)
        neuron
    }
    tensor_c ???
    pbar.finished()

    @public val io = IO(new Bundle {
        val a = Input(tensor_a.asVecType)
        val b = Input(tensor_b.asVecType)
        val c = Output(tensor_c.asVecType)
    })

    tensor_a := io.a
    tensor_b := io.b
    io.c := tensor_c.toVec
}