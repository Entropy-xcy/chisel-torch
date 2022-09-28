package tensor

import chisel3._

trait CTorchType[T <: Data] extends Bundle {
  def +(that: T): T
  def -(that: T): T
  def *(that: T): T
  def /(that: T): T
}

class CTorchFloat(val exp_width: Int, val sig_width: Int) extends CTorchType[CTorchFloat] {
  val getInstance: () => CTorchFloat = (() => new CTorchFloat(exp_width, sig_width))
  val sign: Bool = Bool()
  val exponent: UInt = UInt(exp_width.W)
  val significand: UInt = UInt(sig_width.W)

  def +(that: CTorchFloat): CTorchFloat = {
    new CTorchFloat(exp_width, sig_width)
  }

  def -(that: CTorchFloat): CTorchFloat = {
    new CTorchFloat(exp_width, sig_width)
  }

  def *(that: CTorchFloat): CTorchFloat = {
    new CTorchFloat(exp_width, sig_width)
  }

  def /(that: CTorchFloat): CTorchFloat = {
    new CTorchFloat(exp_width, sig_width)
  }
}

class CTorchUInt(val uint_width: Int) extends CTorchType[CTorchUInt] {
  val getInstance: () => CTorchUInt = () => new CTorchUInt(uint_width)
  val data: UInt = UInt(uint_width.W)

  def +(that: CTorchUInt): CTorchUInt = {
    val result = Wire(new CTorchUInt(uint_width))
    result.data := this.data + that.data
    result
  }

  def -(that: CTorchUInt): CTorchUInt = {
    val result = Wire(new CTorchUInt(uint_width))
    result.data := this.data - that.data
    result
  }

  def *(that: CTorchUInt): CTorchUInt = {
    val result = Wire(new CTorchUInt(uint_width))
    result.data := this.data * that.data
    result
  }

  def /(that: CTorchUInt): CTorchUInt = {
    val result = Wire(new CTorchUInt(uint_width))
    result.data := this.data / that.data
    result
  }
}


