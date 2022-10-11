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

    def computeNumChannels(graph: IntGraph, ops_map: Map[Int, String], num_channels: Int): Map[Int, Int] = {
        // find index of output in ops_map
        val output_index = ops_map.find(_._2 == "output").get._1
        val input_index = ops_map.find(_._2 == "input").get._1

        var channel_map = Map.empty[Int, Int]
        channel_map += (output_index -> num_channels)

        // find all nodes that are predecessors of output
        val output_predecessors = graph(output_index).predecessors.filter(_.id != input_index)
        val output_predecessors_channels = num_channels / output_predecessors.length
        output_predecessors.foreach(node => channel_map += (node.id -> output_predecessors_channels))

        def backtrace(node: IntGraphNode): Unit = {
            val predecessors = node.predecessors.filter(_.id != input_index)
            predecessors.foreach(p => channel_map += (p.id -> channel_map(node.id)))
            predecessors.foreach(backtrace)
        }
        output_predecessors.foreach(backtrace)
        channel_map += (input_index -> num_channels)
        channel_map
    }
}

object CellGraphTest extends App {
    val json = Source.fromFile("1.json")
    val jsonp = net.liftweb.json.parse(json.mkString)
    val (g, op_map) = CellGraph.fromJSON(jsonp)
    val channel_map = CellGraph.computeNumChannels(g, op_map, 32)

    println(g)
    println(op_map)
    println(channel_map)
}
