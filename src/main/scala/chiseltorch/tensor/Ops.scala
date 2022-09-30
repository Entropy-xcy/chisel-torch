package chiseltorch.tensor

import chiseltorch.dtypes.DType

object Ops {
    // Matrix Multiplication
    def mm[T <: DType[T]](a: Tensor[T], b: Tensor[T]): Tensor[T] = {
        require(a.shape == b.shape, "Shapes must be equal")
        require(a.shape.length == 2, "Matrix Multiplication only operates on 2D tensors")
        val m = a.shape.head
        val k = a.shape(1)
        val p = b.shape.head
        require(k == p, "Inner dimensions must be equal")
        val n = b.shape(1)

        val result_tensor = Tensor.Wire(Tensor.empty(Seq(m, n), () => a.data.head.zero))
        for (i <- 0 until m) {
            for (j <- 0 until n) {
                for (l <- 0 until k) {
                    val mul_result = a(i, l).data.head * b(l, j).data.head
                    val add_result = result_tensor(i, j).data.head + mul_result
                    result_tensor(i, j) := add_result
                }
            }
        }

        result_tensor
    }
}
