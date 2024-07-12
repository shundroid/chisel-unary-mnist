#include <iostream>
#include <verilated.h>
#include "Vgen_counter.h"

#include <array>
#include <list>
#include <fstream>
#include <algorithm>


int main(int argc, char **argv) {
  Verilated::commandArgs(argc, argv);

  Vgen_counter *dut = new Vgen_counter();

  dut->clock = 0;
  dut->reset = 1;
  dut->eval();
  dut->clock = 1;
  dut->eval();
  dut->clock = 0;
  dut->reset = 0;


  for (int i = 0; i < 8; i++) {
    std::cout << (int)dut->io_out << std::endl;
    dut->clock = 0;
    dut->eval();
    dut->clock = 1;
    dut->eval();
  }

  dut->final();
}