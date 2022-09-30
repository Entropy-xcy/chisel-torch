package float

import org.scalatest.{FlatSpec, Matchers}
import tensor.{TensorFloatPeekPokeTest, TensorFloatTestModule, TensorIntAssignmentPeekPokeTest, TensorIntAssignmentTestModule}

class FloatTest {

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
