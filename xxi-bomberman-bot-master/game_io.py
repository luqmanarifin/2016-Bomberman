import json
from entities.bomb import Bomb
from entities.player import Player
from entities.power_up import PowerUp
from entities.wall import Wall
from entities.game_state import GameState
from entities.moves import Moves

class GameIOException(Exception):
    def __init__(self, message):
        self.message = message

def location_to_tuple(location):
    return (location['X'] - 1, location['Y'] - 1)

def read_state(file_path, player_key):
    with open(file_path, 'r') as state_file:
        js = json.load(state_file)

    game_state = GameState()

    for player in js['RegisteredPlayerEntities']:
        if not player['Killed']:
            game_state.players.append(Player(player['Name'], player['Key'], player['Points'], player['Killed'], player['BombBag'], player['BombRadius'], location_to_tuple(player['Location'])))

    game_state.current_player = filter(lambda player: player.key == player_key, game_state.players)[0]

    for _singleEntity in js['GameBlocks']:
        for singleEntity in _singleEntity:
            if singleEntity['Entity']:
                if singleEntity['Entity']['$type'] != 'Domain.Entities.PlayerEntity, Domain':
                    wall = singleEntity['Entity']
                    game_state.walls.append(Wall(wall['$type'], location_to_tuple(wall['Location'])))

            if singleEntity['Bomb']:
                bomb = singleEntity['Bomb']
                game_state.bombs.append(Bomb(bomb['Owner']["Key"], bomb['BombRadius'], bomb['BombTimer'], bomb['IsExploding'], location_to_tuple(bomb['Location'])))

            if singleEntity['PowerUp']:
                pUp = singleEntity['PowerUp']
                game_state.power_ups.append(PowerUp(pUp['$type'], location_to_tuple(pUp['Location'])))

    game_state.map = [['.' for j in range(js['MapWidth'])] for i in range(js['MapHeight'])]
    for wall in game_state.walls:
        if wall.destructible:
            game_state.set_map(wall.location, '+')
        else:
            game_state.set_map(wall.location, '#')

    return game_state

def write_move(file_path, move):
    if not (move in Moves.VALID_MOVES):
        raise GameIOException('Wrong movement code')

    with open(file_path, 'w') as move_file:
        move_file.write(str(move) + '\r\n')
