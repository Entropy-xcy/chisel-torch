package module

import chiseltest._

class PEModuleTest extends TestWithBackendSelect with ChiselScalatestTester {
  behavior of "PEGenerator"
  it should "do PEGenerator" in {
    test(new PEGeneratorWrapper(64, 4, "001")).withAnnotations(simAnnos) { pe =>      

    //   val test_inputs = Array(       val test_weights = Array(
    //     1, 2, 3, 4,                     9, 8, 7, 6,
    //     5, 6, 7, 8,                     5, 4, 3, 2,
    //     8, 7, 6, 5,                     1, 2, 3, 4,
    //     4, 3, 2, 1                      5, 6, 7, 8
    //   )                             )
      val transformed_inputs = Array(
        0, 0, 0, 4, 3, 2, 1, 
        0, 0, 8, 7, 6, 5, 0,
        0, 5, 6, 7, 8, 0, 0,
        1, 2, 3, 4, 0, 0, 0
      )
      val transformed_weights = Array(
        0, 0, 0, 0,
        0, 0, 7, 0,
        0, 6, 3, 0,
        5, 2, 3, 8,
        1, 4, 7, 4,
        5, 8, 0, 2,
        9, 0, 0, 6
      )
      val golden_outputs = Array(
        42,      46,	 50,	 54,
        122,	126,    130,	134,
        138,	134,    130,	126,
         58,	 54,    50,	    46
      )

      val timeout = 1000
      for (i <- 0 until 28) {
        pe.io.inputs.bits(i).poke(transformed_inputs(i))
        pe.io.weights.bits(i).poke(transformed_inputs(i))
        pe.io.start.poke(1)
      }

      do {
        pe.clock.step(1)
        pe.io.output.valid.poke(0)
        timeout = timeout - 1
      } while (pe.io.output.done.peekBoolean() == false && timeout > 0)

      assert(timeout != 0, "FAIL - PEGenerator Test has timed out.")

      for (i <- 0 until 16)
        c.io.output.bits(i).expect(golden_outputs(i))
    }
  }
}
