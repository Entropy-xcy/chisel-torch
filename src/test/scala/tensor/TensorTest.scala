package tensor

import chisel3._
import chisel3.iotesters._
import org.scalatest.{FlatSpec, Matchers}

class TensorTestModule extends Module {
    val tensor_temp = Tensor.empty[CTorchUInt](Seq(2, 2), () => new CTorchUInt(8))
    val io = IO(new Bundle {
        val in = Input(tensor_temp.toChiselType)
        val out = Output(tensor_temp.toChiselType)
    })

    val v1 = Wire(new CTorchUInt(8))
    v1 := 1.U
    val v2 = Wire(new CTorchUInt(8))
    v2 := 2.U
    val v3 = v1 + v2

    val tensor = Tensor.Wire(Tensor.empty[CTorchUInt](Seq(2, 2), () => new CTorchUInt(8)))
    tensor(0, 0) := 0.U
    tensor(0, 1) := 1.U
    tensor(1) := v3

    io.out := tensor.toVec
}


class TensorPeekPokeTester(c: TensorTestModule) extends PeekPokeTester(c) {
    def peekVecTensor[T <: Data](base_tensor: Tensor[CTorchType[T]], vec: Vec[CTorchType[T]]): NDSeq[BigInt] = {
        val raw_data = peek(vec)
        val shape = base_tensor.shape

        NDSeq(shape, raw_data)
    }

//
//    println(ndt.toString)
    println("Hello")
}

class TensorSpec extends FlatSpec with Matchers {
    behavior of "Chisel Tensor"

    it should "Print Tensor with correct indexing" in {
        val args = Array("--backend-name", "verilator")
        chisel3.iotesters.Driver.execute(args=args, () => new TensorTestModule) { c =>
            new TensorPeekPokeTester(c)
        } should be(true)
    }
}
