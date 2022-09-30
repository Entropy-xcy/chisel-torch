package tensor

import chisel3._
import chisel3.iotesters._
import org.scalatest.{FlatSpec, Matchers}
import chiseltorch.dtypes.DType
import chiseltorch._
import chiseltorch.common.NDSeq

class TensorIntAssignmentTestModule extends Module {
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


class TensorIntAssignmentPeekPokeTest(c: TensorIntAssignmentTestModule) extends PeekPokeTester(c) {
    def peekVecTensor[T <: DType[T]](base_tensor: Tensor[T], base_vec: Vec[T]): NDSeq[BigInt] = {
        NDSeq(base_tensor.shape, peek(base_vec))
    }

    val tensor_out = peekVecTensor(c.tensor_temp, c.io.out)
    println(tensor_out.toString)
}


class TensorFloatTestModule extends Module {
    val tensor_temp = Tensor.empty(Seq(2, 2), () => new dtypes.Float(8, 9))
    val io = IO(new Bundle {
        val in1 = Input(tensor_temp.asVecType)
        val in2 = Input(tensor_temp.asVecType)
        val out = Output(tensor_temp.asVecType)
    })

    val t1 = Tensor.Wire(Tensor.empty(Seq(2, 2), () => new dtypes.Float(8, 9)))
    val t2 = Tensor.Wire(Tensor.empty(Seq(2, 2), () => new dtypes.Float(8, 9)))
    t1 := io.in1
    t2 := io.in2
    val t3 = t1 + t2

    io.out := t3.toVec
}


class TensorFloatPeekPokeTest(c: TensorFloatTestModule) extends PeekPokeTester(c) {
    println("Hello World")
}


class TensorSpec extends FlatSpec with Matchers {
    behavior of "Chisel Tensor"

    it should "Print Tensor with correct indexing" in {
        val args = Array("--backend-name", "verilator")
        chisel3.iotesters.Driver.execute(args = args, () => new TensorIntAssignmentTestModule) { c =>
            new TensorIntAssignmentPeekPokeTest(c)
        } should be(true)
    }


    it should "Correctly outputs TensorFloat" in {
        val args = Array("--backend-name", "verilator")
        chisel3.iotesters.Driver.execute(args = args, () => new TensorFloatTestModule) { c =>
            new TensorFloatPeekPokeTest(c)
        } should be(true)
    }
}
