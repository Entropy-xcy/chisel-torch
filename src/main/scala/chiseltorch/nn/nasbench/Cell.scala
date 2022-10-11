//package chiseltorch.nn.nasbench
//
//import chisel3._
//import chisel3.stage.ChiselStage
//import chisel3.util._
//import chiseltorch.nn.module.ReLU
//import chiseltorch.tensor.Tensor
//
//import scala.io.Source
//
//class Cell(graph: CellGraph, channel_map: Map[CellGraphNode, Int])(input_shape: Seq[Int]) extends chiseltorch.nn.module.Module {
//    val input_tensor = Tensor.Wire(Tensor.empty(input_shape, () => chiseltorch.dtypes.UInt(8.W)))
//    val output_tensor = Tensor.Wire(Tensor.empty(input_shape, () => chiseltorch.dtypes.UInt(8.W)))
//
//
//    def construct_from_node(node: CellGraphNode): (CellGraphNode, chiseltorch.nn.module.Module) = {
//        node match {
//            case CellGraphNode("conv1x1-bn-relu", predecessors, successors) =>
//                node -> ConvBnRelu1x1(channel_map(node))(Seq(input_shape(0), channel_map(node), input_shape(1), input_shape(2)))
//            case CellGraphNode("conv3x3-bn-relu", predecessors, successors) =>
//                node -> ConvBnRelu3x3(channel_map(node))(Seq(input_shape(0), channel_map(node), input_shape(1), input_shape(2)))
//            case CellGraphNode("maxpool3x3", predecessors, successors) =>
//                node -> Maxpool3x3()(Seq(input_shape(0), channel_map(node), input_shape(1), input_shape(2)))
//            case CellGraphNode("output", predecessors, successors) =>
//                node -> ReLU()(Seq(input_shape(0), channel_map(node), input_shape(1), input_shape(2)))
//            case CellGraphNode("input", predecessors, successors) =>
//                node -> ReLU()(Seq(input_shape(0), channel_map(node), input_shape(1), input_shape(2)))
//            case _ => throw new Exception("Unknown node type")
//        }
//    }
//
//    val modules: Map[CellGraphNode, chiseltorch.nn.module.Module] = graph.nodes.map(construct_from_node).toMap
//
//
//    val io = IO(new Bundle {
//        val input = Input(input_tensor.asVecType)
//        val output = Output(output_tensor.asVecType)
//    })
//
//    input_tensor := io.input
//    io.output := output_tensor.toVec
//
//    override def input: Data = ???
//
//    override def output: Data = ???
//
//    override def in_shape: Seq[Int] = input_shape
//
//    override def out_shape: Seq[Int] = input_shape
//
//    override def param_input: Seq[Data] = ???
//}
//
//object CellTest extends App {
//    val json = Source.fromFile("1.json")
//    val g = CellGraph.fromJSON(json.mkString)
//    val channel_map = CellGraph.computeNumChannels(g, 2)
//
//    println(channel_map)
//
//    (new ChiselStage).emitChirrtl(new Cell(g, channel_map)(Seq(1, 3, 32, 32)))
//}