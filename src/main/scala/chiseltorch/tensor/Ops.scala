package chiseltorch.tensor

import Chisel.Mux
import chisel3.experimental.hierarchy._
import chisel3.{Module, VecInit, fromIntToLiteral}
import chiseltorch.common.ProgressBar
import chiseltorch.dtypes.DType
import chiseltorch.hw.MaxPoolKernel
import chiseltorch.nn.module.{ActLUT, SampleFunctions}

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

    def tanh[T <: DType[T]](a: Tensor[T]): Tensor[T] = {
        val tanh_data = a.data.map(x => {
            val act_lut = Module(new ActLUT(8, SampleFunctions.tanh))
            act_lut.io.input := x
            act_lut.io.output.asInstanceOf[T]
        })
        Tensor(a.shape, tanh_data)
    }


    def exp[T <: DType[T]](a: Tensor[T]): Tensor[T] = {
        val tanh_data = a.data.map(x => {
            val act_lut = Module(new ActLUT(8, SampleFunctions.exp))
            act_lut.io.input := x
            act_lut.io.output.asInstanceOf[T]
        })
        Tensor(a.shape, tanh_data)
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

    def zero_padding[T <: DType[T]](in_tensor: Tensor[T], padding: Int): Tensor[T] = {
        require(in_tensor.shape.length == 4, "Only 4D inputs for Conv2D are supported")
        val in_shape = in_tensor.shape
        val output_data = 0 until in_shape(0) map { i => // i for batch
            0 until in_shape(1) map { j => // j for channel
                0 until in_shape(2) + 2 * padding map { l => // l for height
                    0 until in_shape(3) + 2 * padding map { m => // m for width
                        if (l < padding || l >= in_shape(2) + padding || m < padding || m >= in_shape(3) + padding) {
                            in_tensor.data.head.zero
                        } else {
                            in_tensor(i, j, l - padding, m - padding).data.head
                        }
                    }
                }
            }
        }
        val out_data_flat = output_data.flatten.flatten.flatten
        val out_shape = Seq(in_shape(0), in_shape(1), in_shape(2) + 2 * padding, in_shape(3) + 2 * padding)

        Tensor(out_shape, out_data_flat)
    }

    def batch_norm[T <: DType[T]](input: Tensor[T], mean: T, variance: T, epsilon: T): Tensor[T] = {
        val new_data = input.data.map(x => (x - mean) / (variance + epsilon))
        Tensor(input.shape, new_data)
    }

//    def concatTwo[T <: DType[T]](input_a: Tensor[T], input_b: Tensor[T], dim: Int): Tensor[T] = {
//        require(dim == 1, "Only concat along the channel dim is supported")
//        require(input_a.shape.length == 4, "Only 4D inputs for Conv2D are supported")
//        require(input_b.shape.length == 4, "Only 4D inputs for Conv2D are supported")
//        require(input_a.shape(0) == input_b.shape(0), "Batch size must be the same")
//        require(input_a.shape(2) == input_b.shape(2), "Height must be the same")
//        require(input_a.shape(3) == input_b.shape(3), "Width must be the same")
//
//        val new_data = for (i <- 0 until input_a.shape(0)) yield {
//            for (j <- 0 until input_a.shape(1) + input_b.shape(1)) yield {
//                for (l <- 0 until input_a.shape(2)) yield {
//                    for (m <- 0 until input_a.shape(3)) yield {
//                        if (j < input_a.shape(1)) {
//                            input_a(i, j, l, m).data.head
//                        } else {
//                            input_b(i, j - input_a.shape(1), l, m).data.head
//                        }
//                    }
//                }
//            }
//        }
//        val new_data_flat = new_data.flatten.flatten.flatten
//        val new_shape = Seq(input_a.shape(0), input_a.shape(1) + input_b.shape(1), input_a.shape(2), input_a.shape(3))
//
//        Tensor(new_shape, new_data)
//    }

    def concat[T <: DType[T]](inputs: Seq[Tensor[T]], dim: Int): Tensor[T] = {
        require(dim == 1, "Only concat along the channel dim is supported")
        if (inputs.length == 1)
          return inputs.head
        require(inputs.length > 1, "At least two inputs are required")
        require(inputs.forall(_.shape.length == 4), "Only 4D inputs for Conv2D are supported")
        require(inputs.forall(_.shape(0) == inputs(0).shape(0)), "Batch size must be the same")
        require(inputs.forall(_.shape(2) == inputs(0).shape(2)), "Height must be the same")
        require(inputs.forall(_.shape(3) == inputs(0).shape(3)), "Width must be the same")

        val new_data = for (i <- 0 until inputs(0).shape(0)) yield {
            for (j <- 0 until inputs.map(_.shape(1)).sum) yield {
                for (l <- 0 until inputs(0).shape(2)) yield {
                    for (m <- 0 until inputs(0).shape(3)) yield {
                        var sum = 0
                        var k = 0
                        while (sum <= j) {
                            sum += inputs(k).shape(1)
                            k += 1
                        }
                        inputs(k - 1)(i, j - sum + inputs(k - 1).shape(1), l, m).data.head
                    }
                }
            }
        }
        val new_data_flat = new_data.flatten.flatten.flatten
        val new_shape = Seq(inputs(0).shape(0), inputs.map(_.shape(1)).sum, inputs(0).shape(2), inputs(0).shape(3))

        Tensor(new_shape, new_data_flat)
    }

}
