import Queue
import copy
from entities.moves import Moves
from entities.bomb import Bomb

def map_in_range(map, location):
	x, y = location
	return (x >= 0) and (x < len(map[0])) and (y >= 0) and (y < len(map))

# Check whether a location on the map is equal to any of the characters in chars list.
# If the location is out of range, return False.
def map_equals(map, location, chars):
	equals = False
	x, y = location
	if map_in_range(map, location):
		for char in chars:
			if map[y][x] == char:
				equals = True
				break
	return equals

# Returns a (x, y) location tuple, shifted by offset in the specified direction from the original location.
# Valid directions are 'up', 'left', 'right', and 'down'.
def shift(location, direction, offset=1):
	x, y = location
	if direction == 'up':
		return (x, y-offset)
	elif direction == 'left':
		return (x-offset, y)
	elif direction == 'right':
		return (x+offset, y)
	elif direction == 'down':
		return (x, y+offset)
	else:
		raise Exception('Invalid direction')

# Returns the opposite of the direction given
def opposite(direction):
	if direction == 'up':
		return 'down'
	elif direction == 'left':
		return 'right'
	elif direction == 'right':
		return 'left'
	elif direction == 'down':
		return 'up'
	else:
		raise Exception('Invalid direction')

# Debug helper function
def print_map(map):
	for i in range(0, len(map)):
		row = ''
		for j in range(0, len(map[0])):
			row = row + map[i][j]
		print row

# Backtrack helper function for shortest_path; returns sequence of moves from path start to path end.
def shortest_path_backtrack(map, distances, end_location, costs):
	bt_location = end_location
	bt_x, bt_y = bt_location
	bt_length = distances[bt_y][bt_x]
	bt_path = []
	while bt_length > 0:
		for direction in ['up', 'left', 'right', 'down']:
			bt_next_location = shift(bt_location, direction)
			bt_next_x, bt_next_y = bt_next_location
			if map_in_range(distances, bt_next_location) \
			and costs[map[bt_y][bt_x]] > 0 \
			and (distances[bt_next_y][bt_next_x] + costs[map[bt_y][bt_x]] == distances[bt_y][bt_x]):
				bt_location = bt_next_location
				bt_x, bt_y = bt_location
				bt_length = distances[bt_next_y][bt_next_x]
				if direction == 'up':
					bt_path.append(Moves.MOVE_DOWN)
				elif direction == 'left':
					bt_path.append(Moves.MOVE_RIGHT)
				elif direction == 'right':
					bt_path.append(Moves.MOVE_LEFT)
				elif direction == 'down':
					bt_path.append(Moves.MOVE_UP)
				else:
					raise Exception('Invalid direction')
				break
	bt_path.reverse()
	print "Shortest path move sequence:", bt_path # DEBUG
	return bt_path

# Dijkstra's shortest path algorithm; returns sequence of moves from start location to nearest end character.
def shortest_path(map, start, end_chars, costs):
	POS_INF = 99999999
	distances = [[POS_INF for j in range(0, len(map[0]))] for i in range(0, len(map))]
	start_x, start_y = start
	distances[start_y][start_x] = 0
	pq = Queue.PriorityQueue()
	pq.put((0, start))
	print "Finding shortest path from:", start # DEBUG

	while not pq.empty():
		path_length, current_location = pq.get()

		if map_equals(map, current_location, end_chars):
			print "Found shortest path to", current_location, ", path length", path_length # DEBUG
			return shortest_path_backtrack(map, distances, current_location, costs)			

		for direction in ['up', 'left', 'right', 'down']:
			next_location = shift(current_location, direction)
			next_x, next_y = next_location
			if map_in_range(map, next_location) and (costs[map[next_y][next_x]] >= 0):
				# print "Finding shortest path: now at", current_location, ", going", direction, "to", next_location # DEBUG
				if (path_length + costs[map[next_y][next_x]]) < distances[next_y][next_x]:
					distances[next_y][next_x] = path_length + costs[map[next_y][next_x]]
					pq.put((distances[next_y][next_x], next_location))

	return None

