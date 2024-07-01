module uTestCircuitWrapper(
  input wire clock,
  input wire reset,
  input wire io_in,
  output wire io_out
);
  uTestCircuitRepeat c(clock, reset, io_in, io_out);
endmodule
