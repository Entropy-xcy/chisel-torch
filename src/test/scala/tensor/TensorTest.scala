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

    val tensor = Tensor.Wire(Tensor.empty[CTorchUInt](Seq(2, 2), () => new CTorchUInt(8)))
    tensor(0, 0) := 0.U
    tensor(0, 1) := 1.U
    tensor(1) := 2.U

    io.out := tensor.toVec
}


class TensorPeekPokeTester(c: TensorTestModule) extends PeekPokeTester(c) {
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
