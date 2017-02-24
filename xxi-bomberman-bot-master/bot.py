import sys
from os import path
import game_io
from greedy_strategy.strategy import GreedyStrategy

if __name__ == '__main__':
    if(len(sys.argv) < 2):
        player_key = ''
    else:
        player_key = sys.argv[1]

    if(len(sys.argv) < 3):
        output_path = ''
    else:
        output_path = sys.argv[2]

    if (output_path != '' and path.exists(output_path) == False):
       print
       print ('Error: Output folder "' + sys.argv[1] + '" does not exist.')
       exit(-1)

    # Read game state from state file
    state_file_path = path.join(output_path, '../state.json')
    game_state = game_io.read_state(state_file_path, player_key)

    # Specify strategy and calculate next move
    strategy = GreedyStrategy()
    next_move = strategy.calculate_next_move(game_state)

    # Write next move to move file
    move_file_path = path.join(output_path, 'move.txt')
    game_io.write_move(move_file_path, next_move)
