package utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Message {

    final public MessageType type;
    final public String content;
    public List<String> tokens;

    public Message(MessageType type, String content) {
        super();
        List<String> tokens = Arrays.asList(content.split("\\s*,\\s*"));
        this.type = type;
        this.content = content;
        this.tokens = tokens;
    }

    public Message(Move m) {
        super();

        this.type = MessageType.MOVE;
        this.content = m.toMessage();
    }

    public static Message parseMessage(String s) {
        if (s == null)
            return new Message(MessageType.PARSE_ERROR, "");

        LinkedList<String> tokens = new LinkedList<>(Arrays.asList(s.split(" ")));
        MessageType type = null;
        MessageType[] types = MessageType.values();

        for (int i = 0; i < types.length; i++) {
            if (tokens.get(0).equals(types[i].name())) {
                type = types[i];
                break;
            }
        }

        tokens.remove(0);

        if (type == null)
            return new Message(MessageType.PARSE_ERROR, "");

        Optional<String> content = tokens.stream().reduce((s1, s2) -> s1 + " " + s2);
        if (!content.isPresent())
            return new Message(type, "");

        return new Message(type, content.get());
    }

    public String toString() {
        return type.name() + " " + content;
    }

    public enum MessageType {

        WELCOME,
        MESSAGE,
        OPPONENT_MOVE,
        YOUR_TURN,
        VALID_MOVE,
        ILLEGAL_MOVE,
        TIMEOUT,
        VICTORY,
        TIE,
        DEFEAT,
        MOVE,
        PARSE_ERROR

    }

}
