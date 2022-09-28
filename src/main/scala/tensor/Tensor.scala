package tensor

import chisel3._
import chisel3.stage.ChiselStage

class Tensor(val shape: Seq[Int], val data: Vec[CTorchType[_ <: Data]]) extends Bundle {
  def apply(indice: Int): Tensor = {
    val newShape = shape.drop(1)
    val newData = data.slice(indice * newShape.product, (indice + 1) * newShape.product)
    new Tensor(newShape, VecInit(newData))
  }
}

object Tensor {
  private def createData(shape: Seq[Int], dtype_constructor: () => CTorchType[_ <: Data]): Vec[CTorchType[_ <: Data]] = {
    val total_size = shape.product
    Vec(total_size, dtype_constructor())
  }

  def empty(shape: Seq[Int], dtype_constructor: () => CTorchType[_ <: Data]): Tensor = {
    val data = createData(shape, dtype_constructor)
    new Tensor(shape, data)
  }

  def zeros(shape: Seq[Int], dtype_constructor: () => CTorchType[_ <: Data]): Data = {
    val data = createData(shape, dtype_constructor)
    val wired_data = Wire(new Tensor(shape, data))
    wired_data.data.foreach(_ := 0.U.asTypeOf(wired_data.data.head))
    wired_data
  }
}
