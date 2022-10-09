package chiseltorch.common

trait NDSeq[T] {
    def toString: String
}

case class OneDSeq[T](seq: Seq[T]) extends NDSeq[T] {
    override def toString = {
        seq.mkString("[", ", ", "]")
    }
}

case class NDSeqSeq[T](seq: Seq[NDSeq[T]]) extends NDSeq[T] {
    override def toString = {
        seq.mkString("[\n", ",\n ", "\n]")
    }
}

object NDSeq {
    def apply[T](shape: Seq[Int], seq: Seq[T]): NDSeq[T] = {
        assert(shape.product == seq.size)
        def helper(shape: Seq[Int], seq: Seq[T]): NDSeq[T] = {
            shape.length match {
                case 1 => OneDSeq(seq)
                case _ =>
                    val seq_split = seq.grouped(seq.length / shape.head).toSeq
                    val ndseqs: Seq[NDSeq[T]] = seq_split.map(helper(shape.tail, _)).toList
                    NDSeqSeq(ndseqs)
            }
        }

        helper(shape, seq)
    }

    def indexDim[T](ndseq: NDSeq[T], dim: Int, sel: Int): NDSeq[T] = {
        // Return a tensor with the given dimension selected
        if(dim == 0) {
            ndseq match {
                case OneDSeq(seq) => OneDSeq(Seq(seq(sel)))
                case NDSeqSeq(seq) => NDSeqSeq(Seq(seq(sel)))
            }
        } else {
            ndseq match {
                case OneDSeq(seq) => throw new Exception("Cannot index dimension of 1DSeq")
                case NDSeqSeq(seq) => NDSeqSeq(seq.map(indexDim(_, dim - 1, sel)))
            }
        }
    }

    def flatten[T](ndseq: NDSeq[T]): Seq[T] = {
        ndseq match {
            case OneDSeq(seq) => seq
            case NDSeqSeq(seq) => seq.flatMap(flatten(_))
        }
    }
}

object NDSeqTest extends App {
    val od0 = OneDSeq(Seq(1, 2, 3))
    val od1 = OneDSeq(Seq(4, 5, 6))
    val nd0 = NDSeqSeq(Seq(od0, od1))
    val flat = NDSeq.flatten(nd0)
    val idx = NDSeq.indexDim(nd0, 1, 1)
    val idx_flat = NDSeq.flatten(idx)
    println(flat)
    println(idx_flat)
    println("Hello")
}