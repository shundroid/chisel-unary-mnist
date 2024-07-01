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