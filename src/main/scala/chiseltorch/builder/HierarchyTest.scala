import chisel3._
import chisel3.util._
import chisel3.experimental.hierarchy.{Definition, Instance, instantiable, public}

@instantiable
class AddOne(width: Int) extends Module {
    @public val in  = IO(Input(UInt(width.W)))
    @public val out = IO(Output(UInt(width.W)))
    out := in + 1.U
}

class AddTwo(width: Int) extends Module {
    val in  = IO(Input(UInt(width.W)))
    val out = IO(Output(UInt(width.W)))
    val addOneDef = Definition(new AddOne(width))
    val i0 = Instance(addOneDef)
    val i1 = Instance(addOneDef)
    i0.in := in
    i1.in := i0.out
    out   := i1.out
}