package chiseltorch.nn.nasbench

import chisel3._
import chisel3.util._
import chiseltorch.tensor.Tensor

class Cell(graph: CellGraph) extends chiseltorch.nn.module.Module {
    val input_tensor = Tensor.Wire(Tensor.empty(Seq(32, 32, 3), () => chiseltorch.dtypes.UInt(8.W)))
    val output_tensor = Tensor.Wire(Tensor.empty(Seq(32, 32, 3), () => chiseltorch.dtypes.UInt(8.W)))
    override def input: Data = ???

    override def output: Data = ???

    override def in_shape: Seq[Int] = ???

    override def out_shape: Seq[Int] = ???

    override def param_input: Seq[Data] = ???
}
