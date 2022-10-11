package chiseltorch.nn.nasbench

import scala.io.Source
import net.liftweb.json._

case class CellGraphNode(
                            val op: String,
                            val predecessors: Seq[CellGraphNode],
                            val successors: Seq[CellGraphNode],
                        ) {
    override def toString: String = op
}

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

    def setNumChannel(node: CellGraphNode, num_channels: Int): CellGraph = {
        CellGraph(nodes.map(n => {
            if (n == node) {
                CellGraphNode(n.op, n.predecessors, n.successors)
            } else {
                n
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
        val adj_matrix_ops = parsedJson match {
            case JArray(nodes) =>
                nodes.head match {
                    case JObject(obj) => obj.flatMap({
                        case JField("module_adjacency", JArray(value)) => Some(("module_adjacency", value))
                        case JField("module_operations", JArray(value)) => Some(("module_operations", value))
                        case x =>
                            None
                    })
                    case _ => throw new Exception("Invalid JSON")
                }
            case _ =>
                throw new Exception("Invalid JSON")
        }
        val adj_matrix_raw = adj_matrix_ops.find(_._1 == "module_adjacency").get._2
        val adj_matrix = adj_matrix_raw.map {
            case JArray(value) => value.map({
                case JInt(value) => value.toInt
                case _ => throw new Exception("Invalid JSON")
            })
            case _ => throw new Exception("Invalid JSON")
        }
        val ops_raw = adj_matrix_ops.find(_._1 == "module_operations").get._2
        val ops = ops_raw.map {
            case JString(value) => value
            case _ => throw new Exception("Invalid JSON")
        }

        val nodes = ops.zipWithIndex.map {
            case (op, index) => CellGraphNode(op, Seq.empty, Seq.empty)
        }

        val graph = CellGraph(nodes)

        val graph_with_edges = adj_matrix.zipWithIndex.foldLeft(graph) {
            case (graph, (row, row_index)) =>
                row.zipWithIndex.foldLeft(graph) {
                    case (graph, (value, col_index)) =>
                        if (value == 1) {
                            graph.add_edge(graph.nodes(row_index), graph.nodes(col_index))
                        } else {
                            graph
                        }
                }
        }

        graph_with_edges
    }

    def computeNumChannels(graph: CellGraph, num_channels: Int): Map[CellGraphNode, Int] = {
        var channel_map = Map.empty[CellGraphNode, Int]
        val output_node = graph.nodes.find(_.op == "output").get
        channel_map = channel_map + (output_node -> num_channels)

        val output_predecessors = output_node.predecessors.filter(_.op != "input")
        val output_predecessor_channels: Int = num_channels / output_predecessors.length
        output_predecessors.foreach(node => {
            channel_map = channel_map + (node -> output_predecessor_channels)
        })

        // DFS traceback
        def traceback(n: CellGraphNode): Unit = {
            n.predecessors.foreach(p => channel_map = channel_map + (p -> channel_map(n)))
            n.predecessors.foreach(traceback)
        }
        output_predecessors.foreach(traceback)

        val input_node = graph.nodes.find(_.op == "input").get
        channel_map = channel_map + (input_node -> num_channels)

        channel_map
    }
}

object CellGraphTest extends App {
    val json = Source.fromFile("1.json")
    val g = CellGraph.fromJSON(json.mkString)
    val channel_map = CellGraph.computeNumChannels(g, 128)
    println(g)
    println(channel_map)
}
