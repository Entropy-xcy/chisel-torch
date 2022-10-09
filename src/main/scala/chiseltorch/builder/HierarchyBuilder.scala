package chiseltorch.builder

import chisel3._
import chisel3.experimental.BaseModule
import chisel3.experimental.hierarchy.{Definition, Instance, instantiable, public}
import chisel3.stage.ChiselStage
import chisel3.util._

import scala.collection.mutable.ArrayBuffer


class Adder extends Module {
    val io = IO(new Bundle {
        val a = Input(UInt(8.W))
        val b = Input(UInt(8.W))
        val c = Output(UInt(8.W))
    })
    io.c := io.a + io.b
}

class VectorAdder(dimX: Int) extends Module {
    val io = IO(new Bundle {
        val a = Input(Vec(dimX, UInt(8.W)))
        val b = Input(Vec(dimX, UInt(8.W)))
        val c = Output(Vec(dimX, UInt(8.W)))
    })

    for (i <- 0 until dimX) {
        val adder = Module(new Adder)
        adder.io.a := io.a(i)
        adder.io.b := io.b(i)
        io.c(i) := adder.io.c
    }
}

class MatrixAdder(dimX: Int, dimY: Int) extends Module {
    val io = IO(new Bundle {
        val a = Input(Vec(2, UInt(8.W)))
        val b = Input(Vec(2, UInt(8.W)))
        val c = Output(Vec(2, UInt(8.W)))
    })


    val vec_adder_blk = Module(new VectorAdder(dimX))

    vec_adder_blk.io.a := io.a
    vec_adder_blk.io.b := io.b
    io.c := vec_adder_blk.io.c
}

object HierarchyModuleTest extends App {
    (new ChiselStage).emitVerilog(new MatrixAdder(2, 2))
}
