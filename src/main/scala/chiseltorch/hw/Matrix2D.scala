import chisel3._

class Matrix2D(rows: Int, cols: Int, dataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val row = Input(UInt(log2Up(rows).W))
    val col = Input(UInt(log2Up(cols).W))
    val writeData = Input(UInt(dataWidth.W))
    val readData = Output(UInt(dataWidth.W))
  })

  val matrix = Seq.fill(rows)(Vec.fill(cols)(RegInit(0.U(dataWidth.W))))

  io.readData := matrix(io.row)(io.col)
  matrix(io.row)(io.col) := io.writeData
}