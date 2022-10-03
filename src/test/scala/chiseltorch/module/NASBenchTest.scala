package chiseltorch.module

import chisel3._
import chisel3.stage.ChiselStage
import chiseltest.ChiselScalatestTester
import chiseltorch.tensor.{Conv2DTestModule, Tensor}
import org.scalatest.flatspec.AnyFlatSpec

class NASBenchTestModule extends chisel3.Module {
    val input_tensor = Tensor.Wire(Tensor.empty(Seq(1, 3, 32, 32), () => chiseltorch.dtypes.UInt(8.W)))
    val conv1_input_weight = Tensor.Wire(Tensor.empty(Seq(128, 3, 3, 3), () => chiseltorch.dtypes.UInt(8.W)))
    val conv1_result = chiseltorch.tensor.Ops.conv2d(input_tensor, conv1_input_weight, stride = 1)
    val conv1_output = chiseltorch.tensor.Ops.relu(conv1_result)

    val io = IO(new Bundle {
        val input_tensor_in = Input(input_tensor.asVecType)
        val conv1_input_weight_in = Input(conv1_input_weight.asVecType)
        val out = Output(conv1_output.asVecType)
    })

    input_tensor := io.input_tensor_in
    conv1_input_weight := io.conv1_input_weight_in
    io.out := conv1_output.toVec

    println("NASBench Module Build Done")
}

object NASBenchTest extends App {
    val t0 = System.nanoTime()
    (new ChiselStage).emitFirrtl(new NASBenchTestModule)
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / math.pow(10.0, 9.0) + "s")
}

class NASBenchTest extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "NASBenchTest"

    it should "NASBench Module Build Done" in {
        println("Starting Building NASBench Module")
        val t0 = System.nanoTime()
        (new ChiselStage).emitFirrtl(new NASBenchTestModule)
        val t1 = System.nanoTime()
        println("Elapsed time: " + (t1 - t0) / math.pow(10.0, 9.0) + "s")
    }
}
