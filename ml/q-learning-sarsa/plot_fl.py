import pandas as pd
import matplotlib.pyplot as plt

q = pd.read_csv("results_qlfl_4.csv")
s = pd.read_csv("results_sfl_4.csv")

plt.figure()
plt.plot(q["episode"], q["success_rate100"], label="Q-learning")
plt.plot(s["episode"], s["success_rate100"], label="SARSA")
plt.xlabel("Episode")
plt.ylabel("Rolling Success Rate (100)")
plt.title("FrozenLake-v1: Q-learning vs SARSA")
plt.legend()
plt.savefig("fl_q_vs_s_4.png", dpi=200)
plt.show()