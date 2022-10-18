package chiseltorch.nn.network

import chisel3.stage.ChiselStage
import chiseltorch.nn.module.{Conv2D, Flatten, Linear, MaxPool2D, Pipe, ReLU, Sequential}

object MNISTBuild extends App {
    val t0 = System.nanoTime()
    (new ChiselStage).emitVerilog(new Sequential(
        Seq(
            Conv2D(1, 32, (3, 3), 1),
            ReLU(),
            MaxPool2D((2, 2), 2),
            Flatten(),
            Linear(5408, 100),
            ReLU(),
            Linear(100, 10),
            ReLU(),
        )
    )
    (Seq(1, 1, 28, 28)), Array("-td", "build", "-o", "MNIST"))
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / math.pow(10.0, 9.0) + "s")
}
