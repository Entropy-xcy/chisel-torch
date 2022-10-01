package chiseltorch.tensor

import chisel3._
import chiseltest._
import chiseltorch._
import chiseltorch.dtypes.DType
import org.scalatest.flatspec.AnyFlatSpec

class Conv2DTestModule extends Module {
    val tensor_temp = Tensor.empty(Seq(2, 2), () => new dtypes.UInt(8.W))
    val io = IO(new Bundle {
        val I = Input(tensor_temp.asVecType)
        val w = Input(tensor_temp.asVecType)
        val out = Output(tensor_temp.asVecType)
    })


    val out_tensor = Tensor.zeros(Seq(2, 2), () => new dtypes.UInt(8.W))

    io.out := out_tensor.toVec
}



class Conv2DTest extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "Conv2DTestModule"
    // test class body here
    it should "Compute the Correct Conv2D result" in {
        test(new Conv2DTestModule) { c =>
            println("Not implemented Yet!")
        }
    }
}
