import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ChatFilter {

    String badWordsFileName;

    public ChatFilter(String badWordsFileName) {
        this.badWordsFileName = badWordsFileName;

    }

    public String filter(String msg) {
        File file = new File(badWordsFileName);
        Scanner scan;
        try {
            scan = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//        while (scan.hasNextLine()) {
//
//        }

        return msg;
    }
}
