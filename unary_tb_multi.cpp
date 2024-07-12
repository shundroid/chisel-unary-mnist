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

template <std::size_t N>
std::array<int, N> sort_indices(const std::array<int, N>& arr) {
    std::array<int, N> indices;
    for (std::size_t i = 0; i < N; ++i) {
        indices[i] = i;
    }

    std::sort(indices.begin(), indices.end(), [&arr](std::size_t i1, std::size_t i2) {
        return arr[i1] > arr[i2];
    });

    return indices;
}

int run(json data) {
  Vgen_mnist_unary_with_input_unified *dut = new Vgen_mnist_unary_with_input_unified();
  for (int i = 0; i < 256; i++) {
    set_char(dut->io_in, i, data["image"][i].template get<char>());
  }

  dut->clock = 0;
  dut->reset = 1;
  dut->eval();
  dut->clock = 1;
  dut->eval();
  dut->reset = 0;
  // dut->clock = 0;
  // dut->eval();
  // dut->clock = 1;
  // dut->eval();

  // create 10 lists
  std::array<int, 10> lists;
  for (int i = 0; i < 10; i++) {
    lists[i] = 0;
  }

  for (int i = 0; i < 1024 * 2; i++) {
    for (int j = 0; j < 10; j++) {
      lists[j] += (dut->io_out >> j) & 1;
    }
    dut->clock = 0;
    dut->eval();
    dut->clock = 1;
    dut->eval();
  }

  dut->final();

  // for (int i = 0; i < 10; i++) {
  //   std::cout << i << ": " << lists[i] << std::endl;
  // }

  std::array<int, 10> sorted_indices = sort_indices(lists);
  std::cout << "top 1: " << sorted_indices[0] << ", correct: " << data["pred_sort"][0] << std::endl;
  int correct = 0;
  for (int i = 0; i < 10; i++) {
    if (sorted_indices[i] == data["pred_sort"][i].template get<int>()) {
      correct++;
    } else {
      break;
    }
  }
  return correct;
}

int main(int argc, char **argv) {
  Verilated::commandArgs(argc, argv);

  std::ifstream f("test_images.json");
  json data = json::parse(f);

  ssize_t size = 100; // data.size();
  std::cout << "test images: " << size << std::endl;

  int top1_count = 0;
  int baseline_correct = 0;
  for (int i = 0; i < size; i++) {
    int correct = run(data[i]);
    if (data[i]["label"].template get<int>() == data[i]["pred_sort"][0].template get<int>()) {
      baseline_correct++;
    }
    // std::cout << correct << std::endl;
    if (correct > 0) {
      top1_count++;
    }
    if (i % 100 == 0) {
      std::cout << i << " finished" << std::endl;
    }
  }
  std::cout << "accuracy: " << top1_count << " out of " << size << std::endl;
  std::cout << "baseline accuracy: " << baseline_correct << " out of " << size << std::endl;
}