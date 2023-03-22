import chisel3._
import chisel3.util._

class AddPaddingV(size: Int, dataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(Vec(size, Vec(size, UInt(dataWidth.W))))
    val out = Output(Vec(2*size-1, Vec(size, UInt(dataWidth.W))))
  })

  for (j <- 0 until size) {
    for (i <- 0 until 2*size-1) {
      val rj = size - j - 1
      if (j <= i && i < j + size) {
        io.out(i)(rj) := io.in(size - i + j - 1)(rj)
      } else {
        io.out(i)(rj) := 0.U
      }
    }
  }
}