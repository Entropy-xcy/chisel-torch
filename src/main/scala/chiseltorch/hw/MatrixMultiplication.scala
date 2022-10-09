package chiseltorch.hw

import chisel3._
import chisel3.experimental.hierarchy.{Definition, Instance, instantiable, public}
import chisel3.stage.ChiselStage
import chiseltorch.dtypes.DType
import chiseltorch.tensor.Tensor
import chiseltorch.common.ProgressBar

@instantiable
class DotProduct[T <: DType[T]](length: Int, in_dtype_constructor: () => T) extends chisel3.Module {
    val tensor_a = Tensor.Wire(Tensor.empty(Seq(length), in_dtype_constructor))
    val tensor_b = Tensor.Wire(Tensor.empty(Seq(length), in_dtype_constructor))
    val output_data = tensor_a.data.zip(tensor_b.data).map { case (a, b) => a * b }.reduce(_ + _)
    val output_tensor = Tensor(Seq(1), Seq(output_data))

    @public val io = IO(new Bundle {
        val a = Input(Input(tensor_a.asVecType))
        val b = Input(Input(tensor_b.asVecType))
        val c = Output(Input(output_tensor.asVecType))
    })

    tensor_a := io.a
    tensor_b := io.b
    io.c := output_tensor.toVec
}

@instantiable
class MatrixMultiplication[T <: DType[T]](shape_a: Seq[Int], shape_b: Seq[Int], dtype_constructor: () => T) extends chisel3.Module {
    assert(shape_a.length == 2)
    assert(shape_b.length == 2)
    assert(shape_a(1) == shape_b(0))
    val shape_c = Seq(shape_a(0), shape_b(1))
    val tensor_a = Tensor.Wire(Tensor.empty(shape_a, dtype_constructor))
    val tensor_b = Tensor.Wire(Tensor.empty(shape_b, dtype_constructor))
    val tensor_c = Tensor.Wire(Tensor.empty(shape_c, dtype_constructor))

    val ref_dot_prod = Definition(new DotProduct(shape_a(1), dtype_constructor))

    val pbar = new ProgressBar(shape_a(0) * shape_b(1))
    for (i <- 0 until shape_a(0)) {
        for (j <- 0 until shape_b(1)) {
            val dot_prod = Instance(ref_dot_prod)
            dot_prod.io.a := tensor_a(i).toVec
            dot_prod.io.b := tensor_b.indexDim(1, j).toVec
            tensor_c(i, j) := dot_prod.io.c
            pbar.update(1)
        }
    }
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

object MatrixMultiplication extends App {
    val t0 = System.nanoTime()
    (new ChiselStage).emitVerilog(new MatrixMultiplication(Seq(40, 128), Seq(128, 40), () => chiseltorch.dtypes.UInt(8.W)))
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / math.pow(10.0, 9.0) + "s")
}