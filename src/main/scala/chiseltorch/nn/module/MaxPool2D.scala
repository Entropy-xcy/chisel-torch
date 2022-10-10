package chiseltorch.nn.module
import chisel3.experimental.hierarchy._
import chisel3.stage.ChiselStage
import chisel3.{Bundle, Data, Input, Module, Output, fromIntToWidth}
import chiseltorch.common.ProgressBar
import chiseltorch.tensor.Tensor

@instantiable
class MaxPool2DOne(kernel_size: Tuple2[Int, Int], stride: Int)(input_shape: Seq[Int]) extends chisel3.Module {
    require(input_shape.length == 4, "Maxpool2D input shape must be 4D")
    val num_features = input_shape(1)
    require(num_features == 1, "Maxpool2DPOne only supports 1 feature map")
    val w = input_shape(2)
    val h = input_shape(3)

    val input_tensor = Tensor.Wire(Tensor.empty(Seq(1, num_features, w, h), () => chiseltorch.dtypes.UInt(8.W)))
    val output_tensor = chiseltorch.tensor.Ops.max_pool2d(input_tensor, kernel_size, Some(stride))

    @public val io = IO(new Bundle {
        val input = Input(input_tensor.asVecType)
        val out = Output(output_tensor.asVecType)
    })

    input_tensor := io.input
    io.out := output_tensor.toVec
}


class MaxPool2D(kernel_size: Tuple2[Int, Int], stride: Int)(input_shape: Seq[Int]) extends chiseltorch.nn.module.Module {
    require(input_shape.length == 4, "Maxpool2D input shape must be 4D")
    val num_features = input_shape(1)
    val w = input_shape(2)
    val h = input_shape(3)
    require(kernel_size._1 == kernel_size._2, "Maxpool2D only supports square kernels")

    val ow = (w - kernel_size._1) / stride + 1
    val oh = (h - kernel_size._1) / stride + 1

    val input_tensor = Tensor.Wire(Tensor.empty(Seq(1, num_features, w, h), () => chiseltorch.dtypes.UInt(8.W)))
    val output_tensor = Tensor.Wire(Tensor.empty(Seq(1, num_features, ow, oh), () => chiseltorch.dtypes.UInt(8.W)))
//    val output_tensor = chiseltorch.tensor.Ops.max_pool2d(input_tensor, kernel_size, Some(stride))
    val maxpoolone_def = Definition(new MaxPool2DOne(kernel_size, stride)(Seq(1, 1, w, h)))

    val pbar = new ProgressBar(num_features)
    for (i <- 0 until num_features) {
        pbar.update(i)
        val maxpool_one = Instance(maxpoolone_def)
        maxpool_one.io.input := input_tensor(0)(i).toVec

        output_tensor(0)(i) := maxpool_one.io.out
    }
    pbar.finished()


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

object MaxPool2D {
    def apply(kernel_size: Tuple2[Int, Int], stride: Int)(input_shape: Seq[Int]): MaxPool2D =
        Module(new MaxPool2D(kernel_size, stride)(input_shape))
}

object MaxPool2DBuild extends App {
    (new ChiselStage).emitVerilog(new MaxPool2D((3, 3), 1)(Seq(1, 2, 30, 30)))
}