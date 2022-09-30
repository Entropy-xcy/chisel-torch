package float

import org.scalatest.{FlatSpec, Matchers}
import tensor.{TensorFloatPeekPokeTest, TensorFloatTestModule, TensorIntAssignmentPeekPokeTest, TensorIntAssignmentTestModule}
import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import hardfloat._

class FloatSweepTestModule(expWidth: Int, sigWidth: Int) extends Module {
    val io = IO(new Bundle {
        val in = Input(UInt(32.W))
        val out = Output(UInt(32.W))
    })

    val fpu = Module(new AddRecFN(expWidth, sigWidth))
    fpu.io.a := io.in.asTypeOf(fpu.io.a)
    fpu.io.b := io.in.asTypeOf(fpu.io.b)
    fpu.io.detectTininess := 0.U
    fpu.io.subOp := false.B
    fpu.io.roundingMode := 0.U


    io.out := io.in
}

object TryFloat {
    def apply(expWidth: Int, sigWidth: Int): Unit = {
        (new ChiselStage).emitVerilog(new FloatSweepTestModule(expWidth, sigWidth))
    }
}

class FloatSpec extends FlatSpec with Matchers {
    behavior of "Berkeley HardFloat"

    it should "Buid Without any problem" in {
        TryFloat(8, 24)
        for (expWidth <- 1 to 16; sigWidth <- 1 to 64) {
            try{
                TryFloat(expWidth, sigWidth)
                println(s"success, $expWidth, $sigWidth")
            } catch {
                case e: Exception => println(s"failed, $expWidth, $sigWidth")
            }
        }
    }
}
