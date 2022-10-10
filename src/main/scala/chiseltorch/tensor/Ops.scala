package chiseltorch.tensor

import Chisel.Mux
import chisel3.experimental.hierarchy._
import chisel3.{VecInit, fromIntToLiteral}
import chiseltorch.common.ProgressBar
import chiseltorch.dtypes.DType
import chiseltorch.hw.MaxPoolKernel

object Ops {
    def sum[T <: DType[T]](a: Tensor[T]): T = {
        val sum_data = a.data.reduce((x, y) => x + y)
        sum_data
    }

    def sum[T <: DType[T]](a: Seq[T]): T = {
        val sum_data = a.reduce((x, y) => x + y)
        sum_data
    }

    def relu[T <: DType[T]](a: Tensor[T]): Tensor[T] = {
        val pbar = new ProgressBar(a.data.length)
        val relu_data = a.data.map(x => {
            pbar.update(1)
            Mux(x > x.zero, x, x.zero)
        })
        pbar.finished()
        Tensor(a.shape, relu_data)
    }

    def max[T <: DType[T]](a: Tensor[T]): T = {
        val max_data = a.data.reduce((x, y) => chisel3.Mux(x > y, x, y))
        max_data
    }

    def max[T <: DType[T]](a: Seq[T]): T = {
        val max_data = a.reduce((x, y) => chisel3.Mux(x > y, x, y))
        max_data
    }

    // Matrix Multiplication
    def mm[T <: DType[T]](a: Tensor[T], b: Tensor[T]): Tensor[T] = {
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

    def matmul[T <: DType[T]](a: Tensor[T], b: Tensor[T]): Tensor[T] = {
        // same as mm
        mm(a, b)
    }

    // Convolution
    def conv2d[T <: DType[T]](input: Tensor[T], weight: Tensor[T], stride: Int): Tensor[T] = {
        require(input.shape.length == 4, "Input must be 4D")
        require(weight.shape.length == 4, "Weight must be 4D")
        require(input.shape(1) == weight.shape(1), "Input and weight must have same number of channels")

        val (n, c, h, w) = (input.shape(0), input.shape(1), input.shape(2), input.shape(3))
        val (k, _, kh, kw) = (weight.shape(0), weight.shape(1), weight.shape(2), weight.shape(3))

        val oh = (h - kh) / stride + 1
        val ow = (w - kw) / stride + 1

        val new_data = 0 until n map { i => // i for batch
            0 until k map { j => // j for channel
                0 until oh map { l => // l for height
                    0 until ow map { m => // m for width
                        val to_sum = 0 until c map { p =>
                            0 until kh map { q =>
                                0 until kw map { r =>
                                    input(i, p, l * stride + q, m * stride + r).data.head * weight(j, p, q, r).data.head
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

    // Max Pooling
    def max_pool2d[T <: DType[T]](input: Tensor[T], kernel_size: Tuple2[Int, Int], stride: Option[Int]): Tensor[T] = {
        require(input.shape.length == 4, "Input must be 4D")

        val (n, c, h, w) = (input.shape(0), input.shape(1), input.shape(2), input.shape(3))
        val (kh, kw) = kernel_size
        val s = stride.getOrElse(kh)

        val oh = (h - kh) / s + 1
        val ow = (w - kw) / s + 1

        val maxpool_kernel_ref = Definition(new MaxPoolKernel((kh, kw), () => input.data.head.zero))
        val new_data = 0 until n map { i => // i for batch
            0 until c map { j => // j for channel
                0 until oh map { l => // l for height
                    0 until ow map { m => // m for width
                        val to_max = 0 until kh map { q =>
                            0 until kw map { r =>
                                input(i, j, l * s + q, m * s + r).data.head
                            }
                        }
                        val maxpool_kernel_instance = Instance(maxpool_kernel_ref)
                        val max = to_max.flatten
                        maxpool_kernel_instance.io.in := VecInit(max)
                        maxpool_kernel_instance.io.out
                    }
                }
            }
        }

        val new_shape = Seq(n, c, oh, ow)
        val new_tensor = Tensor(new_shape, new_data.flatten.flatten.flatten)
        new_tensor
    }

    def batch_norm[T <: DType[T]](input: Tensor[T], mean: T, variance: T, epsilon: T): Tensor[T] = {
        val new_data = input.data.map(x => (x - mean) / (variance + epsilon))
        Tensor(input.shape, new_data)
    }
}
