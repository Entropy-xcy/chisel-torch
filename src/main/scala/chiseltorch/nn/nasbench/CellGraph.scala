package chiseltorch.nn.nasbench

import scala.io.Source
import net.liftweb.json._

import scala.sys.exit


object CellGraph {
    def fromJSON(json: JValue): (IntGraph, Map[Int, String]) = {
        val adj_mat_raw = json(0) \ "module_adjacency"
        val ops_raw = json(0) \ "module_operations"

        val ops = ops_raw.children.map(_.values.toString)

        val adj_matrix = adj_mat_raw.children.map(_.children.map(_.values.toString.toInt))


        val op_map = ops.zipWithIndex.map {
            case (op, index) => index -> op
        }.toMap

        val init_g = IntGraph(Map.empty)
        val g_with_nodes = adj_matrix.indices.foldLeft(init_g) {
            case (g, i) => g.addNode(i)
        }
        val graph_x = adj_matrix.indices.foldLeft(g_with_nodes) {
            case (g, i) => adj_matrix(i).indices.foldLeft(g) {
                case (g, j) => if (adj_matrix(i)(j) == 1) g.addEdge(i, j) else g
            }
        }
        (graph_x, op_map)
    }

//    def computeNumChannels(graph: CellGraph, num_channels: Int): Map[CellGraphNode, Int] = {
//        var channel_map = Map.empty[CellGraphNode, Int]
//        val output_node = graph.nodes.find(_.op == "output").get
//        channel_map = channel_map + (output_node -> num_channels)
//
//        val output_predecessors = output_node.predecessors.filter(_.op != "input")
//        val output_predecessor_channels: Int = num_channels / output_predecessors.length
//        output_predecessors.foreach(node => {
//            channel_map = channel_map + (node -> output_predecessor_channels)
//        })
//
//        // DFS traceback
//        def traceback(n: CellGraphNode): Unit = {
//            n.predecessors.foreach(p => channel_map = channel_map + (p -> channel_map(n)))
//            n.predecessors.foreach(traceback)
//        }
//        output_predecessors.foreach(traceback)
//
//        val input_node = graph.nodes.find(_.op == "input").get
//        channel_map = channel_map + (input_node -> num_channels)
//
//        channel_map
//    }
}

object CellGraphTest extends App {
    val json = Source.fromFile("1.json")
    val jsonp = net.liftweb.json.parse(json.mkString)
    val (g, op_map) = CellGraph.fromJSON(jsonp)

    println(g)
}
