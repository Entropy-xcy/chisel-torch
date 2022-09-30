package tensor

import chisel3._
import chisel3.iotesters._
import dtypes.{DType, NDSeq}
import org.scalatest.{FlatSpec, Matchers}

class TTensorTestModule extends Module {
    val tensor_temp = Tensor.empty(Seq(2, 2, 2), () => new dtypes.UInt(8.W))
    val io = IO(new Bundle {
        val in = Input(tensor_temp.asVecType)
        val out = Output(tensor_temp.asVecType)
    })

    val t2 = Tensor.Wire(Tensor.empty(Seq(2, 2, 2), () => new dtypes.UInt(8.W)))
    val t1 = Tensor.Wire(Tensor.empty(Seq(2, 2, 2), () => new dtypes.UInt(8.W)))
    t1 := 1.U
    t2 := 3.U

    t1(0)(0)(0) := 2.U

    t2(1, 1, 1) := 4.U

    val t3 = t1 + t2

    io.out := t3.toVec
}


class TTensorPeekPokeTester(c: TTensorTestModule) extends PeekPokeTester(c) {
    def peekVecTensor[T <: DType[T]](base_tensor: Tensor[T], base_vec: Vec[T]): NDSeq[BigInt] = {
        NDSeq(base_tensor.shape, peek(base_vec))
    }

    val tensor_out = peekVecTensor(c.tensor_temp, c.io.out)
    println(tensor_out.toString)
}

class TensorSpec extends FlatSpec with Matchers {
    behavior of "Chisel Tensor"

    it should "Print Tensor with correct indexing" in {
        val args = Array("--backend-name", "verilator")
        chisel3.iotesters.Driver.execute(args=args, () => new TTensorTestModule) { c =>
            new TTensorPeekPokeTester(c)
        } should be(true)
    }
}
