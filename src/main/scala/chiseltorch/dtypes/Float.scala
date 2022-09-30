package chiseltorch.dtypes

import chisel3._
import hardfloat.{AddRawFN, rawFloatFromFN}
import chiseltorch.dtypes.DType

class Float(val exp_width: Int, val sig_width: Int) extends DType[Float] {
    val sign = chisel3.Bool()
    val exp_width_no_sign = exp_width - 1
    val exp = chisel3.UInt(exp_width_no_sign.W)
    val sig = chisel3.UInt(sig_width.W)

    def asBits: Bits = {
        val bits = Wire(chisel3.UInt((exp_width + sig_width).W))
        bits := chisel3.util.Cat(sign, exp, sig)
        bits
    }

    def +(that: Float): Float = {
        val this_raw_float = rawFloatFromFN(exp_width, sig_width, this.asBits)
        val that_raw_float = rawFloatFromFN(exp_width, sig_width, that.asBits)
        val adder = chisel3.Module(new AddRawFN(exp_width, sig_width))
        adder.io.subOp := 0.U
        adder.io.a := this_raw_float
        adder.io.b := that_raw_float
        adder.io.roundingMode := 0.U
        val out_raw_float = adder.io.rawOut

        val new_float = Wire(new Float(exp_width, sig_width))
        new_float.sign := out_raw_float.sign
        val out_exp = Wire(SInt(exp_width_no_sign.W))
        out_exp := out_raw_float.sExp
        new_float.exp := out_exp.asUInt
        new_float.sig := out_raw_float.sig(out_raw_float.expWidth + 1, out_raw_float.expWidth + 2 - sig_width + 1)

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
