class GameState:
    def __init__(self):
        self.round = 0
        self.players = []
        self.current_player = {}
        self.walls = []
        self.bombs = []
        self.power_ups = []
        self.map = []

    def set_map(self, location, char):
    	x, y = location
        self.map[y][x] = char
