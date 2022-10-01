package chiseltorch.dtypes

import chisel3.{Data, Wire, fromIntToLiteral}
import chisel3.internal.firrtl.Width

class UInt(val int_width: Width) extends DType[UInt] {
    val data = chisel3.UInt(int_width)

    def :=(that: chisel3.UInt): Unit = {
        this.data := that
    }

    def :=(that: UInt): Unit = {
        this.data := that.data
    }

    def +(that: UInt): UInt = {
        val max_width = int_width.max(that.int_width)
        val new_uint = Wire(new UInt(max_width))
        new_uint.data := data + that.data
        new_uint
    }

    def -(that: UInt): UInt = {
        val max_width = int_width.max(that.int_width)
        val new_uint = Wire(new UInt(max_width))
        new_uint.data := data - that.data
        new_uint
    }

    def *(that: UInt): UInt = {
        val max_width = int_width.max(that.int_width)
        val new_uint = Wire(new UInt(max_width))
        new_uint.data := data * that.data
        new_uint
    }

    def /(that: UInt): UInt = {
        val max_width = int_width.max(that.int_width)
        val new_uint = Wire(new UInt(max_width))
        new_uint.data := data / that.data
        new_uint
    }

    def zero: UInt = {
        val zeroi = Wire(new UInt(int_width))
        zeroi.data := 0.U

        zeroi
    }
}


object UInt {
    def apply(width: Width): UInt = {
        new UInt(width)
    }
}
