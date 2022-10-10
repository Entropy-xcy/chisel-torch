package chiseltorch.nn.nasbench

import scala.io.Source
import net.liftweb.json._

case class CellGraphNode(
                            val op: String,
                            val predecessors: Seq[CellGraphNode],
                            val successors: Seq[CellGraphNode],
                        )

case class CellGraph(val nodes: Seq[CellGraphNode]) {
    def add_node(node: CellGraphNode): CellGraph = {
        CellGraph(nodes :+ node)
    }

    def add_edge(from: CellGraphNode, to: CellGraphNode): CellGraph = {
        CellGraph(nodes.map(node => {
            if (node == from) {
                CellGraphNode(node.op, node.predecessors, node.successors :+ to)
            } else if (node == to) {
                CellGraphNode(node.op, node.predecessors :+ from, node.successors)
            } else {
                node
            }
        }))
    }

    override def toString: String = {
        nodes.map(node => {
            val predecessors = node.predecessors.map(_.op).mkString(", ")
            val successors = node.successors.map(_.op).mkString(", ")
            s"($predecessors) -> ${node.op} -> ($successors)"
        }).mkString("\n")
    }
}

object CellGraph {
    def fromJSON(json: String): CellGraph = {
        val parsedJson = net.liftweb.json.parse(json)
        CellGraph(Seq.empty)
    }
}

object CellGraphTest extends App {
    // parse json
    val json = Source.fromFile("1.json")
    // parse
    val g = CellGraph.fromJSON(json.mkString)
    println(g)
}
