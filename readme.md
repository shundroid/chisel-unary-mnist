# chisel-unary

Unary Computing で MNIST を計算する NN を実装しました。

## 準備

scala と chisel, verilator が必要です。

Scala 3.4.2, Chisel 6.2.0, Verilator 5.026 で検証しています。

## 実行(verilator)

`src/main/scala/export.scala` の `ExportMnistUnaryWithInputUnified` object を実行してください。

`gen_mnist_unary_with_input_unified.sv` が生成されます。

その後、 `./run_multi.sh` を実行すると、verilator でテストデータ 100 個の Baseline との一致を確認できます。

## Vivado で合成

`src/main/scala/export.scala` の `ExportMnistUnaryUnified` object を実行してください。

`gen_mnist_unary_unified.sv` が生成されます。これと `wrap_unary_mnist.v` を用いて Vivado で合成できます。
