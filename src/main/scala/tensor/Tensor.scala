package tensor

import chisel3._
import chisel3.stage.ChiselStage

class Tensor(val shape: Seq[Int], val dtype_constructor: () => CTorchType[_]) extends Bundle {
  def createData(shape: Seq[Int], dtype_constructor: () => CTorchType[_]): Data = {
    val current_dim = shape.head
    if (shape.length == 1) {
      Vec(current_dim, dtype_constructor())
    } else {
      Vec(current_dim, createData(shape.drop(1), dtype_constructor))
    }
  }
  val data = createData(shape, dtype_constructor)


}

class TensorTest extends RawModule {
  val shape = Seq(2, 3, 4)
  val tensor = Wire(new Tensor(shape, () => new CTorchFloat(8, 23)))
}

object TensorTestMain extends App {
  (new ChiselStage).emitVerilog(new TensorTest)
}
