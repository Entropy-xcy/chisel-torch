package chiseltorch.nn.module

import chisel3._
import chisel3.experimental.hierarchy.{Definition, Instance, instantiable, public}
import chisel3.stage.ChiselStage
import chisel3.util._
import chiseltorch.common.ProgressBar
import chiseltorch.tensor.{Ops, Tensor}


class Attention(input_dim: Int, hidden_dim: Int, seq_len: Int)(in_size: Seq[Int]) extends chiseltorch.nn.module.Module {
    require(in_size.length == 3, "Attention input shape must be 4D")
    require(in_size(0) == 1, "Attention input batch size must be 1")
    require(in_size(1) == seq_len, "Attention input seq len must be " + seq_len + ", got " + in_size(1))
    require(in_size(2) == input_dim, "Attention input_dim seq len must be " + input_dim + ", got " + in_size(2))

    val W1 = Tensor.Wire(Tensor.empty(Seq(input_dim, hidden_dim), () => chiseltorch.dtypes.UInt(8.W)))
    val W2 = Tensor.Wire(Tensor.empty(Seq(hidden_dim, 1), () => chiseltorch.dtypes.UInt(8.W)))
    val inputs = Tensor.Wire(Tensor.empty(Seq(1, seq_len, input_dim), () => chiseltorch.dtypes.UInt(8.W)))
    val outputs = Tensor.Wire(Tensor.empty(Seq(1, input_dim), () => chiseltorch.dtypes.UInt(8.W)))

    val scores = Ops.mm(inputs.reshape(Seq(seq_len, input_dim)), W1)
    val scores_tanh = Ops.tanh(scores)
    val attn_weights = Ops.mm(scores_tanh, W2)
    val attn_weights_x = Ops.exp(attn_weights)

    for (i <- 0 until input_dim) {
        outputs(0, i) := (inputs(0, i) * attn_weights_x.reshape(Seq(1, seq_len))).sum()
    }

    val io = IO(new Bundle {
        val input = Input(inputs.asVecType)
        val W1_weight = Input(W1.asVecType)
        val W2_weight = Input(W2.asVecType)
        val out = Output(outputs.asVecType)
    })

    inputs := io.input
    W1 := io.W1_weight
    W2 := io.W2_weight
    io.out := outputs.toVec


    override def input: Data = io.input

    override def output: Data = io.out

    override def in_shape: Seq[Int] = in_size

    override def out_shape: Seq[Int] = outputs.shape

    override def param_input: Seq[Data] = Seq(io.W1_weight, io.W2_weight)
}


// Chisel Stage Build This Module
object AttentionBuild extends App {
    val t0 = System.nanoTime()
    (new ChiselStage).emitVerilog(new Attention(3, 3, 4)(Seq(1, 4, 3)))
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / math.pow(10.0, 9.0) + "s")
}
