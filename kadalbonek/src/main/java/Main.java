import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

public class Main {
  public static void main(String... args){

    String botKey = args[0];
    String botDir = args[1].replace("\"","");

    System.out.println("Bot Key: " + botKey);
    System.out.println("Bot Dir: " + botDir);

    Path path = Paths.get(botDir + File.separator + "state.json");

    File move = new File(botDir + File.separator + "move.txt");
    try {
      List<String> lines = Files.readAllLines(path, Charset.defaultCharset());

      StringBuilder fileContent = new StringBuilder();
      for (String line : lines) {

        fileContent.append(line);
      }
      
      Strategy strategy = new Strategy(fileContent.toString());

      if(move.createNewFile()){

        FileWriter write = new FileWriter(move);
        write.write(String.valueOf(strategy.getMove()));
        write.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
