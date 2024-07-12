package unary

import _root_.circt.stage.ChiselStage
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

object Export extends App {
  val filePath = Paths.get("gen.sv")
  Files.write(
    filePath,
    ChiselStage.emitSystemVerilog(
      gen = new uTestCircuitRepeat(1),
      firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
    ).getBytes(StandardCharsets.UTF_8)
  )
}

object ExportBaseline extends App {
  val filePath = Paths.get("gen_baseline.sv")
  Files.write(
    filePath,
    ChiselStage.emitSystemVerilog(
      gen = new uBaseline,
      firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
    ).getBytes(StandardCharsets.UTF_8)
  )
}

object ExportMnistBaseline extends App {
  val filePath = Paths.get("gen_mnist_baseline.sv")
  Files.write(
    filePath,
    ChiselStage.emitSystemVerilog(
      gen = new mnist.BaselineUnified("basic_tf_quant_2146.json"),
      firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
    ).getBytes(StandardCharsets.UTF_8)
  )
}

// object ExportMnistUnary extends App {
//   val filePath = Paths.get("gen_mnist_unary.sv")
//   Files.write(
//     filePath,
//     ChiselStage.emitSystemVerilog(
//       gen = new mnist.UnaryWithInput("basic_tf_quant_2146.json", false),
//       firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
//     ).getBytes(StandardCharsets.UTF_8)
//   )
// }

object ExportMnistUnaryUnified extends App {
  val filePath = Paths.get("gen_mnist_unary_unified.sv")
  Files.write(
    filePath,
    ChiselStage.emitSystemVerilog(
      gen = new mnist.UnaryUnified("basic_tf_quant_2146.json", false),
      firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
    ).getBytes(StandardCharsets.UTF_8)
  )
}

object ExportMnistUnaryWithInputUnified extends App {
  val filePath = Paths.get("gen_mnist_unary_with_input_unified.sv")
  Files.write(
    filePath,
    ChiselStage.emitSystemVerilog(
      gen = new mnist.UnaryWithInputUnified("basic_tf_quant_2146.json", true),
      firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
    ).getBytes(StandardCharsets.UTF_8)
  )
}

object ExportCounter extends App {
  val filePath = Paths.get("gen_counter.sv")
  Files.write(
    filePath,
    ChiselStage.emitSystemVerilog(
      gen = new unary.Counter,
      firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
    ).getBytes(StandardCharsets.UTF_8)
  )
}