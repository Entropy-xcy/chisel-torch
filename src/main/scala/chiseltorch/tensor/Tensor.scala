package chiseltorch.tensor

import chisel3.{Data, fromIntToLiteral}
import chiseltorch.dtypes.DType

class Tensor[T <: DType[T]](val shape: Seq[Int], val data: Seq[T]) {
    def apply(index: Int): Tensor[T] = {
        val new_shape = shape.drop(1)
        val new_data_groups = data.grouped(data.length / shape.head).toSeq
        val new_data = new_data_groups(index)
        new Tensor(new_shape, new_data)
    }

    def apply(index: Int*): Tensor[T] = {
        index.foldLeft(this)((t, i) => t(i))
    }

    def +(that: Tensor[T]): Tensor[T] = {
        val new_data = data.zip(that.data).map { case (d0, d1) => d0 + d1 }
        new Tensor(shape, new_data)
    }

    def toVec: chisel3.Vec[T] = {
        chisel3.VecInit(data)
    }

    def asVecType: chisel3.Vec[T] = {
        val size = shape.product
        chisel3.Vec(size, data.head.cloneType)
    }

    def :=(that: chisel3.Vec[T]): Unit = {
        data.zip(that).foreach { case (d0, d1) => d0 := d1 }
    }

    def :=(that: Tensor[T]): Unit = {
        require(shape == that.shape, s"Assignment shape mismatch, $shape != ${that.shape}")
        data.zip(that.data).foreach { case (d0, d1) => d0 := d1 }
    }

    // Broadcast
    def :=(that: T): Unit = {
        data.foreach { d => d := that }
    }

    // Broadcast
    def :=(that: chisel3.UInt): Unit = {
        data.foreach { d => d := that }
    }

    def :=(that: Data): Unit = {
        // that as Vec
        if (that.isInstanceOf[chisel3.Vec[_]]) {
            this := that.asInstanceOf[chisel3.Vec[T]]
        }
    }

    def reshape(new_shape: Seq[Int]): Tensor[T] = {
        assert(shape.product == new_shape.product)
        new Tensor(new_shape, data)
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

    def zeros[T <: DType[T]](shape: Seq[Int], dtype_constructor: () => T): Tensor[T] = {
        val data = Seq.fill(shape.product) {
            val dtype = chisel3.Wire(dtype_constructor())
            dtype := 0.U
            dtype
        }
        new Tensor(shape, data)
    }

    def Wire[T <: DType[T]](tensor: Tensor[T]): Tensor[T] = {
        val data = tensor.data.map(d => chisel3.Wire(d))
        new Tensor(tensor.shape, data)
    }

    def Reg[T <: DType[T]](tensor: Tensor[T]): Tensor[T] = {
        val data = tensor.data.map(d => chisel3.Reg(d))
        new Tensor(tensor.shape, data)
    }

    // Return a Literal Tensor
    def Lit[T <: DType[T]](shape: Seq[Int], data: Seq[scala.Float], dtype_constructor: () => T): Tensor[T] = {
        val data_lit = data.map(d => dtype_constructor().LitVal(d))
        new Tensor(shape, data_lit)
    }
}
