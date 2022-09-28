package tensor

import chisel3._
import chisel3.util._
import chisel3.iotesters._
import org.scalatest.{Matchers, FlatSpec}

class TensorTestModule extends Module {
  val io = IO(new Bundle {
    val out = Output(Tensor.empty(Seq(2, 3), () => new CTorchUInt(8)))
  })

  val tensor = Tensor.zeros(Seq(2, 3), () => new CTorchUInt(8))

  io.out := tensor
}


class TensorPeekPokeTester(c: TensorTestModule) extends PeekPokeTester(c)  {
  def peekPrintTensor(tensor: Tensor): Unit = {
    def tensorToString(data: Seq[BigInt], shape: Seq[Int]): String = {
      if (shape.length == 1) {
        data.map(_.toString).mkString("[", ", ", "]")
      } else {
        val new_shape = shape.tail
        val new_data = data.grouped(new_shape.product).toSeq
        new_data.map(tensorToString(_, new_shape)).mkString("[", ",\n ", "]")
      }
    }
    val data = peek(tensor.data)
    val shape = tensor.shape

    val str = tensorToString(data, shape)
    print(str)
    print("\n")
    println()
  }

  peekPrintTensor(c.io.out)
}

class TensorSpec extends FlatSpec with Matchers {
  behavior of "Chisel Tensor"

  it should "Print Tensor with correct indexing" in {
    chisel3.iotesters.Driver(() => new TensorTestModule) { c =>
      new TensorPeekPokeTester(c)
    } should be(true)
  }
}