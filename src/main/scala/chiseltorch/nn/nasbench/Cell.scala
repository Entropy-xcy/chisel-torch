package chiseltorch.nn.nasbench

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import chiseltorch.nn.module.{Act, Attention, Conv2D}
import chiseltorch.tensor.{Ops, Tensor}

import java.io.{File, PrintWriter}
import scala.collection.parallel.CollectionConverters.ImmutableIterableIsParallelizable
import scala.io.Source

class Cell(graph: IntGraph, op_map: Map[Int, String], channel_map: Map[Int, Int])(input_shape: Seq[Int]) extends chiseltorch.nn.module.Module {
    val input_tensor = Tensor.Wire(Tensor.empty(input_shape, () => chiseltorch.dtypes.UInt(8.W)))
    val output_tensor = Tensor.Wire(Tensor.empty(input_shape, () => chiseltorch.dtypes.UInt(8.W)))


    var param_ports: Seq[Data] = Seq.empty
    def construct_from_node(node: IntGraphNode): (Int, chiseltorch.nn.module.Module) = {
        val this_in_shape = Seq(input_shape(0), channel_map(node.id), input_shape(2), input_shape(3))
        val mod = op_map(node.id) match {
            case "conv1x1-bn-relu" =>
                val conv1x1 = Module(ConvBnRelu1x1(channel_map(node.id))(this_in_shape))
                conv1x1
            case "conv3x3-bn-relu" =>
                val conv3x3 = Module(ConvBnRelu3x3(channel_map(node.id))(this_in_shape))
                conv3x3
            case "maxpool3x3" =>
                val maxpool3x3 = Module(MaxPool3x3()(this_in_shape))
                maxpool3x3
            case "input" =>
                val input_mod = Module(new chiseltorch.nn.module.Pipe()(this_in_shape))
                input_mod
            case "output" =>
                val output_mod = Module(new chiseltorch.nn.module.Pipe()(this_in_shape))
                output_mod
            case _ => throw new Exception("Unknown op: " + op_map(node.id))
        }

        val this_param_ports = mod.param_input
        this_param_ports.zipWithIndex.foreach(pWithId => {
            val (p, id) = pWithId
            val param_in_port = IO(Input(p.cloneType)).suggestName(s"param_in_${node.id}_$id")
            p := param_in_port
            // append to param_ports
            param_ports = param_ports :+ param_in_port
        })

        (node.id, mod)
    }

    val modules: Map[Int, chiseltorch.nn.module.Module] = graph.nodes.values.map(construct_from_node).toMap
    val input_id = op_map.find(_._2 == "input").get._1
    val output_id = op_map.find(_._2 == "output").get._1

    // Connect Modules Together
    modules.toSeq.foreach(indexWithModule => {
        val id = indexWithModule._1
        val mod = indexWithModule._2
        val this_op = op_map(id)

        val pred_in_datas = graph(id).predecessors.map(pred => {
            val pred_mod = modules(pred.id)
            val pred_op = op_map(pred.id)
            val pred_shape = pred_mod.out_shape

            val (pred_out, pred_out_shape) = if (pred_op == "input") {
                val conv_proj = Conv2D(channel_map(pred.id), channel_map(id), (1, 1), 1)(pred_shape)
                val conv_proj_port = IO(Input(conv_proj.param_input(0).cloneType)).suggestName(s"conv_proj_param_${id}_0")
                conv_proj.param_input(0) := conv_proj_port
                conv_proj.input := pred_mod.output
                (conv_proj.output, conv_proj.out_shape)
            } else {
                (pred_mod.output, pred_shape)
            }

            if (op_map(id) != "output") {
                require(mod.in_shape == pred_out_shape, s"Module $id and ${pred.id} have different shapes: ${mod.in_shape} vs ${pred_mod.out_shape}")
            }
            pred_mod.output
            val pred_mod_out_tensor = Tensor.Wire(Tensor.empty(pred_out_shape, () => chiseltorch.dtypes.UInt(8.W)))
            pred_mod_out_tensor := pred_out
            if (pred.id == input_id && id == output_id) {
                input_tensor := pred_mod_out_tensor
                println("Found Input!!!!")
            }
            (pred_mod_out_tensor, pred.id)
        })

        this_op match {
            case "output" =>
                // reduce by concat
                pred_in_datas.foreach(pred_in_data => {
                    val pred_in_d = pred_in_data._1
                    println("output_in_shape: ", pred_in_d.shape)
                })
                val pred_in_ds = pred_in_datas.filter(_._2 != input_id).map(_._1)
                val input_ds = pred_in_datas.filter(_._2 == input_id).map(_._1)
                val concat_in = Ops.concat(pred_in_ds, 1)
                mod.input := concat_in.toVec
                output_tensor := mod.output
            case "input" =>
                mod.input := input_tensor.toVec
            case _ =>
                // reduce by sum
                val pred_in_ds = pred_in_datas.map(_._1)
                val sum_tensor = pred_in_ds.reduce((a, b) => a + b)
                mod.input := sum_tensor.toVec
        }
    })

    val io = IO(new Bundle {
        val input = Input(input_tensor.asVecType)
        val output = Output(output_tensor.asVecType)
    })

    input_tensor := io.input
    io.output := output_tensor.toVec

    override def input: Data = io.input

    override def output: Data = io.output

    override def in_shape: Seq[Int] = input_shape

    override def out_shape: Seq[Int] = input_shape

    override def param_input: Seq[Data] = param_ports

    println("Finished Elaboration")
}

object CellTest extends App {
    def test_idx(i: Int): Unit = {
        println(s"Building Index: $i")
        val json = Source.fromFile(s"nasbench_metrics/$i.json")
        val jsonp = net.liftweb.json.parse(json.mkString)
        val (g, op_map) = CellGraph.fromJSON(jsonp)
        val channel_map = CellGraph.computeNumChannels(g, op_map, 2)
        (new ChiselStage).emitVerilog(new Cell(g, op_map, channel_map)(Seq(1, 2, 32, 32)))
    }

    val base_dir = "cell_out"
    (0 until 1000).foreach { i =>
        val log_output = new PrintWriter(new File(s"$base_dir/$i.log"))
        try {
            test_idx(i)
            log_output.write("Success")
            log_output.close()
        }
        catch {
            case e: Exception =>
                println(s"Failed to build index $i")
                println(e)
                log_output.write(e.toString)
                log_output.close()
        }
    }
}
