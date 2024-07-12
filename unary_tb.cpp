#include <iostream>
#include <verilated.h>
#include "Vgen_mnist_unary.h"

#include <array>
#include <list>
#include <fstream>
#include "json.hpp"

using json = nlohmann::json;

#include <iostream>
#include <verilated.h>
#include "Vgen_mnist_unary_with_input_unified.h"

#include <array>
#include <list>
#include <fstream>
#include "json.hpp"
#include <algorithm>

using json = nlohmann::json;

void set_char(VlWide<64> &io_in, int index, char data) {
  unsigned long val = (unsigned long)io_in[index / 4];
  val = val & ~(0xff << (index % 4) * 8);
  val = val | (((unsigned long)(data & 0xff)) << (index % 4) * 8);
  io_in[index / 4] = val;
}

signed long long get_prediction(VlWide<11> &io_out, int index) {
  const int output_width = 34;
  int bit_start = index * output_width;
  int bit_end = bit_start + output_width - 1;
  if (bit_end / 32 == bit_start / 32) {
    // won't happen if output_width > 32
    return (((unsigned long long)io_out[bit_start / 32]) >> (bit_start % 32)) & ((1ull << output_width) - 1);
  } else {
    unsigned long long result = ((unsigned long long)io_out[bit_start / 32]) >> (bit_start % 32);
    result &= (1ull << (32 - (bit_start % 32))) - 1;
    result |= ((unsigned long long)io_out[bit_end / 32]) << (32 - (bit_start % 32));
    result &= ((1ull << output_width) - 1);
    // sign extension
    if (result & (1ull << (output_width - 1))) {
      result |= ~((1ull << output_width) - 1);
    }
    return result;
  }
}

int main(int argc, char **argv) {
  Verilated::commandArgs(argc, argv);

  std::ifstream f("test_images.json");
  json data = json::parse(f)[0];
  Vgen_mnist_unary_with_input_unified *dut = new Vgen_mnist_unary_with_input_unified();
  for (int i = 0; i < 256; i++) {
    set_char(dut->io_in, i, data["image"][i].template get<char>());
  }

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
    for (int j = 0; j < 10; j++) {
      lists[j].push_back((dut->io_out >> j) & 1);
    }
  }

  dut->final();

  // export lists as json
  json out_data;
  for (int i = 0; i < 10; i++) {
    out_data["out"][i] = lists[i];
  }
  // save to a file
  std::ofstream o("unary_tb.json");
  o << std::setw(4) << out_data << std::endl;

}