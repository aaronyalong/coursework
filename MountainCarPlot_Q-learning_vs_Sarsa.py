import pandas as pd
import matplotlib.pyplot as plt

q = pd.read_csv("results_qlmc_4.csv")
s = pd.read_csv("results_smc_4.csv")

plt.figure()
plt.plot(q["episode"], q["avg100"], label="Q-learning")
plt.plot(s["episode"], s["avg100"], label="SARSA")
plt.xlabel("Episode")
plt.ylabel("Rolling Avg Return (100)")
plt.title("MountainCar-v0: Q-learning vs SARSA")
plt.legend()
plt.savefig("mountaincar_q_vs_sarsa_v4.png", dpi=200)
plt.close()
print("Saved mountaincar_q_vs_sarsa_v4.png")