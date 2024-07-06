#include <iostream>
#include <verilated.h>
#include "Vgen_mnist_baseline.h"

#include <fstream>
#include "json.hpp"

using json = nlohmann::json;

void set_char(VlWide<64> &io_in, int index, char data) {
  unsigned long val = (unsigned long)io_in[index / 4];
  val = val & ~(0xff << (index % 4) * 8);
  val = val | (((unsigned long)(data & 0xff)) << (index % 4) * 8);
  io_in[index / 4] = val;
  // if (index / 4 == 0) {
  //   std::cout << "index: " << index << std::endl;
  //   std::cout << "data: " << ((uint)data & 0xff) << std::endl;
  //   std::cout << "io_in[0]: " << std::hex << (unsigned long)io_in[0] << std::dec << std::endl;
  // }
}

int onehotToIndex(short x) {
  for (int i = 0; i < 10; i++) {
    if (x & 1) {
      return i;
    }
    x = x >> 1;
  }
  return -1;
}

int main(int argc, char **argv) {
  Verilated::commandArgs(argc, argv);

  std::ifstream f("test_image.json");
  json data = json::parse(f);

  Vgen_mnist_baseline *dut = new Vgen_mnist_baseline();
  for (int i = 0; i < 256; i++) {
    set_char(dut->io_in, i, data["img"][0][i].template get<char>());
  }

  // print dut->io_in[0]
  // std::cout << std::hex << dut->io_in[1] << std::endl;

  dut->eval();

  std::cout << "Prediction: " << onehotToIndex(dut->io_out) << std::endl;

  dut->final();
}