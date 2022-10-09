package chiseltorch.hw

import chisel3._
import chisel3.experimental.hierarchy.{instantiable, public}
import chisel3.util._
import chiseltorch.dtypes.DType
import chiseltorch.tensor.Tensor


@instantiable
class VectorOpSingle[T <: DType[T], U <: DType[U]](shape: Seq[Int], op: T => U, in_dtype_constructor: () => T) extends chisel3.Module {
    val tensor_a = Tensor.Wire(Tensor.empty(shape, in_dtype_constructor))
    val output_tensor = tensor_a.map(op)

    @public val io = IO(new Bundle {
        val a = Input(Input(tensor_a.asVecType))
        val c = Output(Input(output_tensor.asVecType))
    })


    tensor_a := io.a
    io.c := output_tensor.toVec
}


@instantiable
class VectorOpDouble[T <: DType[T], U <: DType[U]](shape: Seq[Int], op: (T, T) => U, in_dtype_constructor: () => T) extends chisel3.Module {
    val tensor_a = Tensor.Wire(Tensor.empty(shape, in_dtype_constructor))
    val tensor_b = Tensor.Wire(Tensor.empty(shape, in_dtype_constructor))
    val output_data = tensor_a.data.zip(tensor_b.data).map { case (a, b) => op(a, b) }
    val output_tensor = Tensor(shape, output_data)

    @public val io = IO(new Bundle {
        val a = Input(Input(tensor_a.asVecType))
        val b = Input(Input(tensor_b.asVecType))
        val c = Output(Input(output_tensor.asVecType))
    })

    tensor_a := io.a
    tensor_b := io.b
    io.c := output_tensor.toVec
}

object VectorOpDouble extends App {
    // build with chisel stage
    chisel3.stage.ChiselStage.emitVerilog(new VectorOpDouble(Seq(1, 2, 3),
        (x: chiseltorch.dtypes.UInt, y: chiseltorch.dtypes.UInt) => x + y, () => chiseltorch.dtypes.UInt(8.W)))
}
