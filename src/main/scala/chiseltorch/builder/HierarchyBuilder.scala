package chiseltorch.builder

import chisel3._
import chisel3.experimental.BaseModule
import chisel3.stage.ChiselStage
import chisel3.util._
import javafx.scene.shape.DrawMode

import scala.collection.mutable.ArrayBuffer


class HierarchyModule extends Module {
    implicit val internal_mod_lst: ArrayBuffer[HierarchyModule] = ArrayBuffer.empty
    implicit val internal_mod_firrtl: ArrayBuffer[String] = ArrayBuffer.empty[String]
}

object HierarchyModule {
    def blackboxModule[T <: Bundle](mod: Module {val io: T}): BlackBox {val io: T} = {
        val blackbox = Module(new BlackBox {
            val io = IO(mod.io.cloneType)
        })
        // DontCare all elements in mod.io
        mod.io.elements.foreach { case (name, data) => data := DontCare }
        blackbox
    }


    def apply[T <: Bundle]
            (bc: ⇒ HierarchyModule {val io: T})(implicit internal_mod_lst: ArrayBuffer[HierarchyModule], internal_mod_firrtl: ArrayBuffer[String]): BlackBox {val io: T} = {
        val internal_firrtl = (new ChiselStage).emitFirrtl(bc)
        val mod = Module(bc)
        val blackbox: BlackBox {val io: T} = blackboxModule(mod)

        // append
        internal_mod_lst += mod
        internal_mod_firrtl += internal_firrtl
        blackbox
    }
}

object HierarchyBuilder {
    def emitFirrtl(gen: => Module): String = {
        implicit val internal_mod_lst: ArrayBuffer[HierarchyModule] = ArrayBuffer.empty
        implicit val internal_mod_firrtl: ArrayBuffer[String] = ArrayBuffer.empty[String]
        val self_firrtl = (new ChiselStage).emitVerilog(gen)
        self_firrtl
    }
}

class Adder extends HierarchyModule {
    val io = IO(new Bundle {
        val a = Input(UInt(8.W))
        val b = Input(UInt(8.W))
        val c = Output(UInt(8.W))
    })
    io.c := io.a + io.b
}

class VectorAdder(dimX: Int) extends HierarchyModule {
    val io = IO(new Bundle {
        val a = Input(Vec(dimX, UInt(8.W)))
        val b = Input(Vec(dimX, UInt(8.W)))
        val c = Output(Vec(dimX, UInt(8.W)))
    })

    for (i <- 0 until dimX) {
        val adder = HierarchyModule(new Adder).suggestName(s"adder_$i")
        adder.io.a := io.a(i)
        adder.io.b := io.b(i)
        io.c(i) := adder.io.c
    }
}

class MatrixAdder(dimX: Int, dimY: Int) extends HierarchyModule {
    val io = IO(new Bundle {
        val a = Input(Vec(2, UInt(8.W)))
        val b = Input(Vec(2, UInt(8.W)))
        val c = Output(Vec(2, UInt(8.W)))
    })


//    val vec_adder = HierarchyModule(new VectorAdder(dimX))
    val vec_adder_blk = HierarchyModule(new VectorAdder(dimX))

    vec_adder_blk.io.a := io.a
    vec_adder_blk.io.b := io.b
    io.c := vec_adder_blk.io.c
}

object HierarchyBuilderTest extends App {
    HierarchyBuilder.emitFirrtl(new MatrixAdder(2, 2))
}
