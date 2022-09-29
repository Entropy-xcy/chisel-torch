package tensor

import chisel3._

case class TypedTensor[T <: CTorchType[_ <: Data]](shape: Seq[Int], data: Seq[T]) {
    def apply(index: Int): TypedTensor[T] = {
        val new_shape = shape.drop(1)
        val new_data = data.slice(index * new_shape.product, (index + 1) * new_shape.product)
        TypedTensor(new_shape, new_data)
    }

    def toChiselType: Vec[T] = {
        val size = shape.product
        Vec(size, data.head)
    }

    def toVec: Vec[T] = {
        VecInit(data)
    }

    def fromVec(vec: Vec[T]): TypedTensor[T] = {
        TypedTensor(shape, vec)
    }

    def := (that: TypedTensor[T]): Unit = {
        for (i <- data.indices) {
            data(i) := that.data(i)
        }
    }

    def := (that: Vec[T]): Unit = {
        for (i <- data.indices) {
            data(i) := that(i)
        }
    }

    def := (that: T): Unit = {
        data.foreach(_ := that)
    }
}

object TypedTensor {
    private def createData[T <: CTorchType[_ <: Data]](shape: Seq[Int], dtype_constructor: ()
        => T): Seq[T] = {
        val total_size = shape.product
        Seq.fill(total_size)(dtype_constructor())
    }

    def empty[T <: CTorchType[_ <: Data]](shape: Seq[Int], dtype_constructor: () => T): TypedTensor[T] = {
        val data = createData(shape, dtype_constructor)
        new TypedTensor[T](shape, data)
    }
    def Wire[T <: CTorchType[_ <: Data]](tensor: TypedTensor[T]): TypedTensor[T] = {
        val data = tensor.data.map(d => chisel3.Wire(d))
        new TypedTensor(tensor.shape, data)
    }

    def Reg[T <: CTorchType[_ <: Data]](tensor: TypedTensor[T]): TypedTensor[T] = {
        val data = tensor.data.map(d => chisel3.Reg(d))
        new TypedTensor(tensor.shape, data)
    }

    def fromVec[T <: CTorchType[_ <: Data]](shape: Seq[Int], vec: Vec[T]): TypedTensor[T] = {
        val data = vec.toSeq
        new TypedTensor(shape, data)
    }
}
