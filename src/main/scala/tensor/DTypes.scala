package tensor

import chisel3._


abstract class CTorchType extends Bundle {
  val getInstance: () => CTorchType

  def +(that: CTorchType): CTorchType
  def -(that: CTorchType): CTorchType
  def *(that: CTorchType): CTorchType
  def /(that: CTorchType): CTorchType

  def sqrt(): CTorchType
  def **(that: Int): CTorchType
}

class CTorchFloat(val exp_width: Int, val sig_width: Int) extends CTorchType {
  val getInstance = (() => new CTorchFloat(exp_width, sig_width))
  val sign        = Bool()
  val exponent    = UInt(exp_width.W)
  val significand = UInt(sig_width.W)
}

class CTorchUInt(val uint_width: Int) extends CTorchType {
  val getInstance = () => new CTorchUInt(uint_width)
  val data = UInt(uint_width.W)
}


