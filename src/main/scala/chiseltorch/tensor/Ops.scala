package chiseltorch.tensor

import chiseltorch.dtypes.DType

object Ops {
    def sum[T <: DType[T]](a: Tensor[T]): T = {
        val sum_data = a.data.reduce((x, y) => x + y)
        sum_data
    }

    def sum[T <: DType[T]](a: Seq[T]): T = {
        val sum_data = a.reduce((x, y) => x + y)
        sum_data
    }

    // Matrix Multiplication
    def mm[T <: DType[T]](a: Tensor[T], b: Tensor[T]): Tensor[T] = {
        require(a.shape == b.shape, "Shapes must be equal")
        require(a.shape.length == 2, "Matrix Multiplication only operates on 2D tensors")
        val m = a.shape.head
        val k = a.shape(1)
        val p = b.shape.head
        require(k == p, "Inner dimensions must be equal")
        val n = b.shape(1)

        val mul_results = 0 until m map { i =>
            0 until n map { j =>
                val to_sum = 0 until k map { l =>
                    a(i, l).data.head * b(l, j).data.head
                }
                Ops.sum(to_sum)
            }
        }

        val new_data = mul_results.flatten
        val new_shape = Seq(m, n)
        Tensor(new_shape, new_data)
    }

    // Convolution
    def conv2d[T <: DType[T]](input: Tensor[T], weight: Tensor[T], stide: Int): Tensor[T] = {
        require(input.shape.length == 4, "Input must be 4D")
        require(weight.shape.length == 4, "Weight must be 4D")
        require(input.shape(1) == weight.shape(1), "Input and weight must have same number of channels")
        require(weight.shape(0) == 1, "Weight must have only one filter")

        val (n, c, h, w) = (input.shape(0), input.shape(1), input.shape(2), input.shape(3))
        val (k, _, kh, kw) = (weight.shape(0), weight.shape(1), weight.shape(2), weight.shape(3))

        val oh = (h - kh) / stide + 1
        val ow = (w - kw) / stide + 1

        val new_data = 0 until n map { i => // i for batch
            0 until k map { j => // j for channel
                0 until oh map { l => // l for height
                     0 until ow map { m => // m for width
                        val to_sum = 0 until c map { p =>
                            0 until kh map { q =>
                                0 until kw map { r =>
                                    input(i, p, l * stide + q, m * stide + r).data.head * weight(j, p, q, r).data.head
                                }
                            }
                        }
                        val sum = to_sum.flatten.flatten
                        Ops.sum(sum)
                    }
                }
            }
        }

        val new_shape = Seq(n, k, oh, ow)
        val new_tensor = Tensor(new_shape, new_data.flatten.flatten.flatten)
        new_tensor
    }
}
