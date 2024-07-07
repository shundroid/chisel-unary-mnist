# load unary_tb.json
import json
import numpy as np
import matplotlib.pyplot as plt
import os
import sys

with open("unary_tb.json", 'r') as f:
  data = json.load(f)["out"]

acc = []

max_iter = len(data[0])

for i in range(len(data)):
  acc.append([])
  for j in range(max_iter):
    if j == 0:
      acc[i].append(data[i][j])
    else:
      acc[i].append(acc[i][-1] + data[i][j])

mean = []
for i in range(len(data)):
  mean.append([])
  for j in range(max_iter):
    mean[i].append(acc[i][j] / (j + 1))

x = np.arange(len(acc[0]))
for i in range(len(data)):
  plt.plot(x, mean[i], label=f"{i}")

plt.xlabel("Iteration")
plt.ylabel("Prediction")

plt.legend(loc='upper left', bbox_to_anchor=(1, 1))
plt.savefig("unary_tb.png")
