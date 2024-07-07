#include <iostream>
#include <verilated.h>
#include "Vgen_mnist_unary.h"

#include <array>
#include <list>
#include <fstream>
#include "json.hpp"

using json = nlohmann::json;

int main(int argc, char **argv) {
  Verilated::commandArgs(argc, argv);

  Vgen_mnist_unary *dut = new Vgen_mnist_unary();

  dut->clock = 0;
  dut->reset = 1;
  dut->eval();
  dut->clock = 1;
  dut->eval();
  dut->clock = 0;
  dut->reset = 0;
  dut->eval();
  dut->clock = 1;
  dut->eval();

  // create 10 lists
  std::array<std::list<int>, 10> lists;

  for (int i = 0; i < 1024 * 8; i++) {
    dut->clock = 0;
    dut->eval();
    dut->clock = 1;
    dut->eval();
    lists[0].push_back(dut->io_out_0);
    lists[1].push_back(dut->io_out_1);
    lists[2].push_back(dut->io_out_2);
    lists[3].push_back(dut->io_out_3);
    lists[4].push_back(dut->io_out_4);
    lists[5].push_back(dut->io_out_5);
    lists[6].push_back(dut->io_out_6);
    lists[7].push_back(dut->io_out_7);
    lists[8].push_back(dut->io_out_8);
    lists[9].push_back(dut->io_out_9);
  }

  dut->final();

  // export lists as json
  json data;
  for (int i = 0; i < 10; i++) {
    data["out"][i] = lists[i];
  }
  // save to a file
  std::ofstream o("unary_tb.json");
  o << std::setw(4) << data << std::endl;

}