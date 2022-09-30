package tensor.dtypes

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
}
