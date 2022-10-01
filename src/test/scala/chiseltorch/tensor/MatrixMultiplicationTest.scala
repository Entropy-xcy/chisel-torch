package chiseltorch.tensor

import chisel3._
import chisel3.iotesters._
import chiseltest._
import chiseltorch._
import chiseltorch.common.NDSeq
import chiseltorch.dtypes.DType
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.{FlatSpec, Matchers}

class MatrixMultiplicationTestModule extends Module {
    val tensor_temp = Tensor.empty(Seq(2, 2), () => new dtypes.UInt(8.W))
    val io = IO(new Bundle {
        val a = Input(tensor_temp.asVecType)
        val b = Input(tensor_temp.asVecType)
        val out = Output(tensor_temp.asVecType)
    })

    val a = Tensor.Wire(Tensor.empty(Seq(2, 2), () => new dtypes.UInt(8.W)))
    val b = Tensor.Wire(Tensor.empty(Seq(2, 2), () => new dtypes.UInt(8.W)))
    a := io.a
    b := io.b

    val c = Ops.mm(a, b)

    io.out := c.toVec
}



class MatrixMultiplicationTest extends AnyFlatSpec with ChiselScalatestTester {
    def pokeTensor[T <: DType[T]](vec: Vec[T], poke_val: Seq[chisel3.UInt]) : Unit = {
        for (i <- 0 until vec.length) {
            val sample_val = vec.head.zero
            sample_val := poke_val(i)
            vec(i).poke(sample_val)
        }
    }

    behavior of "MatrixMultiplicationTestModule"
    // test class body here
    it should "Compute the Correct MM result" in {
        test(new MatrixMultiplicationTestModule) { c =>
            c.io.a(0).data.poke(1.U)
            c.io.a(1).data.poke(2.U)
            c.io.a(2).data.poke(3.U)
            c.io.a(3).data.poke(4.U)
            c.io.b(0).data.poke(5.U)
            c.io.b(1).data.poke(6.U)
            c.io.b(2).data.poke(7.U)
            c.io.b(3).data.poke(8.U)

            c.clock.step(1)

            c.io.out(0).data.expect(19.U)
            c.io.out(1).data.expect(22.U)
            c.io.out(2).data.expect(43.U)
            c.io.out(3).data.expect(50.U)
            println(c.io.out(0).data.peek())
            println(c.io.out(1).data.peek())
            println(c.io.out(2).data.peek())
            println(c.io.out(3).data.peek())
            println("Success!")
        }
    }
}
