package chiseltorch.hw

import chisel3._
import chisel3.experimental.hierarchy.{Definition, Instance, instantiable, public}
import chisel3.stage.ChiselStage
import chiseltorch.dtypes.DType
import chiseltorch.tensor.Tensor
import chiseltorch.common.ProgressBar


























































































































































class ProcessingElementDN[T <: DType[T]](width: Int, output: () => T) extends chisel3.Module {

    @public 
    val io = IO(new Bundle {
        val input = Input(new T(width.W))
        val weight = Input(new T(width.W))
        val reset = Input(chisel3.Bool())
        val output = Output(new T(width.W))
    })
    val result = RegInit(0.T(W.W))
    when (io.reset) {
        result := 0.T
    }
    result := io.input * io.weight
    io.output := result
}


class NFU2_Cell[T <: DType[T]](width: Int, output: () => T) extends chisel3.Module {
    @public 
    val io = IO(new Bundle {
        val operand_A = Input(new T(width.W))
        val operand_B = Input(new T(width.W))
        val output = Output(new T(width.W))
    })
    io.output := io.operand_A + io.operand_B
}


class NFU_Build[T <: DType[T]](width: Int, tn: UInt) extends chisel3.Module {
    require(size % 2 == 0, "shape_a must be a multiple of 2")
    val io = IO(new Bundle {
        val NBin = Input(Vec(tn, new T(width.W)))
        val SB = Input(Vec(tn*tn, new T(width.W)))
        val reset = Input(Bool())
        val NBout = Output(Vec(tn, new T(width.W)))
    })
    val size = tn*tn

    for (i <- 0 until size / 2) {
        val index = i*2
        
        val pe1 = Module(new ProcessingElementDN(width, () => T))
        val pe2 = Module(new ProcessingElementDN(width, () => T))
        pe1.io.input := io.NBin(index % tn)
        pe1.io.weight := io.SB(index)
        pe1.io.reset := io.reset
        pe2.io.input := io.NBin((index+1) % tn)
        pe2.io.weight := io.SB(index+1)
        pe2.io.reset := io.reset

        val nfu2_cell = Module(new NFU2_Cell(width, () => T))
        nfu2_cell.io.operand_A := pe1.io.output
        nfu2_cell.io.operand_B := pe2.io.output

        io.NBout(i) := nfu2_cell.io.output
    }

    val pe_list = for (i <- 0 until size) yield {
    val pe = Module(new ProcessingElementDN(width, () => T))
        pe
    }
    val pe_list_ios = VecInit(pe_list.map(_.io))
}


@instantiable
class SystolicArrayDianNao[T <: DType[T]](shape_a: Seq[Int], shape_b: Seq[Int], dtype_constructor: () => T) extends chisel3.Module {
    require(shape_a.length == 2, "shape_a must be 2-dimensional")
    require(shape_b.length == 2, "shape_b must be 2-dimensional")
    require(shape_a(1) == shape_b(0), "shape_a(1) must be equal to shape_b(0)")

    val shape_c = Seq(shape_a(0), shape_b(1))
    val tensor_a = Tensor.Wire(Tensor.empty(shape_a, dtype_constructor))
    val tensor_b = Tensor.Wire(Tensor.empty(shape_b, dtype_constructor))
    val tensor_c = Tensor.Wire(Tensor.empty(shape_c, dtype_constructor))

    val pbar = new ProgressBar(shape_a(0) * shape_b(1))
    val neurons = for (i <- 0 until shape_a(0); j <- 0 until shape_b(1)) yield {
        val neuron = Module(new Neuron(8, () => chiseltorch.dtypes.UInt(8.W)))
        neuron.io.input := tensor_a(i).toVec
        neuron.io.weights := tensor_b.indexDim(1, j).toVec
        tensor_c(i, j) := neuron.io.output
        pbar.update(1)
        neuron
    }
    tensor_c ???
    pbar.finished()

    @public val io = IO(new Bundle {
        val a = Input(tensor_a.asVecType)
        val b = Input(tensor_b.asVecType)
        val c = Output(tensor_c.asVecType)
    })

    tensor_a := io.a
    tensor_b := io.b
    io.c := tensor_c.toVec
}