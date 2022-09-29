package tensor

import chisel3._

class Tensor[T <: Data](val shape: Seq[Int], val data: Seq[CTorchType[T]]) {
    def apply(index: Int): Tensor[T] = {
        val new_shape = shape.drop(1)
        val new_data = data.slice(index * new_shape.product, (index + 1) * new_shape.product)
        new Tensor(new_shape, new_data)
    }

    def apply(indices: Int*): Tensor[T] = {
        indices.foldLeft(this)((tensor, index) => tensor(index))
    }

    def toChiselType: Vec[CTorchType[T]] = {
        val size = shape.product
        Vec(size, data.head)
    }

    def toVec: Vec[CTorchType[T]] = {
        VecInit(data)
    }
    def := (that: Tensor[T]): Unit = {
        for (i <- data.indices) {
            data(i) := that.data(i)
        }
    }

    def := (that: Vec[T]): Unit = {
        assert(data.length == that.length)
        for (i <- data.indices) {
            data(i) := that(i)
        }
    }

    def := (that: T): Unit = {
        data.foreach(_ := that)
    }

    def := (that: UInt): Unit = {
        data.foreach(_ := that)
    }

    def +(that: Tensor[T]): Tensor[T] = {
        val d0 = data.head
        val d1 = that.data.head
        new Tensor(shape, Seq.empty[CTorchType[T]])
    }

}

object Tensor {
    private def createData[T <: Data](shape: Seq[Int], dtype_constructor: ()
        => T): Seq[T] = {
        val total_size = shape.product
        Seq.fill(total_size)(dtype_constructor())
    }

    def empty[T <: Data](shape: Seq[Int], dtype_constructor: () => CTorchType[T]): Tensor[T] = {
        val data = createData(shape, dtype_constructor)
        new Tensor[T](shape, data)
    }
    def Wire[T <: Data](tensor: Tensor[T]): Tensor[T] = {
        val data = tensor.data.map(d => chisel3.Wire(d))
        new Tensor(tensor.shape, data)
    }

    def Reg[T <: Data](tensor: Tensor[T]): Tensor[T] = {
        val data = tensor.data.map(d => chisel3.Reg(d))
        new Tensor(tensor.shape, data)
    }
}
