verilator --cc gen_mnist_unary_with_input_unified.sv --exe unary_tb_multi.cpp
make -j -C obj_dir/ -f Vgen_mnist_unary_with_input_unified.mk
./obj_dir/Vgen_mnist_unary_with_input_unified
