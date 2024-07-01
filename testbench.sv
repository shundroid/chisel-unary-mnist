module testbench;
  logic clk;
  logic rst;
  initial begin
    clk = 0;
    forever #5 clk = ~clk;
  end

  int i;
  initial begin
    $dumpfile("testbench.vcd");
    $dumpvars(0, testbench);
    rst = 1;
    #10;
    rst = 0;
    #1280;
    $display("i: %d\n", i);
    $finish;
  end

  logic out;
  uTestCircuit uTestCircuit (
    .clock (clk),
    .reset (rst),
    .io_out (out)
  );

  int cycle;
  always @(negedge clk) begin
    cycle = cycle + 1;
    i = i + out;
  end
endmodule