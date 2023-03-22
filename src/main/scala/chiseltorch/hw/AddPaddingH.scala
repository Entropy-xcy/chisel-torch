import chisel3._
import chisel3.util._

class AddPaddingH(size: Int, dataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(Vec(size, Vec(size, UInt(dataWidth.W))))
    val out = Output(Vec(size, Vec(2*size-1, UInt(dataWidth.W))))
  })

  for (i <- 0 until size) {
    for (j <- 0 until 2*size-1) {
      val ri = size - i - 1
      if (i <= j && j < i + size) {
        io.out(ri)(j) := io.in(ri)(size - j + i - 1)
      } else {
        io.out(ri)(j) := 0.U
      }
    }
  }
}