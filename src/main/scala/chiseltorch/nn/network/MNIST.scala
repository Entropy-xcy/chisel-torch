package chiseltorch.nn.network

import chisel3.stage.ChiselStage
import chiseltorch.nn.module.{Conv2D, Flatten, Linear, MaxPool2D, Pipe, ReLU, Sequential}

object MNISTBuild extends App {
    val t0 = System.nanoTime()
    (new ChiselStage).emitVerilog(new Sequential(
        Seq(
            Conv2D(1, 5, (3, 3), 1),
            ReLU(),
            Flatten(),
            Linear(3380, 10)
        )
    )
    (Seq(1, 1, 28, 28)), Array("-td", "build", "-o", "MNIST"))
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / math.pow(10.0, 9.0) + "s")
}
