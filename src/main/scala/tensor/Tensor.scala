package tensor

class Tensor[T <: DType[T]](val shape: Seq[Int], val data: Seq[T]) {
    def +(that: Tensor[T]): Tensor[T] = {
        val new_data = data.zip(that.data).map { case (d0, d1) => d0 + d1 }
        new Tensor(shape, new_data)
    }

    def toVec: chisel3.Vec[T] = {
        chisel3.VecInit(data)
    }

    def asVecType: chisel3.Vec[T] = {
        val size = shape.product
        chisel3.Vec(size, data.head)
    }
}

object Tensor {
    def apply[T <: DType[T]](shape: Seq[Int], data: Seq[T]): Tensor[T] = {
        new Tensor(shape, data)
    }

    def empty[T <: DType[T]](shape: Seq[Int], dtype_constructor: () => T): Tensor[T] = {
        val data = Seq.fill(shape.product)(dtype_constructor())
        new Tensor(shape, data)
    }

    def Wire[T <: DType[T]](tensor: Tensor[T]): Tensor[T] = {
        val data = tensor.data.map(d => chisel3.Wire(d))
        new Tensor(tensor.shape, data)
    }
}


