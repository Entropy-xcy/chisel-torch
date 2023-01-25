package chiseltorch.nn.sysray

import chisel3.Data
import scala.collection.immutable

trait Module extends chisel3.Module {
    def input: Data
    def output: Data
    def in_shape: immutable.Seq[Int]
    def out_shape: immutable.Seq[Int]
    def param_input: Seq[Data]
}
