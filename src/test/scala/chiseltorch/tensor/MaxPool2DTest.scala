package chiseltorch.tensor

import chisel3._
import chiseltest._
import chiseltorch._
import chiseltorch.dtypes.DType
import org.scalatest.flatspec.AnyFlatSpec

class MaxPool2DTestModule extends Module {
    val I_tensor = Tensor.Wire(Tensor.empty(Seq(1, 1, 10, 10), () => new dtypes.UInt(8.W)))

    val O_tensor = Ops.max_pool2d(I_tensor, (3, 3), Some(1))

    val io = IO(new Bundle {
        val I = Input(I_tensor.asVecType)
        val O = Output(O_tensor.asVecType)
    })

    I_tensor := io.I
    io.O := O_tensor.toVec
}


class MaxPool2DTest extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "MaxPool2DTestModule"
    // test class body here
    it should "Compute the Correct MaxPool2D result" in {
        test(new MaxPool2DTestModule) { c =>
            for (i <- 0 until c.io.I.length) {
                c.io.I(i).data.poke(i.U)
            }

            c.clock.step(1)

            c.io.O.map(_.data.peek()).foreach(println)
        }
    }
}
