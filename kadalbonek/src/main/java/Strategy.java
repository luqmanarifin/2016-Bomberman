/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Luqman A. Siswanto
 */
public class Strategy {
  private int answer;
  
  private Player[] players;
  private Bomb[][] bombs;
  private int[][] s;
  
  public Strategy(String jsonString) {
    answer = -1;
  }
  
  public int getMove() {
    if (answer == -1) solve();
    return answer;
  }
  
  private void solve() {
    
  }
}
