package Message;
import Enums.MessageType;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Serializable messaging envelope used for client/server communication.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String messageID;
    private final MessageType messageType;
    private final String sender;
    private final String recipient;
    private final Object payload;
    private final LocalDateTime timestamp;

    public Message(String messageID, MessageType messageType, String sender, String recipient, Object payload, LocalDateTime timestamp) {
        this.messageID = messageID;
        this.messageType = messageType;
        this.sender = sender;
        this.recipient = recipient;
        this.payload = payload;
        this.timestamp = timestamp;
    }

    public String getMessageID() {
        return messageID;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public Object getPayload() {
        return payload;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Message[id=" + messageID + ", type=" + messageType + ", sender=" + sender + ", recipient=" + recipient + ", timestamp=" + timestamp + "]";
    }
}
