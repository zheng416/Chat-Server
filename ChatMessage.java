
import java.io.Serializable;

final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;
    private int type;
    private String message;
    private String recipient;

    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.
    public ChatMessage(int type1, String msg1) {
        this.type = type1;
        this.message = msg1;
    }

    public ChatMessage(String m) {
        this.message = m;
        if (m.equals("/logout")) {
            this.type = 1;
        } else if (m.equals("/list")) {
            this.type = 4;
        } else if (m.length() > 3) {
            if (m.substring(0, 4).equals("/msg")) {
                String[] a = m.split(" ");
                if (a.length >= 3) {
                    recipient = a[1];
                    this.message = a[2];
                    for (int i = 3; i < a.length; i++) {
                        this.message += a[i];
                    }
                    this.type = 2;
                } else {
                    if (a.length == 2) {
                        recipient = a[1];
                        this.message = "";
                        this.type =2;
                    } else {
                        this.type = 0;
                    }

                }
            }
        } else {
            this.type = 0;
        }

    }

    public String getMessage() {
        return this.message;
    }

    public int getType() {
        return this.type;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }

    public void setType(int type1) {
        this.type = type1;
    }

    public String getRecipient() {
        return this.recipient;
    }
}
