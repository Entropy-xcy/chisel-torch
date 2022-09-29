package tensor

import chisel3._
import chisel3.iotesters._
import org.scalatest.{FlatSpec, Matchers}

class TTensorTestModule extends Module {
    val tensor_temp = Tensor.empty(Seq(2, 2), () => new Float(8.W, 8.W))
    val io = IO(new Bundle {
        val in = Input(tensor_temp.asVecType)
        val out = Output(tensor_temp.asVecType)
    })

    val t2 = Tensor.Wire(Tensor.empty(Seq(2, 2), () => new Float(8.W, 8.W)))
    val t1 = Tensor.Wire(Tensor.empty(Seq(2, 2), () => new Float(8.W, 8.W)))
    t2.data(0) := 0.U
    t2.data(1) := 1.U
    t2.data(2) := 2.U
    t2.data(3) := 3.U

    t1.data(0) := 0.U
    t1.data(1) := 1.U
    t1.data(2) := 2.U
    t1.data(3) := 3.U

    val t3 = t1 + t2

    io.out := t3.toVec
}


class TTensorPeekPokeTester(c: TTensorTestModule) extends PeekPokeTester(c) {
    println("Hello")
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
