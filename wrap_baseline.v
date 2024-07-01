module uBaselineWrapper(
  input wire clock,
  input wire reset,
  input wire [9:0] io_in,
  output wire [9:0] io_out
);
  uBaselineRepeat c(clock, reset, io_in, io_out);
endmodule