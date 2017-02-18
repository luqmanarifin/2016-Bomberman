/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Luqman A. Siswanto
 */
public class Bomb {
  public Player owner;
  public int bombRadius;
  public int bombTimer;
  public boolean isExploding;
  public int x;
  public int y;
  
  public Bomb(Player owner, int bombRadius, int bombTimer, boolean isExploding, int x, int y) {
    this.owner = owner;
    this.bombRadius = bombRadius;
    this.bombTimer = bombTimer;
    this.isExploding = isExploding;
    this.x = x;
    this.y = y;
  }
}
