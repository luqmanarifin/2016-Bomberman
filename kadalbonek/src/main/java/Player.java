/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Luqman A. Siswanto
 */
public class Player {
  public String name;
  public String key;
  public int points;
  public boolean killed;
  public int bombBag;
  public int bombRadius;
  public int x;
  public int y;

  public Player() {
  }

  public Player(String name, String key, int points, boolean killed, int bombBag, int bombRadius, int x, int y) {
    this.name = name;
    this.key = key;
    this.points = points;
    this.killed = killed;
    this.bombBag = bombBag;
    this.bombRadius = bombRadius;
    this.x = x;
    this.y = y;
  }
}
