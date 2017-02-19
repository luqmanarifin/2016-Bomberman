/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.sun.corba.se.impl.encoding.CDROutputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    for (int i = 0; i < players.length; i++) {
      if (botKey.equals(players[i].key)) {
        me = players[i];
      }
    }
    generateVisit();
  }

  boolean passable(int i, int j) {
    return (0 <= s[i][j] && s[i][j] <= 30) || s[i][j] == Constant.LOWONG || s[i][j] == Constant.BOMB_RADIUS
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
            vis[i][j] = Integer.parseInt(line);
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
          bw.write("" + vis[i][j]);
          bw.write("\n");
        }
      }
      bw.close();
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
    for (int i = 1; i <= mapWidth; i++) {
      for (int j = 1; j <= mapHeight; j++) {
        vis[i][j] = 0;
      }
    }

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
    if (val == 5) {
      bombs[me.x][me.y] = new Bomb(me, me.bombRadius, Math.min(10, 3 * me.bombBag + 1), false, me.x, me.y);
      s[me.x][me.y] = Constant.BOM;
      if (distanceToSafePlace(me.x, me.y) >= bombs[me.x][me.y].bombTimer - 1) return true;
      s[me.x][me.y] = Constant.LOWONG;
    }
    if (val >= 5) val = 0;
    int ti = me.x + da[val];
    int tj = me.y + db[val];
    boolean[][] blast = getOtherBlasts();
    if (distanceToSafePlace(ti, tj) == Constant.INF) return true;
    for (int i = 1; i <= mapHeight; i++) {
      for (int j = 1; j <= mapWidth; j++) {
        if (s[i][j] == Constant.BOM && bombs[i][j].owner.key.equals(me.key)) {
          if (direct(i, j, ti, tj) && distanceToSafePlace(ti, tj) >= bombs[i][j].bombTimer - 1) return true;
        }
        if (s[i][j] == Constant.BOM && !bombs[i][j].owner.key.equals(me.key)) {
          if (direct(i, j, ti, tj) && blast[ti][tj] && bombs[i][j].bombTimer == 1) return true;
        }
      }
    }
    return false;
  }

  public int getMove() {
    if (answer.isEmpty()) solve();
    List<Integer> random = new ArrayList<Integer>();
    for (int i = 1; i <= 4; i++) random.add(i);
    Collections.shuffle(random);
    for (int i = 0; i < random.size(); i++) {
      int ti = me.x + da[random.get(i)];
      int tj = me.y + db[random.get(i)];
      if (!answer.contains(random.get(i)) && valid(ti, tj) && passable(ti, tj)) {
        answer.add(random.get(i));
      }
    }
    System.out.println("moves");
    for (int i = 0; i < answer.size(); i++) System.out.print(answer.get(i) + " ");
    System.out.println();
    for (int i = 0; i < answer.size(); i++) {
      if (!dangerousMove(answer.get(i))) {
        return answer.get(i);
      }
      System.out.println(answer.get(i) + " dangerous move!");
    }
    return 0;
  }
  
  private void solve() {
    System.out.println("POSISI NOW " + me.x + " " + me.y);
    if (inDanger()) {
      System.out.println("danger");
      runFromDanger();
    } else if (possibleAttack()) {
      System.out.println("attack");
      attack();
    } else if (powerUpNearby()) {
      System.out.println("powerup");
      takePowerUp();
    } else if (moreBricks()) {
      System.out.println("bricks");
      destroyBrick();
    } else {
      System.out.println("yolo");
      yoloMode();
    }
  }

  int[][] bfs(int start_a, int start_b) {
    int[][] num = null;
    return bfs(start_a, start_b, num);
  }

  // return distance from a node to all other nodes
  int[][] bfs(int start_a, int start_b, int[][] num) {
    int[] p = new int[mapHeight * mapWidth * 5];
    int[] q = new int[mapHeight * mapWidth * 5];
    int[][] dist = new int[mapWidth + 5][mapHeight + 5];
    num = new int[mapWidth + 5][mapHeight + 5];
    for (int i = 1; i <= mapWidth; i++) {
      for (int j = 1; j <= mapHeight; j++) {
        dist[i][j] = Constant.INF;
      }
    }
    dist[start_a][start_b] = 0;
    int b = 0, e = 1;
    p[0] = start_a; q[0] = start_b;
    while (b < e) {
      int pp = p[b];
      int qq = q[b];
      b++;
      for (int i = 1; i <= 4; i++) {
        int ti = pp + da[i];
        int tj = qq + db[i];
        if (valid(ti, tj) && passable(ti, tj)) {
          int cur = num[pp][qq] + 1 - vis[ti][tj];
          if (dist[pp][qq] + 1 < dist[ti][tj]) {
            dist[ti][tj] = dist[pp][qq] + 1;
            num[ti][tj] = cur;
            p[e] = ti;
            q[e] = tj;
            e++;
          }
          if (dist[pp][qq] + 1 == dist[ti][tj] && cur > num[ti][tj]) {
            dist[ti][tj] = dist[pp][qq] + 1;
            num[ti][tj] = cur;
            p[e] = ti;
            q[e] = tj;
            e++;
          }
        }
      }
    }
    return dist;
  }

  // return possible direction for routing
  int[] route(int from_a, int from_b, int to_a, int to_b) {
    int[][] num = new int[mapWidth + 5][mapHeight + 5];
    int[][] dist = bfs(to_a, to_b, num);
    List<Integer> ret = new ArrayList<Integer>();
    for (int i = 1; i <= 4; i++) {
      int ti = from_a + da[i];
      int tj = from_b + db[i];
      if (valid(ti, tj) && passable(ti, tj) && dist[from_a][from_b] == dist[ti][tj] + 1) {
        ret.add(i);
      }
    }
    Collections.shuffle(ret);
    for (int i = 0; i < ret.size(); i++) {
      for (int j = 0; j + 1 < ret.size(); j++) {
        int ti = from_a + da[ret.get(j)];
        int tj = from_b + db[ret.get(j)];

        int tii = from_a + da[ret.get(j + 1)];
        int tjj = from_b + db[ret.get(j + 1)];
        if (num[ti][tj] < num[tii][tjj]) {
          int tmp = ret.get(j);
          ret.set(j, ret.get(j + 1));
          ret.set(j + 1, tmp);
        }
      }
    }
    int[] prim = new int[ret.size()];
    for (int i = 0; i < ret.size(); i++) prim[i] = ret.get(i);
    return prim;
  }

  boolean[][] getFakeBlasts(int i, int j) {
    boolean[][] blast = new boolean[mapWidth + 5][mapHeight + 5];
    for (int k = 1; k <= me.bombRadius; k++) {
      if (1 <= j + k && j + k <= mapHeight) {
        if (!bombPassable(i, j + k)) break;
        blast[i][j + k] = true;
      }
    }
    for (int k = 1; k <= me.bombRadius; k++) {
      if (1 <= i + k && i + k <= mapWidth) {
        if (!bombPassable(i + k, j)) break;
        blast[i + k][j] = true;
      }
    }
    for (int k = -1; k >= -me.bombRadius; k--) {
      if (1 <= j + k && j + k <= mapHeight) {
        if (!bombPassable(i, j + k)) break;
        blast[i][j + k] = true;
      }
    }
    for (int k = -1; k >= -me.bombRadius; k--) {
      if (1 <= i + k && i + k <= mapWidth) {
        if (!bombPassable(i + k, j)) break;
        blast[i + k][j] = true;
      }
    }
    return blast;
  }

  boolean[][] getMyBlasts() {
    boolean[][] blast = new boolean[mapWidth + 5][mapHeight + 5];
    for (int i = 1; i <= mapWidth; i++) {
      for (int j = 1; j <= mapHeight; j++) {
        if (s[i][j] == Constant.BOM && bombs[i][j].owner.key.equals(botKey)) {
          for (int k = 1; k <= bombs[i][j].bombRadius; k++) {
            if (1 <= j + k && j + k <= mapHeight) {
              if (!bombPassable(i, j + k)) break;
              blast[i][j + k] = true;
            }
          }
          for (int k = 1; k <= bombs[i][j].bombRadius; k++) {
            if (1 <= i + k && i + k <= mapWidth) {
              if (!bombPassable(i + k, j)) break;
              blast[i + k][j] = true;
            }
          }
          for (int k = -1; k >= -bombs[i][j].bombRadius; k--) {
            if (1 <= j + k && j + k <= mapHeight) {
              if (!bombPassable(i, j + k)) break;
              blast[i][j + k] = true;
            }
          }
          for (int k = -1; k >= -bombs[i][j].bombRadius; k--) {
            if (1 <= i + k && i + k <= mapWidth) {
              if (!bombPassable(i + k, j)) break;
              blast[i + k][j] = true;
            }
          }
        }
      }
    }
    return blast;
  }

  boolean[][] getOtherBlasts() {
    boolean[][] blast = new boolean[mapWidth + 5][mapHeight + 5];
    for (int i = 1; i <= mapWidth; i++) {
      for (int j = 1; j <= mapHeight; j++) {
        if (s[i][j] == Constant.BOM && !bombs[i][j].owner.key.equals(me.key)) {
          for (int k = 1; k <= bombs[i][j].bombRadius; k++) {
            if (1 <= j + k && j + k <= mapHeight) {
              if (!bombPassable(i, j + k)) break;
              blast[i][j + k] = true;
            }
          }
          for (int k = 1; k <= bombs[i][j].bombRadius; k++) {
            if (1 <= i + k && i + k <= mapWidth) {
              if (!bombPassable(i + k, j)) break;
              blast[i + k][j] = true;
            }
          }
          for (int k = -1; k >= -bombs[i][j].bombRadius; k--) {
            if (1 <= j + k && j + k <= mapHeight) {
              if (!bombPassable(i, j + k)) break;
              blast[i][j + k] = true;
            }
          }
          for (int k = -1; k >= -bombs[i][j].bombRadius; k--) {
            if (1 <= i + k && i + k <= mapWidth) {
              if (!bombPassable(i + k, j)) break;
              blast[i + k][j] = true;
            }
          }
        }
      }
    }
    return blast;
  }

  boolean[][] getBlasts() {
    boolean[][] blast = new boolean[mapWidth + 5][mapHeight + 5];
    for (int i = 1; i <= mapWidth; i++) {
      for (int j = 1; j <= mapHeight; j++) {
        if (s[i][j] == Constant.BOM) {
          for (int k = 1; k <= bombs[i][j].bombRadius; k++) {
            if (1 <= j + k && j + k <= mapHeight) {
              if (!bombPassable(i, j + k)) break;
              blast[i][j + k] = true;
            }
          }
          for (int k = 1; k <= bombs[i][j].bombRadius; k++) {
            if (1 <= i + k && i + k <= mapWidth) {
              if (!bombPassable(i + k, j)) break;
              blast[i + k][j] = true;
            }
          }
          for (int k = -1; k >= -bombs[i][j].bombRadius; k--) {
            if (1 <= j + k && j + k <= mapHeight) {
              if (!bombPassable(i, j + k)) break;
              blast[i][j + k] = true;
            }
          }
          for (int k = -1; k >= -bombs[i][j].bombRadius; k--) {
            if (1 <= i + k && i + k <= mapWidth) {
              if (!bombPassable(i + k, j)) break;
              blast[i + k][j] = true;
            }
          }
        }
      }
    }
    return blast;
  }

  // get safe places
  boolean[][] getSafePlaces() {
    boolean[][] safe = getBlasts();
    for (int i = 1; i <= mapWidth; i++) {
      for (int j = 1; j <= mapHeight; j++) {
        safe[i][j] = !safe[i][j];
        if (!passable(i, j)) {
          safe[i][j] = false;
        }
      }
    }
    return safe;
  }

  boolean valid(int i, int j) {
    return 1 <= i && i <= mapWidth && 1 <= j && j <= mapHeight;
  }

  // return minimum distance to safe place
  int distanceToSafePlace(int a, int b) {
    int[][] dist = bfs(a, b);
    boolean[][] safe = getSafePlaces();
    int ret = Constant.INF;
    for (int i = 1; i <= mapWidth; i++) {
      for (int j = 1; j <= mapHeight; j++) {
        if (safe[i][j]) {
          ret = Math.min(ret, dist[i][j]);
        }
      }
    }
    return ret;
  }

  // return minimum distance between two place
  int minimumDistance(int from_a, int from_b, int to_a, int to_b) {
    int[][] dist = bfs(from_a, from_b);
    return dist[to_a][to_b];
  }

  // bomb illuminate some point
  private boolean direct(int bomb_a, int bomb_b, int pos_a, int pos_b) {
    int i = bomb_a;
    int j = bomb_b;
    if (i == pos_a) {
      int l = Math.min(j, pos_b);
      int r = Math.max(j, pos_b);
      boolean allPassable = true;
      for (int k = l + 1; k < r; k++) {
        if (!bombPassable(i, k)) {
          allPassable = false;
        }
      }
      if (allPassable && r - l <= bombs[i][j].bombRadius) {
        return true;
      }
    } else if (j == pos_b) {
      int l = Math.min(i, pos_a);
      int r = Math.max(i, pos_a);
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
    return false;
  }

  private boolean inDanger() {
    for (int i = 1; i <= mapWidth; i++) {
      for (int j = 1; j <= mapHeight; j++) {
        if (s[i][j] == Constant.BOM) {
          // bomb milik musuh
          if (bombs[i][j].owner.key != me.key) {
            if (direct(i, j, me.x, me.y)) {
              return true;
            }
          } else {    // bomb punya sendiri
            if (direct(i, j, me.x, me.y) && distanceToSafePlace(me.x, me.y) >= bombs[i][j].bombTimer - 1) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private void runFromDanger() {
    boolean[][] safe = getSafePlaces();
    int[][] dist = bfs(me.x, me.y);
    int distanceToSafe = distanceToSafePlace(me.x, me.y);

    for (int i = 1; i <= mapWidth; i++) {
      for (int j = 1; j <= mapHeight; j++) {
        if (safe[i][j] && dist[i][j] == distanceToSafe) {
          int[] candidate = route(me.x, me.y, i, j);
          for (int k = 0; k < candidate.length; k++) {
            if (!answer.contains(candidate[k])) {
              answer.add(candidate[k]);
            }
          }
        }
      }
    }
  }

  private boolean possibleAttack() {
    boolean[][] blast = getMyBlasts();
    for (int k = 0; k < players.length; k++) {
      if (players[k].killed || players[k].key.equals(me.key)) continue;
      int[][] dist = bfs(players[k].x, players[k].y);
      for (int i = 1; i <= mapWidth; i++) {
        for (int j = 1; j <= mapHeight; j++) {
          if (blast[i][j] && dist[i][j] <= 2) {
            return true;
          }
        }
      }
    }
    boolean[][] fakeBlast = getFakeBlasts(me.x, me.y);
    for (int k = 0; k < players.length; k++) {
      if (players[k].killed || players[k].key.equals(me.key)) continue;
      if (fakeBlast[players[k].x][players[k].y]) {
        return true;
      }
    }
    return false;
  }

  private void attack() {
    boolean[][] blast = getMyBlasts();
    for (int k = 0; k < players.length; k++) {
      if (players[k].killed || players[k].key.equals(me.key)) continue;
      int[][] dist = bfs(players[k].x, players[k].y);
      for (int i = 1; i <= mapWidth; i++) {
        for (int j = 1; j <= mapHeight; j++) {
          if (blast[i][j] && dist[i][j] <= 2) {
            if (!answer.contains(6)) {
              answer.add(6);
            }
          }
        }
      }
    }
    boolean[][] fakeBlast = getFakeBlasts(me.x, me.y);
    for (int k = 0; k < players.length; k++) {
      if (players[k].killed || players[k].key.equals(me.key)) continue;
      if (fakeBlast[players[k].x][players[k].y] && me.bombBag > 0) {
        if (!answer.contains(5)) {
          answer.add(5);
        }
      }
    }
  }

  private boolean powerUpNearby() {
    for (int i = 1; i <= mapWidth; i++) {
      for (int j = 1; j <= mapHeight; j++) {
        if (s[i][j] == Constant.BOMB_RADIUS && me.bombRadius > mapHeight && me.bombRadius> mapWidth) continue;
        if (s[i][j] == Constant.BOMB_RADIUS || s[i][j] == Constant.BOMB_BAG || s[i][j] == Constant.SUPER_POWER_UP) {
          int[][] dist = bfs(i, j);
          boolean best = true;
          for (int k = 0; k < players.length; k++) {
            if (players[k].killed || players[k].key.equals(me.key)) continue;
            if (players[k].key.equals(me.key)) continue;
            if (dist[players[k].x][players[k].y] <= dist[me.x][me.y]) {
              best = false;
              break;
            }
          }
          if (best) return true;
        }
      }
    }
    return false;
  }

  private void takePowerUp() {
    for (int i = 1; i <= mapWidth; i++) {
      for (int j = 1; j <= mapHeight; j++) {
        if (s[i][j] == Constant.BOMB_RADIUS && me.bombRadius > mapHeight && me.bombRadius> mapWidth) continue;
        if (s[i][j] == Constant.BOMB_RADIUS || s[i][j] == Constant.BOMB_BAG || s[i][j] == Constant.SUPER_POWER_UP) {
          int[][] dist = bfs(i, j);
          boolean best = true;
          for (int k = 0; k < players.length; k++) {
            if (players[k].killed || players[k].key.equals(me.key)) continue;
            if (dist[players[k].x][players[k].y] <= dist[me.x][me.y]) {
              best = false;
              break;
            }
          }
          if (best) {
            int[] candidates = route(me.x, me.y, i, j);
            for (int k = 0; k < candidates.length; k++) {
              if (!answer.contains(candidates[k])) {
                answer.add(candidates[k]);
              }
            }
          }
        }
      }
    }
  }

  private boolean moreBricks() {
    for (int i = 1; i <= mapHeight; i++) {
      for (int j = 1; j <= mapWidth; j++) {
        if (s[i][j] == Constant.BATA) {
          return true;
        }
      }
    }
    return false;
  }

  int getBricks(int i, int j) {
    int cnt = 0;
    for (int k = 1; k <= me.bombRadius; k++) {
      if (1 <= j + k && j + k <= mapHeight) {
        if (!bombPassable(i, j + k)) {
          if (s[i][j + k] == Constant.BATA) cnt++;
          break;
        }
      }
    }
    for (int k = 1; k <= me.bombRadius; k++) {
      if (1 <= i + k && i + k <= mapWidth) {
        if (!bombPassable(i + k, j)) {
          if (s[i + k][j] == Constant.BATA) cnt++;
          break;
        }
      }
    }
    for (int k = -1; k >= -me.bombRadius; k--) {
      if (1 <= j + k && j + k <= mapHeight) {
        if (!bombPassable(i, j + k)) {
          if (s[i][j + k] == Constant.BATA) cnt++;
          break;
        }
      }
    }
    for (int k = -1; k >= -me.bombRadius; k--) {
      if (1 <= i + k && i + k <= mapWidth) {
        if (!bombPassable(i + k, j)) {
          if (s[i + k][j] == Constant.BATA) cnt++;
          break;
        }
      }
    }
    return cnt;
  }

  private int myWorstBombTimer() {
    int ret = 0;
    for (int i = 1; i <= mapWidth; i++) {
      for (int j = 1; j <= mapHeight; j++) {
        if (s[i][j] == Constant.BOM && bombs[i][j].owner.key.equals(me.key)) {
          ret = Math.max(ret, bombs[i][j].bombTimer);
        }
      }
    }
    return ret;
  }

  private void destroyBrick() {
    int[][] dist = bfs(me.x, me.y);
    double[][] val = new double[mapHeight + 5][mapWidth + 5];
    for (int i = 1; i <= mapWidth; i++) {
      for (int j = 1; j <= mapHeight; j++) {
        val[i][j] = Constant.INF;
      }
    }
    System.out.println("destroy");
    for (int i = 0; i < answer.size(); i++) System.out.print(answer.get(i) + " ");
    System.out.println();

    double best = Constant.INF;
    int p = -1, q = -1;
    for (int i = 1; i <= mapWidth; i++) {
      for (int j = 1; j <= mapHeight; j++) {
        if (s[i][j] == Constant.BOM && bombs[i][j].owner.key.equals(me.key)) continue;
        if (passable(i, j)) {
          int bricks = getBricks(i, j);
          if (bricks > 0) {
            val[i][j] = (double) dist[i][j] / bricks;
            if (val[i][j] < best) {
              best = val[i][j];
              p = i;
              q = j;
            }
          }
        }
      }
    }

    int distanceToSafe = distanceToSafePlace(me.x, me.y);
    int worstBombTimer = myWorstBombTimer();
    if (distanceToSafe <= 2 && worstBombTimer > 1) {
      List<Integer> a = new ArrayList<Integer>();
      List<Integer> b = new ArrayList<Integer>();
      List<Integer> c = new ArrayList<Integer>();
      for (int i = 1; i <= mapWidth; i++) {
        for (int j = 1; j <= mapHeight; j++) {
          if (s[i][j] == Constant.BOM && bombs[i][j].owner.key.equals(me.key)) {
            a.add(i);
            b.add(j);
            c.add(s[i][j]);
            s[i][j] = Constant.LOWONG;
            for (int k = 1; k <= 4; k++) {
              for (int l = 1; l <= me.bombRadius; l++) {
                int ti = i + l * da[k];
                int tj = j + l * db[k];
                if (!passable(ti, tj)) {
                  if (s[ti][tj] == Constant.BATA) {
                    a.add(ti);
                    b.add(tj);
                    c.add(s[ti][tj]);
                  }
                  break;
                }
              }
            }
          }
        }
      }
      dist = bfs(me.x, me.y);
      val = new double[mapHeight + 5][mapWidth + 5];
      for (int i = 1; i <= mapWidth; i++) {
        for (int j = 1; j <= mapHeight; j++) {
          val[i][j] = Constant.INF;
        }
      }
      for (int i = 1; i <= mapWidth; i++) {
        for (int j = 1; j <= mapHeight; j++) {
          if (s[i][j] == Constant.BOM && bombs[i][j].owner.key.equals(me.key)) continue;
          if (passable(i, j)) {
            int bricks = getBricks(i, j);
            if (bricks > 0) {
              val[i][j] = (double) (dist[i][j] + 2) / bricks;
              if (val[i][j] < best) {
                best = val[i][j];
                p = 0;
                q = 0;
              }
            }
          }
        }
      }
      for (int i = 0; i < a.size(); i++) {
        s[a.get(i)][b.get(i)] = c.get(i);
      }
    }

    System.out.println("pq " + p + " " + q + " " + best);
    if (p == 0) {
      answer.add(6);
    } else if (p == me.x && q == me.y) {
      if (me.bombBag > 0) {
        answer.add(5);
      }
    } else if (p != -1) {
      int[] candidates = route(me.x, me.y, p, q);
      for (int i = 0; i < candidates.length; i++) {
        System.out.println("cok " + candidates[i]);
        if (!answer.contains(candidates[i])) {
          answer.add(candidates[i]);
        }
      }
    }
  }

  private void yoloMode() {
    int best = Constant.INF;
    int p = -1;
    for (int k = 0; k < players.length; k++) {
      if (players[k].killed || players[k].key.equals(me.key)) continue;
      int cur = minimumDistance(me.x, me.y, players[k].x, players[k].y);
      if (cur < best) {
        best = cur;
        p = k;
      }
    }
    int[] candidates = route(me.x, me.y, players[p].x, players[p].y);
    for (int i = 0; i < candidates.length; i++) {
      if (!answer.contains(candidates[i])) {
        answer.add(candidates[i]);
      }
    }
  }
}
