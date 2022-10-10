package chiseltorch.nn.nasbench

import chisel3._
import chisel3.stage.ChiselStage
import chiseltorch.nn.module._

object ConvBnRelu {
    def apply(channels: Int, kernel_size: Int)(shape: Seq[Int]) = new Sequential(
            Seq(
                Conv2D(channels, channels, (kernel_size, kernel_size), 1),
                BatchNorm2d(channels, 0.5),
                ReLU()
            )
    )(shape)
}

object ConvBnRelu3x3 {
    def apply(channels: Int)(shape: Seq[Int]) = {
        val mod = new Sequential(
            Seq(
                Conv2D(channels, channels, (3, 3), 1, 1),
                BatchNorm2d(channels, 0.5),
                ReLU()
            )
        )(shape)
        println(mod.in_shape)
        println(mod.out_shape)
        mod
    }
}

object ConvBnRelu1x1 {
    def apply(channels: Int)(shape: Seq[Int]) = {
        val mod = new Sequential(
            Seq(
                Conv2D(channels, channels, (1, 1), 1, 0),
                BatchNorm2d(channels, 0.5),
                ReLU()
            )
        )(shape)
        println(mod.in_shape)
        println(mod.out_shape)
        mod
    }
}

object Maxpool3x3 {
    def apply()(shape: Seq[Int]) = {
        val mod = new Sequential(
            Seq(
                MaxPool2D((3, 3), 1, 1)
            )
        )(shape)
        println(mod.in_shape)
        println(mod.out_shape)
        mod
    }
}

object Projection {
    def apply(from_channels: Int, to_channels: Int)(shape: Seq[Int]) = {
        val mod = new Sequential(
            Seq(
                Conv2D(from_channels, to_channels, (1, 1), 1, 0),
            )
        )(shape)
        println(mod.in_shape)
        println(mod.out_shape)
        mod
    }
}

object NASBenchCellElementTestBuild extends App {
    (new ChiselStage).emitChirrtl(Projection(1, 2)(Seq(1, 1, 32, 32)))
}
