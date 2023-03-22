import chisel3._
import chisel3.util._

class Cov2Max(sizeMatrix: Int, sizeKernel: Int, dataWidth: Int, stride: Int) extends Module {

    val io = IO(new Bundle {
        val in = Input(Vec(sizeMatrix, Vec(sizeMatrix, UInt(dataWidth.W))))
        val kernel = Input(Vec(sizeKernel, Vec(sizeKernel, UInt(dataWidth.W))))
        val out = Output(Vec(sizeMatrix - sizeKernel + 1, Vec(sizeMatrix - sizeKernel + 1, UInt(dataWidth.W))))
    })

  // Reshape the input image into a matrix
  val M = Wire(Vec((sizeMatrix- sizeKernel + 1) * (sizeMatrix- sizeKernel+ 1), Vec(sizeKernel * sizeKernel, UInt(dataWidth.W))))
  for (i <- 0 until sizeMatrix- sizeKernel + 1) {
    for (j <- 0 until sizeMatrix- sizeKernel+ 1) {
      val subregion = io.in.slice(i, i + sizeKernel).map(row => row.slice(j, j + sizeKernel)).flatten
      M(i * (sizeMatrix- sizeKernel+ 1) + j) := VecInit(subregion)
    }
  }

  // Reshape the convolution kernel into a matrix
  val K_m = Wire(Vec(sizeKernel * sizeKernel, Vec(1, UInt(dataWidth.W))))
  K_m := io.kernel.flatten.map(e => VecInit(Seq(e)))

  // Compute the convolution as a matrix multiplication
  val outVec = M.map(row => (row.asUInt * K_m.asUInt).reduce(_ + _))

  // Reshape the output column vector into a matrix
  for (i <- 0 until sizeMatrix- sizeKernel + 1) {
    for (j <- 0 until sizeMatrix- sizeKernel+ 1) {
      io.out(i)(j) := outVec(i * (sizeMatrix- sizeKernel+ 1) + j)
    }
  }
}
