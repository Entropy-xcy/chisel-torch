import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest._


class Cov2MaxTest extends TestWithBackendSelect with ChiselScalatestTester {

    "Cov2Max" should "convert convolution to matrix multiplication" in {
    test(new Cov2Max(4, 2, 16, 1)) { c =>
    
        }
    }
}