package chiseltorch.nn.module
import chisel3.{Bundle, Data, Input, Output, fromIntToWidth, Module}
import chiseltorch.tensor.Tensor

class MaxPool2d(kernel_size: Tuple2[Int, Int], stride: Int)(input_shape: Seq[Int]) extends Module {
    // FIXME
    require(input_shape.length == 4, "Maxpool2D input shape must be 4D")
    val num_features = input_shape(1)
    val w = input_shape(2)
    val h = input_shape(3)

    val input_tensor = Tensor.Wire(Tensor.empty(Seq(1, num_features, w, h), () => chiseltorch.dtypes.UInt(8.W)))
    val output_tensor = chiseltorch.tensor.Ops.max_pool2d(input_tensor, kernel_size, Some(stride))

    val io = IO(new Bundle {
        val input = Input(input_tensor.asVecType)
        val out = Output(output_tensor.asVecType)
    })

    input_tensor := io.input
    io.out := output_tensor.toVec

    override def input: Data = io.input

    override def output: Data = io.out

    override def in_shape: Seq[Int] = input_shape

    override def out_shape: Seq[Int] = output_tensor.shape

    override def param_input: Option[Data] = None
}

object MaxPool2d {
    def apply(kernel_size: Tuple2[Int, Int], stride: Int)(input_shape: Seq[Int]): MaxPool2d =
        Module(new MaxPool2d(kernel_size, stride)(input_shape))
}