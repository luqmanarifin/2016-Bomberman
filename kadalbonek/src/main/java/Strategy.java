/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Luqman A. Siswanto
 */
public class Strategy {
  private int answer;

  private String botKey;
  private Player me;
  private Player[] players;
  private Bomb[][] bombs;
  private int[][] s;
  private boolean[][] vis;
  
  private int currentRound;
  private int playerBounty;
  private int mapHeight;
  private int mapWidth;
  
  public Strategy(String botKey, String jsonString) {
    this.botKey = botKey;
    answer = -1;
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
  }

  private Player parsePlayer(JSONObject jsonPlayer) {
    String name = (String) jsonPlayer.get("Name");
    String key = (String) jsonPlayer.get("Key");
    int points = Integer.parseInt((String) jsonPlayer.get("Points"));
    boolean killed = Boolean.parseBoolean((String) jsonPlayer.get("Killed"));
    int bombBag = Integer.parseInt((String) jsonPlayer.get("BombBag"));
    int bombRadius = Integer.parseInt((String) jsonPlayer.get("BombRadius"));
    JSONObject location = (JSONObject) jsonPlayer.get("Location");
    int x = Integer.parseInt((String) location.get("X"));
    int y = Integer.parseInt((String) location.get("Y"));

    return new Player(name, key, points, killed, bombBag, bombRadius, x, y);
  }

  private void parse(JSONObject jsonObject) throws ParseException {
    JSONArray jsonPlayers = (JSONArray) jsonObject.get("RegisteredPlayerEntities");
    players = new Player[jsonPlayers.size()];

    for (int i = 0; i < jsonPlayers.size(); i++) {
      JSONObject jsonPlayer = (JSONObject) jsonPlayers.get(i);
      players[i] = parsePlayer(jsonPlayer);
    }

    currentRound = Integer.parseInt((String) jsonObject.get("CurrentRound"));
    playerBounty = Integer.parseInt((String) jsonObject.get("PlayerBounty"));
    mapHeight = Integer.parseInt((String) jsonObject.get("MapHeight"));
    mapWidth = Integer.parseInt((String) jsonObject.get("MapWidth"));

    JSONArray gameBlocks = (JSONArray) jsonObject.get("GameBlocks");
    for (int i = 0; i < gameBlocks.size(); i++) {
      JSONArray column = (JSONArray) gameBlocks.get(i);
      for (int j = 0; j < column.size(); j++) {
        JSONObject gameBlock = (JSONObject) column.get(j);

        JSONObject entity = (JSONObject) gameBlock.get("Entity");
        JSONObject bomb = (JSONObject) gameBlock.get("Bomb");
        JSONObject powerUp = (JSONObject) gameBlock.get("PowerUp");
        boolean exploding = Boolean.parseBoolean((String) gameBlock.get("Exploding"));
        JSONObject location = (JSONObject) gameBlock.get("Location");
        int x = Integer.parseInt((String) location.get("X"));
        int y = Integer.parseInt((String) location.get("Y"));
      }
    }


  }


  
  public int getMove() {
    if (answer == -1) solve();
    return answer;
  }
  
  private void solve() {
    
  }
}
