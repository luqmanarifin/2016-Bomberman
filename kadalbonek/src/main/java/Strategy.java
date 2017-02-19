/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author Luqman A. Siswanto
 */
public class Strategy {
  private ArrayList<Integer> answer;

  private String botKey;
  private Player me;
  private Player[] players;
  private Bomb[][] bombs;
  private int[][] s;
  private int[][] vis;
  
  private int currentRound;
  private int playerBounty;
  private int mapHeight;
  private int mapWidth;

  private int[] da = {0, 0, -1, 1, 0};
  private int[] db = {0, -1, 0, 0, 1};
  
  public Strategy(String botKey, String jsonString) {
    this.botKey = botKey;
    answer = new ArrayList<Integer>();
    JSONObject jsonObject = null;
    try {
      jsonObject = (JSONObject) new JSONParser().parse(jsonString);
      parse(jsonObject);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    generateVisit();
    for (int i = 0; i < players.length; i++) {
      if (botKey.equals(players[i].key)) {
        me = players[i];
      }
    }
  }

  boolean passable(int i, int j) {
    return s[i][j] == Constant.LOWONG || s[i][j] == Constant.BOMB_RADIUS
            || s[i][j] == Constant.BOMB_BAG || s[i][j] == Constant.SUPER_POWER_UP;
  }

  boolean bombPassable(int i, int j) {
    return (0 <= s[i][j] && s[i][j] <= 30) || s[i][j] == Constant.BOM || s[i][j] == Constant.LOWONG
            || s[i][j] == Constant.BOMB_RADIUS || s[i][j] == Constant.BOMB_BAG || s[i][j] == Constant.SUPER_POWER_UP;
  }

  private void generateVisit() {
    String fileName = "temp.in";
    if (currentRound == 0) {
      for (int i = 1; i <= mapWidth; i++) {
        for (int j = 1; j <= mapHeight; j++) {
          if (passable(i, j)) {
            vis[i][j] = 1;
          } else {
            vis[i][j] = 0;
          }
        }
      }
    } else {
      // baca dari file

      // FileReader reads text files in the default encoding.
      FileReader fileReader = null;
      try {
        fileReader = new FileReader(fileName);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }

      // Always wrap FileReader in BufferedReader.
      BufferedReader bufferedReader = new BufferedReader(fileReader);

      try {
        String line;
        for (int i = 1; i <= mapWidth; i++) {
          for (int j = 1; j <= mapHeight; j++) {
            line = bufferedReader.readLine();
            s[i][j] = Integer.parseInt(line);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      // Always close files.
      try {
        bufferedReader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    vis[me.x][me.y] = 1;

    // tulis ke file
    FileWriter fw = null;
    try {
      fw = new FileWriter(fileName);
      BufferedWriter bw = new BufferedWriter(fw);
      for (int i = 1; i <= mapWidth; i++) {
        for (int j = 1; j <= mapHeight; j++) {
          bw.write(s[i][j]);
          bw.write("\n");
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Player parsePlayer(JSONObject jsonPlayer) {
    String name = (String) jsonPlayer.get("Name");
    String key = (String) jsonPlayer.get("Key");
    int points = (int) (long) (Long) jsonPlayer.get("Points");
    boolean killed = (Boolean) jsonPlayer.get("Killed");
    int bombBag = (int) (long) (Long) jsonPlayer.get("BombBag");
    int bombRadius = (int) (long) (Long) jsonPlayer.get("BombRadius");
    JSONObject location = (JSONObject) jsonPlayer.get("Location");
    int x = (int) (long) (Long) location.get("X");
    int y = (int) (long) (Long) location.get("Y");

    return new Player(name, key, points, killed, bombBag, bombRadius, x, y);
  }

  private void parse(JSONObject jsonObject) throws ParseException {
    JSONArray jsonPlayers = (JSONArray) jsonObject.get("RegisteredPlayerEntities");
    players = new Player[jsonPlayers.size()];

    for (int i = 0; i < jsonPlayers.size(); i++) {
      JSONObject jsonPlayer = (JSONObject) jsonPlayers.get(i);
      players[i] = parsePlayer(jsonPlayer);
    }

    currentRound = (int) (long) (Long) jsonObject.get("CurrentRound");
    playerBounty = (int) (long) (Long) jsonObject.get("PlayerBounty");
    mapHeight = (int) (long) (Long) jsonObject.get("MapHeight");
    mapWidth = (int) (long) (Long) jsonObject.get("MapWidth");

    bombs = new Bomb[mapWidth + 5][mapHeight + 5];
    s = new int[mapWidth + 5][mapHeight + 5];
    vis = new int[mapWidth + 5][mapHeight + 5];

    JSONArray gameBlocks = (JSONArray) jsonObject.get("GameBlocks");
    for (int i = 0; i < gameBlocks.size(); i++) {
      JSONArray column = (JSONArray) gameBlocks.get(i);
      for (int j = 0; j < column.size(); j++) {
        JSONObject gameBlock = (JSONObject) column.get(j);
        JSONObject entity = (JSONObject) gameBlock.get("Entity");
        String nameEntity = (entity != null? (String) entity.get("$type") : "");
        JSONObject bomb = (JSONObject) gameBlock.get("Bomb");
        JSONObject powerUp = (JSONObject) gameBlock.get("PowerUp");
        boolean exploding = (Boolean) gameBlock.get("Exploding");
        JSONObject location = (JSONObject) gameBlock.get("Location");
        int x = (int) (long) (Long) location.get("X");
        int y = (int) (long) (Long) location.get("Y");

        if (bomb != null && !exploding) {
          Player owner = parsePlayer((JSONObject) bomb.get("Owner"));
          int bombRadius = (int) (long) (Long) bomb.get("BombRadius");
          int bombTimer = (int) (long) (Long) bomb.get("BombTimer");
          bombs[x][y] = new Bomb(owner, bombRadius, bombTimer, exploding, x, y);
          s[x][y] = Constant.BOM;
        } else if (nameEntity.equals("Domain.Entities.IndestructibleWallEntity, Domain")) {
          s[x][y] = Constant.TEMBOK;
        } else if (nameEntity.equals("Domain.Entities.DestructibleWallEntity, Domain")) {
          s[x][y] = Constant.BATA;
        } else if (powerUp != null) {
          String typePowerUp = (String) powerUp.get("$type");
          if (typePowerUp.equals("Domain.Entities.PowerUps.SuperPowerUp, Domain")) {
            s[x][y] = Constant.SUPER_POWER_UP;
          } else if (typePowerUp.equals("Domain.Entities.PowerUps.BombBag, Domain")) {
            s[x][y] = Constant.BOMB_BAG;
          } else {
            s[x][y] = Constant.BOMB_RADIUS;
          }
        } else if (entity == null) {
          s[x][y] = Constant.LOWONG;
        } else {
          String key = (String) entity.get("Key");
          for (int k = 0; k < players.length; k++) {
            if (key.equals(players[k].key)) {
              s[x][y] = k;
            }
          }
        }
      }
    }
  }

  public boolean dangerousMove(int val) {

  }

  public int getMove() {
    if (answer.isEmpty()) solve();
    for (int i = 0; i < answer.size(); i++) {
      if (!dangerousMove(answer.get(i))) {
        return answer.get(i);
      }
    }
    return 0;
  }
  
  private void solve() {
    if (inDanger()) {
      runFromDanger();
    } else if (possibleAttack()) {
      attack();
    } else if (powerUpNearby()) {
      takePowerUp();
    } else if (moreBricks()) {
      destroyBrick();
    } else {
      yoloMode();
    }
  }

  // return possible direction for routing
  int[] route(int from_a, int from_b, int to_a, int to_b) {

  }

  // return minimum distance to safe place
  int distanceToSafePlace() {

  }

  // return minimum distance between two place
  int minimumDistance(int from_a, int from_b, int to_a, int to_b) {

  }

  private boolean inDanger() {
    for (int i = 1; i <= mapWidth; i++) {
      for (int j = 1; j <= mapHeight; j++) {
        if (s[i][j] == Constant.BOM) {
          // bomb milik musuh
          if (bombs[i][j].owner.key != me.key) {
            if (i == me.x) {
              int l = Math.min(j, me.y);
              int r = Math.max(j, me.y);
              boolean allPassable = true;
              for (int k = l + 1; k < r; k++) {
                if (!bombPassable(i, k)) {
                  allPassable = false;
                }
              }
              if (allPassable && r - l <= bombs[i][j].bombRadius) {
                return true;
              }
            } else if (j == me.y) {
              int l = Math.min(i, me.x);
              int r = Math.max(i, me.x);
              boolean allPassable = true;
              for (int k = l + 1; k < r; k++) {
                if (!bombPassable(k, j)) {
                  allPassable = false;
                }
              }
              if (allPassable && r - l <= bombs[i][j].bombRadius) {
                return true;
              }
            }
          } else {    // bomb punya sendiri

          }
        }
      }
    }
    return false;
  }

  private void runFromDanger() {

  }

  private boolean possibleAttack() {

  }

  private void attack() {

  }

  private boolean powerUpNearby() {

  }

  private void takePowerUp() {

  }

  private boolean moreBricks() {

  }

  private void destroyBrick() {

  }

  private void yoloMode() {

  }
}
