import argparse
import json
import logging
import os
import random
import math
from pprint import pprint


logger = logging.getLogger()
logging.basicConfig(level=logging.DEBUG, format='%(asctime)s - %(levelname)-7s - [%(funcName)s] %(message)s')
# uncomment for submission
# logger.disabled = True

ACTIONS = {
	-1: 'DoNothing',
	1: 'MoveUp',
	2: 'MoveLeft',
	3: 'MoveRight',
	4: 'MoveDown',
	5: 'PlaceBomb',
	6: 'TriggerBomb',
}


def main(player_key, output_path):

	logger.info('Player key: {}'.format(player_key))
	logger.info('Output path: {}'.format(output_path))

	with open(os.path.join(output_path, 'state.json'), 'r') as f:
		state = json.load(f)
		# logger.info('State: {}'.format(state))


	# Constants for json path
	PLAYER_ENTITY = "RegisteredPlayerEntities"
	GAME_BLOCKS = "GameBlocks"

	# Constants for data about map
	MAP_HEIGHT = state["MapHeight"]
	MAP_WIDTH = state["MapWidth"]
	CURRENT_ROUND = state["CurrentRound"]

	# Constants for entity type
	WALL = "Domain.Entities.IndestructibleWallEntity, Domain"
	OBSTACLE = "Domain.Entities.DestructibleWallEntity, Domain"
	PLAYER = "Domain.Entities.PlayerEntity, Domain"
	SUPER_POWER_UP = "Domain.Entities.PowerUps.SuperPowerUp, Domain"
	POWER_UP_BOMBBAG = "Domain.Entities.PowerUps.BombBagPowerUpEntity, Domain"
	POWER_UP_BOMBRADIUS = "Domain.Entities.PowerUps.BombRaduisPowerUpEntity, Domain" # emang typo dari sananya kok :(
	TOTAL_PLAYER = len(state[PLAYER_ENTITY])

	# Class queue
	class Queue:
		def __init__(self):
			self.items = []

		def isEmpty(self):
			return self.items == []

		def enqueue(self, item):
			self.items.insert(0,item)

		def dequeue(self):
			return self.items.pop()

		def size(self):
			return len(self.items)


	# Functions and Procedures

	def Player_Name(index):
	# Getter untuk nama bot
		return(state[PLAYER_ENTITY][index]["Name"])

	def Player_Index(key):
	# Getter untuk indeks bot
		i = 0
		while ((i < TOTAL_PLAYER) and (Player_Key(i) != key)):
			i += 1
		return (i)

	def Player_Key(index):
	# Getter untuk key bot
		return (state[PLAYER_ENTITY][index]["Key"])

	def Player_Points(index):
	# Getter untuk jumlah point yang dimiliki bot
		return (state[PLAYER_ENTITY][index]["Points"])

	def Player_Killed(index):
	# Getter untuk status nyawa musuh
		return(state[PLAYER_ENTITY][index]["Killed"])

	def Player_BombBag(index):
	# Getter untuk jumlah bomb bag yang dimiliki bot
		return(state[PLAYER_ENTITY][index]["BombBag"])

	def Player_BombRadius(index):
	# Getter untuk blast radius yang dimiliki bot
		return(state[PLAYER_ENTITY][index]["BombRadius"])

	def Player_X(index):
	# Getter untuk absis bot
		return(state[PLAYER_ENTITY][index]["Location"]["X"])

	def Player_Y(index):
	# Getter untuk ordinat bot
		return(state[PLAYER_ENTITY][index]["Location"]["Y"])

	def Map_Entity(x, y):
	# Getter untuk entitas yang berada pada petak (x, y)
		if (state[GAME_BLOCKS][x-1][y-1]["Entity"] == None):
			return(None)
		elif (state[GAME_BLOCKS][x-1][y-1]["Entity"]["$type"] == PLAYER):
			return(state[GAME_BLOCKS][x-1][y-1]["Entity"]["Key"])
		else:
			return(state[GAME_BLOCKS][x-1][y-1]["Entity"]["$type"])

	def Map_Bomb(x, y):
	# Bernilai true apabila ada bom pada petak (x, y)
		if (state[GAME_BLOCKS][x-1][y-1]["Bomb"] == None):
			return(False)
		else:
			return(True)

	def Map_Bomb_Key(x, y):
	# Getter untuk pemilik bom pada petak (x, y) 
		return(state[GAME_BLOCKS][x-1][y-1]["Bomb"]["Owner"]["Key"])

	def Map_Bomb_Radius(x, y):
	# Getter untuk blast radius bom yang terletak pada petak (x, y)
		return(state[GAME_BLOCKS][x-1][y-1]["Bomb"]["BombRadius"])

	def Map_Bomb_Timer(x, y):
	# Getter untuk timer bom yang terletak pada petak (x, y)
		return(state[GAME_BLOCKS][x-1][y-1]["Bomb"]["BombTimer"])

	def Map_PowerUp(x, y):
	# Getter untuk power up pada petak (x, y)
		if (state[GAME_BLOCKS][x-1][y-1]["PowerUp"] == None):
			return(None)
		else:
			return(state[GAME_BLOCKS][x-1][y-1]["PowerUp"]["$type"])

	def Map_Exploding(x, y):
	# Getter untuk status peledakan petak (x, y)
		return(state[GAME_BLOCKS][x-1][y-1]["Exploding"])


	def HasPlacedBomb():
	# Memberikan nilai true apabila bot kita telah meletakkan bom dan timernya > 2
		found = False
		y = 0
		while ((y < MAP_HEIGHT) and (not found)):
			x = 0
			while ((x < MAP_WIDTH) and (not found)):
				if ((Map_Bomb(x, y)) and (Map_Bomb_Key(x, y) == player_key) and (Map_Bomb_Timer(x, y) > 2)):
					found = True
				x += 1
			y += 1
		return (found)

	def InDanger (x, y):
	# Memberi nilai true apabila bot kita berada dalam zona bahaya
	# Zona bahaya: dapat terkena ledakan bom
		danger = False
		# Left check
		x_left = x
		while ((x_left >= 0) and (Map_Entity(x_left, y) != WALL) and (Map_Entity(x_left, y) != OBSTACLE) and (not danger)):
			if (Map_Bomb(x_left, y)) and (Map_Bomb_Radius(x_left, y) >= abs(x_left - x)):
				danger = True
			else:
				x_left -= 1
		# Right check
		x_right = x + 1
		while ((x_right <= MAP_WIDTH) and (Map_Entity(x_right, y) != WALL) and (Map_Entity(x_right, y) != OBSTACLE) and (not danger)):
			if (Map_Bomb(x_right, y)) and (Map_Bomb_Radius(x_right, y) >= abs(x_right - x)):
				danger = True
			else:
				x_right += 1
		# Up check
		y_up = y - 1
		while ((y_up >= 0) and (Map_Entity(x, y_up) != WALL) and (Map_Entity(x, y_up) != OBSTACLE) and (not danger)):
			if (Map_Bomb(x, y_up)) and (Map_Bomb_Radius(x, y_up) >= abs(y_up - y)):
				danger = True
			else:
				y_up -= 1
		# Down check
		y_down = y + 1
		while ((y_down <= MAP_HEIGHT) and (Map_Entity(x, y_down) != WALL) and (Map_Entity(x, y_down) != OBSTACLE) and (not danger)):
			if (Map_Bomb(x, y_down)) and (Map_Bomb_Radius(x, y_down) >= abs(y_down - y)):
				danger = True
			else:
				y_down += 1
		# Return
		return (danger)

	def DangerCounter(x, y):
	# Mengembalikan timer bomb yang paling kecil yang dapat membahayakan bila bot berada di posisi x, y
		most_urgent_timer = 99
		# Left check
		x_left = x
		while ((x_left >= 0) and (Map_Entity(x_left, y) != WALL) and (Map_Entity(x_left, y) != OBSTACLE)):
			if ((Map_Bomb(x_left, y)) and (Map_Bomb_Radius(x_left, y) >= abs(x_left - x)) and (most_urgent_timer > Map_Bomb_Timer(x_left, y))):
				most_urgent_timer = Map_Bomb_Timer(x_left, y)
			x_left -= 1
		# Right check
		x_right = x + 1
		while ((x_right <= MAP_WIDTH) and (Map_Entity(x_right, y) != WALL) and (Map_Entity(x_right, y) != OBSTACLE)):
			if ((Map_Bomb(x_right, y)) and (Map_Bomb_Radius(x_right, y) >= abs(x_right - x)) and (most_urgent_timer > Map_Bomb_Timer(x_right, y))):
				most_urgent_timer = Map_Bomb_Timer(x_right, y)
			x_right += 1
		# Up check
		y_up = y - 1
		while ((y_up >= 0) and (Map_Entity(x, y_up) != WALL) and (Map_Entity(x, y_up) != OBSTACLE)):
			if ((Map_Bomb(x, y_up)) and (Map_Bomb_Radius(x, y_up) >= abs(y_up - y)) and (most_urgent_timer > Map_Bomb_Timer(x, y_up))):
				most_urgent_timer = Map_Bomb_Timer(x, y_up)
			y_up -= 1
		# Down check
		y_down = y + 1
		while ((y_down <= MAP_HEIGHT) and (Map_Entity(x, y_down) != WALL) and (Map_Entity(x, y_down) != OBSTACLE)):
			if ((Map_Bomb(x, y_down)) and (Map_Bomb_Radius(x, y_down) >= abs(y_down - y)) and (most_urgent_timer > Map_Bomb_Timer(x, y_down))):
				most_urgent_timer = Map_Bomb_Timer(x, y_down)
			y_down += 1
		# Return
		return(most_urgent_timer)

	def Distance (x1, y1, x2, y2):
	# Mengembalikan banyak petak yang harus dilalui apabila ingin berpindah dari (x1, y1) ke (x2, y2)
		return (abs(x1 - x2) + abs(y1 - y2))

	def PythagorasPow (x1, y1, x2, y2):
	# Mengembalikan kuadrat jarak Euclidean dari (x1, y1) ke (x2, y2)
		return ((x1-x2)**2 + (y1-y2)**2)

	def IsPowerUpInRange (x, y, radius):
	# Mengembalikan nilai true apabila terdapat power up dalam radius tertentu dari titik (x, y)
		# Mencegah x keluar batas map
		x_end = x + radius
		if (x_end > MAP_WIDTH):
			x_end = MAP_WIDTH
		# Mencegah y keluar batas map
		y_start = y - radius
		if (y_start < 1):
			y_start = 1
		y_end = y + radius
		if (y_end > MAP_HEIGHT):
			y_end = MAP_HEIGHT
		found = False # Inisialisasi awal
		# Pencarian power up per ordinat
		while ((y_start <= y_end) and (not found)):
			# Mencegah x keluar batas map
			x_start = x - radius
			if (x_start < 1):
				x_start = 1
			# Melakukan iterasi per absis
			while ((x_start <= x_end) and (not found)):
				if (Map_PowerUp(x_start, y_start) != None):
					found = True
				else:
					x_start += 1
			y_start += 1
		# Return
		return (found)

	def IsEnemyInRange (player_index,radius):
	# Mengembalikan indeks musuh (yang masih hidup) yang berada dalam radius tertentu dari bot
	# Bernilai -1 apabila tidak ada musuh yang berada dalam radius
		enemy_index = 0
		found = False
		# Pencarian musuh dalam radius tertentu
		while ((enemy_index < TOTAL_PLAYER-1) and (not found)):
			if ((enemy_index != player_index) and (not Player_Killed(enemy_index)) and (radius >= Distance(Player_X(player_index), Player_Y(player_index), Player_X(enemy_index), Player_Y(enemy_index)))):
				found = True
			else:
				enemy_index += 1
		if (found):
			return(enemy_index)
		else:
			return(-1)
		
	def SOS(index):
	# Menghasilkan aksi yang harus bot lakukan untuk melarikan diri dari zona bahaya
		goal = 0
		X = Queue() # Queue yang digunakan untuk menyimpan absis (x)
		Y = Queue() # Queue yang digunakan untuk menyimpan ordinat (y)
		M = Queue() # Queue yang digunakan untuk menyimpan aksi (move)
		X.enqueue(Player_X(index)) # Insialisasi awal dengan absis bot saat ini
		Y.enqueue(Player_Y(index)) # Inisialisasi awal dengan ordinat bot saat ini
		M.enqueue([]) # Inisialisasi awal dengan list kosong
		# Melakukan iterasi selama queue absis tidak kosong dan belum menemukan jalan keluar
		while ((not X.isEmpty()) and (goal == 0)):
			i = X.dequeue()
			j = Y.dequeue()
			move = M.dequeue()
			valid = False # valid adalah penentu apakah jalan tersebut dapat dilalui atau tidak
			if ((Map_Entity(i,j) == None) or (Map_Entity(i,j) == player_key)): # Kosong (tidak ada halangan)
				if (Map_Bomb(i,j)): # Ada bom
					if ((Player_X(index) == i) and (Player_Y(index) == j)): # Posisi bom = posisi bot
						valid = True
				else: # Tidak ada bom
					valid = True
			count = DangerCounter(i,j)-len(move)
			# Menentukan apakah sempat melarikan diri dengan pergerakan tersebut
			if ((count == 0) or (count == 1)):
				valid = False
			if (valid):
				if (not InDanger(i,j)):
					goal = move[0]
				elif (len(move) < 10):
					temp = TargetPos(i,j,math.floor(MAP_WIDTH/2),1)
					if (temp == -1):
						temp = TargetPos(i,j,math.floor(MAP_WIDTH/2),2)
					x_target = GetTargetX(temp)
					y_target = GetTargetY(temp)
					dist = []
					dist.append(Distance(i,j-1,x_target,y_target)) # Memasukkan jarak antar tetangga atas (i, j) ke koordinat target
					dist.append(Distance(i-1,j,x_target,y_target)) # Memasukkan jarak antar tetangga kiri (i, j) ke koordinat target
					dist.append(Distance(i+1,j,x_target,y_target)) # Memasukkan jarak antar tetangga kanan (i, j) ke koordinat target
					dist.append(Distance(i,j+1,x_target,y_target)) # Memasukkan jarak antar tetangga bawah (i, j) ke koordinat target
					X.enqueue(i)
					Y.enqueue(j)
					M.enqueue(move + [-1])
					for q in range(0,4):
						shortest = 0
						for w in range(1,4):
							if (dist[w] < dist[shortest]):
								shortest = w
						if (shortest == 0):
							X.enqueue(i)
							Y.enqueue(j-1)
							M.enqueue(move + [1])
						elif (shortest == 1):
							X.enqueue(i-1)
							Y.enqueue(j)
							M.enqueue(move + [2])
						elif (shortest == 2):
							X.enqueue(i+1)
							Y.enqueue(j)
							M.enqueue(move + [3])
						elif (shortest == 3):
							X.enqueue(i)
							Y.enqueue(j+1)
							M.enqueue(move + [4])
						dist[shortest] = 100000		#big number
		if (goal == 0): # Tidak ada jalan keluar
			return (-1)
		else:
			return(goal)
	
	def TargetPos(x,y,radius,search):
	# Terdiri dari 2 jenis search
	# Search 1: Mengembalikan nilai yang mengandung koordinat target
	# Search 2: Mengembalikan nilai yang mengandung indeks musuh
		x_end = x + radius # Menjaga agar x tidak keluar batas
		if (x_end > MAP_WIDTH):
			x_end = MAP_WIDTH
		y_start = y - radius # Menjaga agar y tidak keluar batas
		if (y_start < 1):
			y_start = 1
		y_end = y + radius # Menjaga agar y tidak keluar batas
		if (y_end > MAP_HEIGHT):
			y_end = MAP_HEIGHT
		x_start = x - radius # Menjaga agar x tidak keluar batas
		if (x_start < 1):
			x_start = 1
		# Insialisasi awal
		found_x = -1
		found_y = -1
		# Melakukan pencarian
		for i in range(x_start, x_end):
			for j in range(y_start, y_end):
				# Search 1
				if (search == 1):
					if (Map_PowerUp(i, j) != None):
						if (found_x == -1):
							found_x = i
							found_y = j
						else:
							if (Distance(x,y,i,j) < Distance(x,y,found_x,found_y)):
								found_x = i
								found_y = j
				# Search 2
				elif (search == 2):
					player_index = Player_Index(player_key)
					enemy_index = IsEnemyInRange(player_index,radius)
					if ((enemy_index != player_index) and (not Player_Killed(enemy_index)) and (i == Player_X(enemy_index)) and (j == Player_Y(enemy_index)) and (Distance(x, y, i, j) <= radius)):
							if (found_x == -1):
								found_x = i
								found_y = j
							else:
								if (Distance(x,y,i,j) < Distance(x,y,found_x,found_y)):
									found_x = i
									found_y = j
		if (found_x == -1): # Tidak ketemu
			return -1
		else:
			if (search == 1): # Search 1
				return (found_x*(10**(math.floor(math.log(MAP_HEIGHT,10))+1))+found_y) # Return value adalah koordinat target (data dimanipulasi)
			elif (search == 2): # Search 2
				# return(enemy_index*(10**(2*math.floor(math.log(MAP_HEIGHT,10))+1))+found_x*(10**(math.floor(math.log(MAP_HEIGHT,10))+1))+found_y) # Return value adalah indeks musuh (data dimanipulasi)
				return(enemy_index)

	def GetEnemyIndex(val):
	# Mengekstrak indeks musuh dari manipulasi data yang telah dilakukan
		# return (math.floor(val/(10**(2*math.floor(math.log(MAP_HEIGHT,10))))))
		return(val)

	def GetTargetX(val):
	# Mengekstrak absis target dari manipulasi data yang telah dilakukan
		return (math.floor(val/(10**(math.floor(math.log(MAP_HEIGHT,10))+1))))
	
	def GetTargetY(val):
	# Mengekstrak ordinat target dari manipulasi data yang telah dilakukan
		return (val % (10**(math.floor(math.log(MAP_HEIGHT,10))+1)))
	
	def GoToTarget(x,y,radius,index,search):
	# Menghasilkan aksi yang harus dilakukan untuk bergerak mendekati target (Search 1) atau musuh (Search 2)
	# Menggunakan Greedy Best-First Search
		if (search == 1): # Search 1: mencari power up
			temp = TargetPos(x,y,radius,1) # Koordinat target berupa manipulasi data
			smin = 9999 # Insialisasi awal
			move = -1 # Inisialisasi awal
			# Perbandingan nilai heuristik (kuadrat pythagoras) dari keempat tetangganya
			if (Map_Entity(Player_X(index), Player_Y(index)-1) == None) and (not InDanger(Player_X(index), Player_Y(index)-1)):
				sup = PythagorasPow(GetTargetX(temp),GetTargetY(temp),x,y-1) # Atas
				if (smin > sup) :
					smin = sup
					move = 1
			if (Map_Entity(Player_X(index)-1, Player_Y(index)) == None) and (not InDanger(Player_X(index)-1, Player_Y(index))):
				sleft = PythagorasPow(GetTargetX(temp),GetTargetY(temp),x-1,y) # Kiri
				if (smin > sleft) :
					smin = sleft
					move = 2
			if (Map_Entity(Player_X(index)+1, Player_Y(index)) == None) and (not InDanger(Player_X(index)+1, Player_Y(index))):
				sright = PythagorasPow(GetTargetX(temp),GetTargetY(temp),x+1,y) # Kanan
				if (smin > sright) :
					smin = sright
					move = 3
			if (Map_Entity(Player_X(index), Player_Y(index)+1) == None) and (not InDanger(Player_X(index), Player_Y(index)+1)):
				sdown = PythagorasPow(GetTargetX(temp),GetTargetY(temp),x,y+1) # Bawah
				if (smin > sdown) :
					smin = sdown
					move = 4
			# Mengembalikan aksi terbaik yang didapatkan
			return move
		else: # Search 2 dan 3: mencari musuh
			if (search == 2): # Dalam radius tertentu
				temp = TargetPos(x,y,radius,2) # Indeks musuh berupa manipulasi data
				enemy_index = GetEnemyIndex(temp) # Inisialisasi dengan indeks musuh sesungguhnya
			else: # Mengincar musuh yang masih hidup di manapun mereka berada
				found = False
				searchingindex = 0
				while (searchingindex <= TOTAL_PLAYER-1) and (not found):
					if (Player_Key(searchingindex) != player_key) and (not Player_Killed(searchingindex)):
						found = True
						enemy_index = searchingindex
					else:
						searchingindex += 1
			# Apabila jarak musuh <= blast radius bom bot dan musuh masih hidup
			if ((not Player_Killed(enemy_index)) and Distance(Player_X(index),Player_Y(index),Player_X(enemy_index),Player_Y(enemy_index)),Player_BombRadius(index)):
				time_to_attack = False
				# Horizontal check
				if (Player_X(enemy_index) == Player_X(index)):
					# Left check
					x_left = x
					while ((x_left >= 0) and (Map_Entity(x_left, y) != WALL) and (Map_Entity(x_left, y) != OBSTACLE) and (not time_to_attack)):
						if (Map_Entity(x_left, y) == Player_Key(enemy_index)):
							time_to_attack = True
						else:
							x_left -= 1
					# Right check
					x_right = x + 1
					while ((x_right <= MAP_WIDTH) and (Map_Entity(x_right, y) != WALL) and (Map_Entity(x_right, y) != OBSTACLE) and (not time_to_attack)):
						if (Map_Entity(x_right, y) == Player_Key(enemy_index)):
							time_to_attack = True
						else:
							x_right += 1
				# Vertical check
				elif (Player_Y(enemy_index) == Player_Y(index)):
					# Up check
					y_up = y - 1
					while ((y_up >= 0) and (Map_Entity(x, y_up) != WALL) and (Map_Entity(x, y_up) != OBSTACLE) and (not time_to_attack)):
						if (Map_Entity(x, y_up) == Player_Key(enemy_index)):
							time_to_attack = True
						else:
							y_up -= 1
					# Down check
					y_down = y + 1
					while ((y_down <= MAP_HEIGHT) and (Map_Entity(x, y_down) != WALL) and (Map_Entity(x, y_down) != OBSTACLE) and (not time_to_attack)):
						if (Map_Entity(x, y_down) == Player_Key(enemy_index)):
							time_to_attack = True
						else:
							y_down += 1
			# Ada kemungkinan dapat meledakkan musuh
			if (time_to_attack):
				return(5)
			else: # not time_to_attack
				smin = 9999 # Inisialisasi awal
				move = -1 # Inisialisasi awal
				# Perbandingan nilai heuristik (kuadrat pythagoras) dari keempat tetangganya
				if (Map_Entity(Player_X(index), Player_Y(index)-1) == None) and (not InDanger(Player_X(index), Player_Y(index)-1)):
					sup = PythagorasPow(Player_X(enemy_index),Player_Y(enemy_index),Player_X(index),Player_Y(index)-1)
					if (smin > sup) :
						smin = sup
						move = 1
				if (Map_Entity(Player_X(index)-1, Player_Y(index)) == None) and (not InDanger(Player_X(index)-1, Player_Y(index))):
					sleft = PythagorasPow(Player_X(enemy_index),Player_Y(enemy_index),Player_X(index)-1,Player_Y(index))
					if (smin > sleft) :
						smin = sleft
						move = 2
				if (Map_Entity(Player_X(index)+1, Player_Y(index)) == None) and (not InDanger(Player_X(index)+1, Player_Y(index))):
					sright = PythagorasPow(Player_X(enemy_index),Player_Y(enemy_index),Player_X(index)+1,Player_Y(index))
					if (smin > sright) :
						smin = sright
						move = 3
				if (Map_Entity(Player_X(index), Player_Y(index)+1) == None) and (not InDanger(Player_X(index), Player_Y(index)+1)):
					sdown = PythagorasPow(Player_X(enemy_index),Player_Y(enemy_index),Player_X(index),Player_Y(index)+1)
					if (smin > sdown) :
						smin = sdown
						move = 4
			# Mengembalikan aksi terbaik yang didapatkan
			return move

	def Choice(index):
	# Menentukan aksi yang dilakukan dengan berdasarkan urutan prioritas bot
		# Prioritas pertama: kabur dari zona bahaya
		if (InDanger(Player_X(index), Player_Y(index))):
			return(SOS(index))
		# Prioritas kedua: memicu ledakan bom yang sudah bot letakkan apabila sudah tidak berada di zona bahaya
		elif (HasPlacedBomb()):
			return(6)
		# Prioritas ketiga: meledakkan obstacle yang ada di sebelah bot
		elif ((Map_Entity(Player_X(index)-1, Player_Y(index)) == OBSTACLE) or (Map_Entity(Player_X(index)+1, Player_Y(index)) == OBSTACLE) or (Map_Entity(Player_X(index), Player_Y(index)-1) == OBSTACLE) or (Map_Entity(Player_X(index), Player_Y(index)+1) == OBSTACLE)):
			return(5)
		# Prioritas keempat: mengejar power up sebagai target
		elif (IsPowerUpInRange(Player_X(index),Player_Y(index), math.floor(MAP_WIDTH/2))):
			return(GoToTarget(Player_X(index),Player_Y(index),MAP_WIDTH,index,1))
		# Prioritas kelima: mengejar musuh dalam radius tertentu
		elif (IsEnemyInRange(index,math.floor(MAP_WIDTH)) != -1):
			enemy_key = Player_Key(IsEnemyInRange(index,math.floor(MAP_WIDTH)))
			if (((Map_Entity(Player_X(index)-1, Player_Y(index)) == enemy_key) or (Map_Entity(Player_X(index)+1, Player_Y(index)) == enemy_key) or (Map_Entity(Player_X(index), Player_Y(index)-1) == enemy_key) or (Map_Entity(Player_X(index), Player_Y(index)+1) == enemy_key)) and SOS(Player_Index(enemy_key))):
				return(5)
			else:
				return(GoToTarget(Player_X(index),Player_Y(index),MAP_WIDTH,index,2))
		# Prioritas keenam: berdiam diri apabila telah terpojok
		elif ((InDanger(Player_X(index)-1,Player_Y(index))) and (InDanger(Player_X(index),Player_Y(index)+1)) and (InDanger(Player_X(index),Player_Y(index)-1)) and (InDanger(Player_X(index),Player_Y(index)+1))):
			return(-1)
		# Prioritas ketujuh: hunting down the enemy
		else:
			return(GoToTarget(Player_X(index),Player_Y(index),MAP_WIDTH,index,3))


	action = Choice(Player_Index(player_key))
	logger.info('Action: {}'.format(ACTIONS[action]))
	
	with open(os.path.join(output_path, 'move.txt'), 'w') as f:
		f.write('{}\n'.format(action))


if __name__ == '__main__':
	parser = argparse.ArgumentParser()
	parser.add_argument('player_key', nargs='?')
	parser.add_argument('output_path', nargs='?', default=os.getcwd())
	args = parser.parse_args()

	assert(os.path.isdir(args.output_path))

	main(args.player_key, args.output_path)