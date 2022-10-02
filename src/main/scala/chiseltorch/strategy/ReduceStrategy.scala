package chiseltorch.strategy

trait ReduceStrategy {
    def reduce[T](seq: Seq[T])(op: (T, T) => T): T
}

object SerialReduceStrategy extends ReduceStrategy {
    def reduce[T](seq: Seq[T])(op: (T, T) => T): T = seq.reduce(op)
}

object BinaryTreeReduceStrategy extends ReduceStrategy {
    def reduce[T](seq: Seq[T])(op: (T, T) => T): T = {
        def reduceSeq(seq: Seq[T]): T = seq match {
            case Seq() => throw new UnsupportedOperationException("empty seq")
            case Seq(x) => x
            case _ => {
                val (left, right) = seq.splitAt(seq.length / 2)
                op(reduceSeq(left), reduceSeq(right))
            }
        }
        reduceSeq(seq)
    }
}
