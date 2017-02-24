# xxi Bomberman Bot

[Bomberman]("https://github.com/EntelectChallenge/2016-Bomberman") game bot using greedy algorithm, written in Python 2.

Created for K-01 IF2211 Strategi Algoritma (Algorithms) class, Teknik Informatika Institut Teknologi Bandung, by:
- Jonathan Christopher (13515001)
- Turfa Auliarachman (13515133)
- Jordhy Fernando (13515004) 

## How to run

1. Download the [Bomberman game engine]("https://github.com/EntelectChallenge/2016-Bomberman") from its Github repository. Version 1.2.6 is recommended.

2. Extract the `Game Engine` folder and place in in this directory's parent directory.

3. To run a match between bots, use `run-bvb.bat` or `<path to Bomberman.exe> --pretty --debug -b "<Path to bot 1>" "<Path to bot 2>" "<Path to bot 3>" "<Path to bot 4>"`.

4. To run a human-vs-bot match, use `run-pvb.bat` or `<path to Bomberman.exe> --pretty -c 1 -b "<Path to bot>"`.

## Strategy

For each round, the bot will try to execute the following goals, starting from the highest priority to the lowest:

```
# Precompute

Calculate bomb, danger, blast and target zones.
- danger_zones: map of areas that will be hit by blast (marked 'x').
Areas that have a high probability of being blasted in the next turn are marked '*'.
- target_zones: map of areas that will probably be blasted by our own bomb (marked 'x').
Note: blast area calculation does not consider bomb/player blast cascade effect.
- power_up_map: danger_zones map, but with location of powerups.

# Priorities

1. If in danger zone, try to find a path to nearest safe location. If there's no path to safety, place bomb and trigger it (the enemy who placed bomb that trapped us will probably get killed too).

2. If an enemy player is in the target zone (in range of our bombs), trigger our bombs - around 50% probability of a kill. Note: does not consider blast cascade effect yet

3. If placing a bomb will trap enemy, place bomb

4. If next to a destructible wall, place bomb or trigger if bomb not available yet

5. Try to find a path to nearest accessible power up.

6. Try to find a path to the nearest destructible wall.

7. Try to find a path to the nearest enemy.

8. Don't know what else to do, do nothing for this round.

# Notes

- The algorithms used does not consider blast cascade effect yet (one bomb's explosion triggering another).
- When the bot is about to place a bomb, it will first check whether that action could trap itself.
- The path finding/shortest path algorithm used is based on Dijkstra's algorithm.
```
