package chiseltorch.hw

import chisel3._
import chisel3.util._
import chisel3.experimental.hierarchy._
import chisel3.stage.ChiselStage
import chiseltorch.dtypes.DType
import chiseltorch.tensor.{Ops, Tensor}


@instantiable
class MaxPoolKernel[T <: DType[T]](kernel_size: (Int, Int), dtype_constructor: () => T) extends Module {
    val input_tensor = Tensor.empty(Seq(kernel_size._1 * kernel_size._2), dtype_constructor)
    input_tensor := 0.U
    val output = dtype_constructor()
    output := 0.U

    @public val io = IO(new Bundle {
        val in = Input(input_tensor.asVecType)
        val out = Output(output.cloneType)
    })

    io.out := Ops.max(io.in)

}

@instantiable
class MaxPool2DFast extends chisel3.Module {

}


object MaxPool2DFastBuild extends App {
    (new ChiselStage).emitVerilog(new MaxPoolKernel((3, 3), () => chiseltorch.dtypes.UInt(8.W)))
}

