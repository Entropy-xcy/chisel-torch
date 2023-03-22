import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest._

class PaddingTest extends TestWithBackendSelect with ChiselScalatestTester {

  "AddPadding" should "add paddings horizontally" in {
    test(new AddPaddingH(4, 16)) { c =>
      val test_inputs = Seq(
        Seq(1, 2, 3, 4),
        Seq(5, 6, 7, 8),
        Seq(8, 7, 6, 5),
        Seq(4, 3, 2, 1)
      ).map(_.map(_.U(16.W)))

      val expectedOutput = Seq(
        Seq(0, 0, 0, 4, 3, 2, 1), 
        Seq(0, 0, 8, 7, 6, 5, 0),
        Seq(0, 5, 6, 7, 8, 0, 0),
        Seq(1, 2, 3, 4, 0, 0, 0)
      ).map(_.map(_.U(16.W)))

      c.io.in.zip(input).foreach { case (inRow, inputRow) =>
        inRow.zip(inputRow).foreach { case (inElem, inputElem) =>
          inElem.poke(inputElem)
        }
      }

      c.clock.step(1)

      c.io.out.zip(expectedOutput).foreach { case (outRow, expectedRow) =>
        outRow.zip(expectedRow).foreach { case (outElem, expectedElem) =>
          outElem.expect(expectedElem)
        }
      }
    }
  }

    "AddPadding" should "add paddings vertically" in {
    test(new AddPaddingV(4, 16)) { c =>
      val test_inputs = Seq(
        Seq(9, 8, 7, 6),
        Seq(5, 4, 3, 2),
        Seq(1, 2, 3, 4),
        Seq(5, 6, 7, 8)
      ).map(_.map(_.U(16.W)))

      val expectedOutput = Seq(
        Seq(0, 0, 0, 8),
        Seq(0, 0, 7, 4),
        Seq(0, 6, 3, 2),
        Seq(5, 2, 3, 6),
        Seq(1, 4, 7, 0),
        Seq(5, 8, 0, 0),
        Seq(9, 0, 0, 0)
      ).map(_.map(_.U(16.W)))

      c.io.in.zip(input).foreach { case (inRow, inputRow) =>
        inRow.zip(inputRow).foreach { case (inElem, inputElem) =>
          inElem.poke(inputElem)
        }
      }

      c.clock.step(1)

      c.io.out.zip(expectedOutput).foreach { case (outRow, expectedRow) =>
        outRow.zip(expectedRow).foreach { case (outElem, expectedElem) =>
          outElem.expect(expectedElem)
        }
      }
    }
  }
}
