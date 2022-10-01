package chiseltorch.dtypes

import chisel3.internal.firrtl.Width
import chisel3._
import chisel3.util._
import hardfloat._

trait DType[T] extends Bundle {
    def +(that: T): T
    def -(that: T): T
    def *(that: T): T
    def /(that: T): T
    def := (that: T): Unit
    def :=(that: chisel3.UInt): Unit
    def zero: T
    def LitVal(lit_val: scala.Float): T
}
