import chisel3._
import chisel3.experimental.hierarchy.{Definition, Instance, instantiable, public}
import chiseltorch.dtypes.DType


class PEGenerator[T <: DType[T]](width: Int, modeSelect: String) extends chisel3.Module {
    @public 
    val io = IO(new Bundle {
        val in_x = Input(new T(width.W))
        val in_w = Input(new T(width.W))
        val start = Input(chisel3.Bool())
        val out_x = Output(new T(width.W))
        val out_w = Output(new T(width.W))
        val output = Output(new T(width.W))
    })
    val accumulator = RegInit(0.T)
    val inputLatch = RegInit(0.T)
    val weightLatch = RegInit(0.T)

    when (io.start) {
        inputLatch := io.in_x
        weightLatch := io.in_w
        accumulator := 0.T
    }
    
    modeSelect match
        case "000" => { // Fused Multiply Add
            io.output := io.in_x * io.in_w 
            io.out_x := 0.T
            io.out_w := 0.T
        } 
        case "001" => { // Output Stationary
            accumulator := accumulator + io.in_x * io.in_w 
            io.output := accumulator
            inputLatch := io.in_x
            io.out_x := inputLatch
            weightLatch := io.in_w
            io.out_w := weightLatch
        }
        case "010" => { // Weight Stationary
            val prev_data = io.in_w
            accumulator := prev_data + io.in_x * weightLatch
            io.output := accumulator
            inputLatch := io.in_x
            io.out_x := inputLatch
            io.out_w = 0.T
        }
        case "011" => { // NVDLA (NVIDIA Deep Learning Accelerator)
            accumulator := accumulator + io.in_x * weightLatch
            io.output := accumulator
            weightLatch := io.in_w
            io.out_w := 0.T
            io.out_x := 0.T
        }
        case "111" => { // Naive Vector Machine
            accumulator := accumulator + inputLatch * weightLatch
            io.output := accumulator 
            inputLatch := io.in_x
            weightLatch := io.in_w
            io.out_x := 0.T
            io.out_w := 0.T
        }
}


        // case "100" => { // Row Stationary - Eyeriss, T(W), ST(I), S(O)
        //     accumulator := prev_data + io.in_x * weightLatch
        // }

class PEAdder[T <: DType[T]](width: Int) extends chisel3.Module {
      @public 
    val io = IO(new Bundle {
        val in_A = Input(new T(width.W))
        val in_B = Input(new T(width.W))
        val output = Output(new T(width.W))
    })

    io.output := io.in_A + io.in_B
}    




@instantiable
class PEGenerator[T <: DType[T]](shape_a: Seq[Int], shape_b: Seq[Int], dtype_constructor: () => T) extends chisel3.Module {
    require(shape_a.length == 2, "shape_a must be 2-dimensional")
    require(shape_b.length == 2, "shape_b must be 2-dimensional")
    require(shape_a(1) == shape_b(0), "shape_a(1) must be equal to shape_b(0)")

    val shape_c = Seq(shape_a(0), shape_b(1))
    val tensor_a = Tensor.Wire(Tensor.empty(shape_a, dtype_constructor))
    val tensor_b = Tensor.Wire(Tensor.empty(shape_b, dtype_constructor))
    val tensor_c = Tensor.Wire(Tensor.empty(shape_c, dtype_constructor))

    val pbar = new ProgressBar(shape_a(0) * shape_b(1))
    val neurons = for (i <- 0 until shape_a(0); j <- 0 until shape_b(1)) yield {
        val neuron = Module(new Neuron(8, () => chiseltorch.dtypes.UInt(8.W)))
        neuron.io.input := tensor_a(i).toVec
        neuron.io.weights := tensor_b.indexDim(1, j).toVec
        tensor_c(i, j) := neuron.io.output
        pbar.update(1)
        neuron
    }
    tensor_c ???
    pbar.finished()

    @public val io = IO(new Bundle {
        val a = Input(tensor_a.asVecType)
        val b = Input(tensor_b.asVecType)
        val c = Output(tensor_c.asVecType)
    })

    tensor_a := io.a
    tensor_b := io.b
    io.c := tensor_c.toVec
}