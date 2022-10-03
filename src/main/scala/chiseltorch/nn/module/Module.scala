package chiseltorch.nn.module

import chisel3.Data

trait Module extends chisel3.Module {
    def input: Data
    def output: Data
    def in_shape: Seq[Int]
    def out_shape: Seq[Int]
    def param_input: Option[Data]
}
