package chiseltorch.tensor

import chisel3._
import chiseltest._
import chiseltorch._
import chiseltorch.dtypes.DType
import org.scalatest.flatspec.AnyFlatSpec

class Conv2DTestModule(input_channels: Int, input_h: Int, input_w: Int,
                       output_channels: Int, filter_h: Int, filter_w: Int) extends Module {
    val I_tensor = Tensor.Wire(Tensor.empty(Seq(1, input_channels, input_h, input_w), () => new dtypes.UInt(32.W)))
    val W_tensor = Tensor.Wire(Tensor.empty(Seq(output_channels, input_channels, filter_h, filter_w), () => new dtypes.UInt(32.W)))

    val O_tensor = Ops.conv2d(I_tensor, W_tensor, 1)

    val io = IO(new Bundle {
        val I = Input(I_tensor.asVecType)
        val W = Input(W_tensor.asVecType)
        val O = Output(O_tensor.asVecType)
    })

    I_tensor := io.I
    W_tensor := io.W
    io.O := O_tensor.toVec
}



class Conv2DTest extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "Conv2DTestModule"
    // test class body here
    it should "Compute the Correct Conv2D result" in {
        test(new Conv2DTestModule(1, 10, 10, 1, 3, 3)) { c =>
            /*
            Input:
                tensor([[[[1., 1., 0., 1., 1., 1., 1., 1., 1., 1.],
                          [1., 1., 1., 1., 1., 1., 1., 1., 1., 1.],
                          [1., 1., 1., 1., 1., 1., 1., 1., 1., 1.],
                          [1., 1., 1., 1., 1., 1., 1., 1., 1., 1.],
                          [1., 1., 1., 1., 1., 1., 1., 1., 1., 1.],
                          [1., 1., 1., 1., 1., 1., 1., 1., 1., 1.],
                          [1., 1., 1., 1., 1., 1., 1., 1., 1., 1.],
                          [1., 1., 1., 1., 1., 1., 1., 1., 1., 1.],
                          [1., 1., 1., 1., 1., 1., 1., 1., 1., 1.],
                          [1., 1., 1., 1., 1., 1., 1., 1., 1., 1.]]]])
            Weight:
                tensor([[[[1., 1., 1.],
                          [1., 1., 1.],
                          [1., 1., 1.]]]])
            */
            c.io.I.foreach(_.data.poke(1.U))
            c.io.W.foreach(_.data.poke(1.U))
            c.io.I(2).data.poke(0.U)

            c.clock.step(1)

            /*
            Output:
                tensor([[[[8., 8., 8., 9., 9., 9., 9., 9.],
                          [9., 9., 9., 9., 9., 9., 9., 9.],
                          [9., 9., 9., 9., 9., 9., 9., 9.],
                          [9., 9., 9., 9., 9., 9., 9., 9.],
                          [9., 9., 9., 9., 9., 9., 9., 9.],
                          [9., 9., 9., 9., 9., 9., 9., 9.],
                          [9., 9., 9., 9., 9., 9., 9., 9.],
                          [9., 9., 9., 9., 9., 9., 9., 9.]]]])
             */
            c.io.O(0).data.expect(8.U)
            c.io.O(1).data.expect(8.U)
            c.io.O(2).data.expect(8.U)
            for (i <- 3 until  64) {
                c.io.O(i).data.expect(9.U)
            }
        }
    }

    it should "Compute the Multichannel Conv2D result" in {
        test(new Conv2DTestModule(2, 10, 10, 3, 3, 3)) { c =>
            for (i <- 0 until c.io.I.length) {
                c.io.I(i).data.poke(i.U)
            }
            for (i <- 0 until c.io.W.length) {
                c.io.W(i).data.poke(i.U)
            }

            println(c.I_tensor.shape)
            println(c.W_tensor.shape)
            println(c.O_tensor.shape)
            c.io.O.map(_.data.peek()).foreach(println(_))
        }
    }
}
