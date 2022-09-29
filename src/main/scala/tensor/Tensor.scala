package tensor

import chisel3._
import chisel3.stage.ChiselStage

case class Tensor(shape: Seq[Int], data: Seq[CTorchType[_ <: Data]]) {
    def apply(index: Int): Tensor = {
        val new_shape = shape.drop(1)
        val new_data = data.slice(index * new_shape.product, (index + 1) * new_shape.product)
        Tensor(new_shape, new_data)
    }

    def apply(indices: Int*): CTorchType[_ <: Data] = {
        val index = indices.foldLeft(0)((acc, i) => acc * shape(1) + i)
        data(index)
    }

    def toVec: Vec[CTorchType[_ <: Data]] = {
        VecInit(data)
    }

    def fromVec(vec: Vec[CTorchType[_ <: Data]]): Tensor = {
        Tensor(shape, vec)
    }

    def toChiselType: Vec[CTorchType[_ <: Data]]  = {
        val size = shape.product
        Vec(size, data.head)
    }

    def := (that: Tensor): Unit = {
        for (i <- data.indices) {
            data(i) := that.data(i)
        }
    }

    def := (that: Vec[CTorchType[_ <: Data]]): Unit = {
        for (i <- data.indices) {
            data(i) := that(i)
        }
    }

    def := (that: CTorchType[_ <: Data]): Unit = {
        data.foreach(_ := that)
    }

    def := (that: UInt): Unit = {
        data.foreach(_ := that)
    }

}

object Tensor {
    private def createData(shape: Seq[Int], dtype_constructor: () => CTorchType[_ <: Data]): Seq[CTorchType[_ <: Data]] = {
        val total_size = shape.product
        Seq.fill(total_size)(dtype_constructor())
    }

    def empty(shape: Seq[Int], dtype_constructor: () => CTorchType[_ <: Data]): Tensor = {
        val data = createData(shape, dtype_constructor)
        new Tensor(shape, data)
    }

    def Wire(tensor: Tensor): Tensor = {
        val data = tensor.data.map(d => chisel3.Wire(d))
        new Tensor(tensor.shape, data)
    }

    def Reg(tensor: Tensor): Tensor = {
        val data = tensor.data.map(d => chisel3.Reg(d))
        new Tensor(tensor.shape, data)
    }

    def fromVec(shape: Seq[Int], vec: Vec[CTorchType[_ <: Data]]): Tensor = {
        Tensor(shape, vec)
    }

    def zeros(shape: Seq[Int], dtype_constructor: () => CTorchType[_ <: Data]): Tensor = {
        val t = empty(shape, dtype_constructor)
        val tt = Wire(t)
        tt.data.foreach(_ := 0.U)
        tt
    }
}
