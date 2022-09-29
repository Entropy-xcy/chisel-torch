package dtypes

import Chisel.fromIntToWidth
import chisel3.internal.firrtl.Width
import chisel3.{Bundle, Wire}

trait DType[T] extends Bundle {
    def +(that: T): T
    def := (that: T): Unit
    def :=(that: chisel3.UInt): Unit
}

class UInt(val int_width: Width) extends DType[UInt] {
    val data = chisel3.UInt(int_width)

    def :=(that: chisel3.UInt): Unit = {
        this.data := that
    }

    def :=(that: UInt): Unit = {
        this.data := that.data
    }

    def +(that: UInt): UInt = {
        val new_data = data + that.data
        val new_uint = Wire(new UInt(new_data.getWidth.W))
        new_uint.data := new_data
        new_uint
    }
}

class Float(val exp_width: Width, val sig_width: Width) extends DType[Float] {
    val sign = chisel3.Bool()
    val exp_width_no_sign = exp_width.+(-1)
    val exp = chisel3.UInt(exp_width_no_sign)
    val sig = chisel3.UInt(sig_width)

    def +(that: Float): Float = {
        // Dummy Implementation
        val new_float = Wire(new Float(exp_width, sig_width))
        new_float.sign := sign
        new_float.exp := exp + that.exp
        new_float.sig := sig + that.sig
        new_float
    }

    def :=(that: Float): Unit = {
        this.sign := that.sign
        this.exp := that.exp
        this.sig := that.sig
    }
    def :=(that: chisel3.UInt): Unit = {
        val that_bundle = that.asTypeOf(new Float(exp_width, sig_width))
        this.sign := that_bundle.sign
        this.exp := that_bundle.exp
        this.sig := that_bundle.sig
    }
}

object UInt {
    def apply(width: Width): UInt = {
        new UInt(width)
    }
}

