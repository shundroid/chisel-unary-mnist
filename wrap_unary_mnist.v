module UnaryWrapper(
  input wire clock,
  input wire reset,
  input wire [255:0] io_in,
  output wire [9:0] io_out
);
  UnaryUnified c(
    .clock(clock),
    .reset(reset),
    .io_in(io_in),
    .io_out(io_out)
  );
endmodule