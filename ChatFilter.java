import java.io.*;
import java.util.Scanner;

public class ChatFilter {

    String badWordsFileName;

    public ChatFilter(String badWordsFileName) {
        this.badWordsFileName = badWordsFileName;

    }

    public String filter(String msg) {
        File file = new File(badWordsFileName);
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String word;
            String x = "";
            while ((word = br.readLine()) != null) {
                if (msg.toLowerCase().contains(word.toLowerCase())) {
                    for (int i = 0; i < word.length() ; i++) {
                        x += "*";
                    }
                    msg = msg.replaceAll("(?i)" + word, x); //Case Insensitive (?i)
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return msg;
    }
}
