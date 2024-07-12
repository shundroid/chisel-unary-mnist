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

// int onehotToIndex(short x) {
//   for (int i = 0; i < 10; i++) {
//     if (x & 1) {
//       return i;
//     }
//     x = x >> 1;
//   }
//   return -1;
// }

int main(int argc, char **argv) {
  Verilated::commandArgs(argc, argv);

  std::ifstream f("test_image2.json");
  json data = json::parse(f);

  Vgen_mnist_baseline *dut = new Vgen_mnist_baseline();
  for (int i = 0; i < 256; i++) {
    set_char(dut->io_in, i, data["img"][0][i].template get<char>());
  }

  // print dut->io_in[0]
  // std::cout << std::hex << dut->io_in[1] << std::endl;

  dut->eval();

  for (int i = 0; i < 10; i++) {
    std::cout << i << ": " << get_prediction(dut->io_out, i) << std::endl;
  }
  // std::cout << "Prediction: " << onehotToIndex(dut->io_out) << std::endl;

  dut->final();
}