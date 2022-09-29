package tensor


trait NDSeq[T]
case class OneDSeq[T](seq: Seq[T]) extends NDSeq[T]
case class NDSeqSeq[T](seq: Seq[NDSeq[T]]) extends NDSeq[T]
