module BaselineWrapper(
  input wire clock,
  input wire reset,
  input wire [2047:0] io_in,
  output wire [9:0] io_out
);
  BaselineUnified c(
    .clock(clock),
    .reset(reset),
    .io_in(io_in),
    .io_out(io_out)
  );
endmodule