package chiseltorch.nn.nasbench

case class IntGraphNode(id: Int, predecessors: Seq[IntGraphNode], successors: Seq[IntGraphNode]) {
    override def toString: String = id.toString
}

case class IntGraph(nodes: Map[Int, IntGraphNode]) {
    def addNode(id: Int): IntGraph = {
        IntGraph(nodes + (id -> IntGraphNode(id, Seq(), Seq())))
    }

    def addEdge(from: Int, to: Int): IntGraph = {
        IntGraph(nodes.map(node => {
            if (node._1 == from) {
                (node._1, IntGraphNode(node._2.id, node._2.predecessors, node._2.successors :+ nodes(to)))
            } else if (node._1 == to) {
                (node._1, IntGraphNode(node._2.id, node._2.predecessors :+ nodes(from), node._2.successors))
            } else {
                node
            }
        }))
    }


    def apply(id: Int): IntGraphNode = nodes(id)

    override def toString: String = {
        nodes.map(node => {
            val from_ids = node._2.predecessors.map(_.id)
            val to_ids = node._2.successors.map(_.id)
            s"(${from_ids.mkString(", ")}) -> ${node._1} -> (${to_ids.mkString(", ")})"
        }).mkString("\n")
    }
}

