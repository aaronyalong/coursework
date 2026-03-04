# libraries
from auxFunctions import getState, createEmptyQTable, maxAction, save_obj
import gymnasium as gym
import numpy as np
import random
import csv

# new environment
env = gym.make("FrozenLake-v1", is_slippery=True)

episodes = 50000 # episodes
alpha = 0.05      # learning rate
# alpha = 0.1      # learning rate
gamma = 0.9      # discount factor
epsilon = 1.0    # e-Greedy

# states are discrete, can define as an array
n_states = env.observation_space.n
n_actions = env.action_space.n
Q = np.zeros((n_states, n_actions))

# rewards 
successes = np.zeros(episodes)
success_rate100 = np.zeros(episodes)

# episodes
for i in range(episodes):
    observation, info = env.reset()
    state = observation

    done = False
    score = 0

    # action before truncation
    if random.random() < epsilon:
        action = env.action_space.sample()
    else:
        action = int(np.argmax(Q[state]))

    while not done:
        next_obs, reward, terminated, truncated, info = env.step(action)
        done = terminated or truncated
        next_state = next_obs

        # next action 
        if not done:
            if random.random() < epsilon:
                next_action = env.action_space.sample()
            else:
                next_action = int(np.argmax(Q[next_state]))
        else:
            next_action = 0

        # update
        target = reward + (gamma * Q[next_state, next_action] if not done else 0.0)
        Q[state, action] += alpha * (target - Q[state, action])

        state = next_state
        action = next_action
        score += reward

    # performance metric
    successes[i] = 1 if score > 0 else 0
    success_rate100[i] = np.mean(successes[max(0, i-99):i+1])

    epsilon = epsilon - 2/episodes if epsilon > 0.01 else 0.01

# store results into csv for plot
with open("results_sfl_4.csv", "w", newline="") as f:
    w = csv.writer(f)
    w.writerow(["episode", "success", "success_rate100"])
    for ep in range(episodes):
        w.writerow([ep+1, successes[ep], success_rate100[ep]])

print("Saved results_sfl_4.csv")