def calculate_danger_zones(game_state):
	danger_zones = copy.deepcopy(game_state.map)

	# Mark danger_zone blast zone with 'x'
	for bomb in game_state.bombs:
		bomb_x, bomb_y = bomb.location
		danger_zones[bomb_y][bomb_x] = 'b'
		bomb_travel = ['up', 'left', 'right', 'down']
		for i in range(1, bomb.radius+1):
			remove_direction = []
			for direction in bomb_travel:
				check_location = shift(bomb.location, direction, i)
				check_x, check_y = check_location
				if map_equals(danger_zones, check_location, ['.']):
					if bomb.timer > 2:
						danger_zones[check_y][check_x] = 'x'
				elif map_equals(danger_zones, check_location, ['#', '+']):
					remove_direction.append(direction)
			for direction in remove_direction:
				bomb_travel.remove(direction)

	# Mark danger_zones with '*' for bombs with timer <= 2
	for bomb in game_state.bombs:
		bomb_x, bomb_y = bomb.location
		danger_zones[bomb_y][bomb_x] = 'b'
		bomb_travel = ['up', 'left', 'right', 'down']
		for i in range(1, bomb.radius+1):
			remove_direction = []
			for direction in bomb_travel:
				check_location = shift(bomb.location, direction, i)
				check_x, check_y = check_location
				if map_equals(danger_zones, check_location, ['.', 'x']):
					if bomb.timer <= 2:
						danger_zones[check_y][check_x] = '*'
				elif map_equals(danger_zones, check_location, ['#', '+']):
					remove_direction.append(direction)
			for direction in remove_direction:
				bomb_travel.remove(direction)

	# Mark enemy players on danger_zones map
	for player in game_state.players:
		if player.key != game_state.current_player.key:
			enemy_x, enemy_y = player.location
			if map_equals(danger_zones, player.location, ['.', 'x']):
				danger_zones[enemy_y][enemy_x] = 'e'

	return danger_zones

def calculate_target_zones(game_state):
	target_zones = copy.deepcopy(game_state.map)	

	# Mark target_zones with 'x' for our bombs
	for bomb in game_state.bombs:
		bomb_x, bomb_y = bomb.location
		bomb_travel = ['up', 'left', 'right', 'down']
		for i in range(1, bomb.radius+1):
			remove_direction = []
			for direction in bomb_travel:
				check_location = shift(bomb.location, direction, i)
				check_x, check_y = check_location
				if map_equals(target_zones, check_location, ['.']):
					if bomb.owner == game_state.current_player.key:
						target_zones[check_y][check_x] = 'x'
				elif map_equals(target_zones, check_location, ['#', '+']):
					remove_direction.append(direction)
			for direction in remove_direction:
				bomb_travel.remove(direction)

	return target_zones

def can_escape(danger_zones, escape_from_location):
	path_to_safety = shortest_path(
		map = danger_zones,
		start = escape_from_location,
		end_chars = ['.'],
		costs = { '#': -1, '*': -1, 'b': -1, 'e': -1, '+': -1, 'x': 10, '.': 1 }
	)
	if path_to_safety is None:
		return False
	elif len(path_to_safety) > 3: # Escape is too far away, probably won't make it
		return False
	else:
		return True

def can_escape_after_bomb_placed(game_state, escape_from_location):
	placed_bomb_game_state = copy.deepcopy(game_state)
	cp = placed_bomb_game_state.current_player
	placed_bomb_game_state.bombs.append(Bomb(cp.key, cp.bomb_radius, min(cp.bomb_bag*3 + 1, 10), False, cp.location))
	placed_bomb_map = calculate_danger_zones(placed_bomb_game_state)

	return can_escape(placed_bomb_map, escape_from_location)
