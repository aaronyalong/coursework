from auxFunctions import getState, createEmptyQTable, maxAction, save_obj
import gymnasium as gym
import random
import numpy as np
import csv

env = gym.make('MountainCar-v0')
env._max_episode_steps = 1000

# Create an empty Q-table
Q = createEmptyQTable()

# Hyperparameters 
# alpha = 0.1 # Learning Rate
alpha = 0.2 # Learning Rate
gamma = 0.9 # Discount Factor 
epsilon = 1 # e-Greedy 
# episodes = 50000 
episodes = 10000 

score = 0
# Variable to keep track of the total score obtained
# at each episode to plot it later
total_score = np.zeros(episodes)
avg100 = np.zeros(episodes)

for i in range(episodes):
    done = False
    observation, info = env.reset()
    state = getState(observation)
    
    if i % 500 == 0:
        print(f'episode: {i}, score: {score}, epsilon: {epsilon:0.3f}')
    
    score = 0
    while not done:
        # e-Greedy strategy
        # Explore random action with probability epsilon
        if random.uniform(0, 1) < epsilon:
            action = env.action_space.sample()
        # Take best action with probability 1-epsilon
        else:
            action = maxAction(Q, state)
        
        # Observe next state based on 
        next_observation, reward, terminated, truncated, info = env.step(action)
        done = terminated or truncated
        next_state = getState(next_observation)
        
        # Add reward to the score of the episode
        score += reward
        
        # Get next action
        next_action = maxAction(Q, next_state)
        
        # Update Q value for state and action given the bellman equation
        Q[state, action] = Q[state, action] + alpha * (reward + gamma * Q[next_state, next_action] - Q[state, action]) 
        
        # Move to next state
        state = next_state

    # Save score for this episode
    total_score[i] = score

    avg100[i] = np.mean(total_score[max(0, i-99):i+1])
    # Reduce epsilon 
    epsilon = epsilon - 2/episodes if epsilon > 0.01 else 0.01
    #epsilon = max(0.01, epsilon * 0.995)

# Save Q-table as .pkl file
save_obj(Q, 'Q-table-Q-Learning')

with open("results_qlmc_4.csv", "w", newline="") as f:
    w = csv.writer(f)
    w.writerow(["episode", "return", "avg100"])
    for ep in range(episodes):
        w.writerow([ep+1, total_score[ep], avg100[ep]])