package tensor

import chisel3._
import chisel3.util._
import chisel3.iotesters._
import org.scalatest.{Matchers, FlatSpec}

class TensorTestModule extends Module {
    val tensor_temp = Tensor.empty(Seq(4, 4), () => new CTorchUInt(8))
    val io = IO(new Bundle {
        val in = Input(tensor_temp.toChiselType)
        val out = Output(tensor_temp.toChiselType)
    })
    val tensor_in = tensor_temp.fromVec(io.in)

    val tensor = Tensor.Wire(Tensor.empty(Seq(4, 4), () => new CTorchUInt(8)))
    tensor := io.in

    tensor(0, 0) := 0.U
    tensor(0)(1) := 1.U
    tensor(1)(0) := 2.U

    tensor(2) := 4.U

    io.out := tensor.toVec
}


class TensorPeekPokeTester(c: TensorTestModule) extends PeekPokeTester(c) {
    def peekVecTensor(base_tensor: Tensor, vec: Vec[_]) : NDSeq[BigInt] = {
        val raw_data = peek(vec)
        val shape = base_tensor.shape

        NDSeq(shape, raw_data)
    }

    val ndt = peekVecTensor(c.tensor, c.io.out)

    println(ndt.toString)
}

class TensorSpec extends FlatSpec with Matchers {
    behavior of "Chisel Tensor"

    it should "Print Tensor with correct indexing" in {
//        val args = Array("--backend-name", "verilator")
        val args: Array[String] = Array()
        chisel3.iotesters.Driver.execute(args=args, () => new TensorTestModule) { c =>
            new TensorPeekPokeTester(c)
        } should be(true)
    }
}