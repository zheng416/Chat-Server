import java.io.*;

public class ChatFilter {
    String badWords;
    public ChatFilter(String badWordsFileName) {
     this.badWords  = badWordsFileName;
    }

    public String filter(String msg) {
        File bad = new File(badWords);
        try {
            BufferedReader in = new BufferedReader(new FileReader(bad));
            try {
                String read = in.readLine();
                while (read != null) {
                    String re = "";
                    for (int i = 0; i < read.length(); i++) {
                        re += "*";
                    }
                    msg.replaceAll("(?i)"+read, re);
                    read = in.readLine();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return msg;
    }
}
