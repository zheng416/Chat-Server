
import java.io.Serializable;

final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;
    private int type;
    private String message;
    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.
    public ChatMessage(int type1, String msg1) {
        this.type = type1;
        this.message = msg1;
    }
    public ChatMessage() {

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
}
