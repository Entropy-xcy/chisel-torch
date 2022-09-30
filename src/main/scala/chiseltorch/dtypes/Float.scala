package chiseltorch.dtypes

import chisel3._
import chisel3.util._
import hardfloat.{AddRawFN, DivSqrtRawFN_small, MulRawFN, rawFloatFromFN}
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

    def -(that: Float): Float = {
        val this_raw_float = rawFloatFromFN(exp_width, sig_width, this.asBits)
        val that_raw_float = rawFloatFromFN(exp_width, sig_width, that.asBits)
        val adder = chisel3.Module(new AddRawFN(exp_width, sig_width))
        adder.io.subOp := 1.U
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

    def *(that: Float): Float = {
        val this_raw_float = rawFloatFromFN(exp_width, sig_width, this.asBits)
        val that_raw_float = rawFloatFromFN(exp_width, sig_width, that.asBits)
        val mul = chisel3.Module(new MulRawFN(exp_width, sig_width))
        mul.io.a := this_raw_float
        mul.io.b := that_raw_float
        val out_raw_float = mul.io.rawOut

        val new_float = Wire(new Float(exp_width, sig_width))
        new_float.sign := out_raw_float.sign
        val out_exp = Wire(SInt(exp_width_no_sign.W))
        out_exp := out_raw_float.sExp
        new_float.exp := out_exp.asUInt
        new_float.sig := out_raw_float.sig(out_raw_float.expWidth + 1, out_raw_float.expWidth + 2 - sig_width + 1)

        new_float
    }

    def /(that: Float): Float = {
        val this_raw_float = rawFloatFromFN(exp_width, sig_width, this.asBits)
        val that_raw_float = rawFloatFromFN(exp_width, sig_width, that.asBits)
        val div = chisel3.Module(new DivSqrtRawFN_small(exp_width, sig_width, 0))
        div.io.a := this_raw_float
        div.io.b := that_raw_float
        div.io.inValid := 1.U
        div.io.sqrtOp := false.B
        div.io.roundingMode := 0.U
        val out_raw_float = div.io.rawOut

        val new_float = Wire(new Float(exp_width, sig_width))
        new_float.sign := out_raw_float.sign
        val out_exp = Wire(SInt(exp_width_no_sign.W))
        out_exp := out_raw_float.sExp
        new_float.exp := out_exp.asUInt
        new_float.sig := out_raw_float.sig(out_raw_float.expWidth + 1, out_raw_float.expWidth + 2 - sig_width + 1)

        new_float
    }

    def zero: Float = {
        val zero_f = chisel3.Wire(new Float(exp_width, sig_width))
        zero_f.sig := 0.U
        zero_f.sign := false.B
        zero_f.exp := 0.U

        zero_f
    }

    def :=(that: Float): Unit = {
        this.sign := that.sign
        this.exp := that.exp
        this.sig := that.sig
    }

    def :=(that: chisel3.UInt): Unit = {
        val that_bits_canon = Wire(Bits((exp_width + sig_width).W))
        that_bits_canon := that
        val that_bits = that_bits_canon.asBools
        this.sign := that_bits.head
        this.exp := VecInit(that_bits.slice(1, 1 + exp_width_no_sign - 1)).asUInt
        this.sig := VecInit(that_bits.slice(exp_width_no_sign, that_bits.length - 1)).asUInt
    }
}
