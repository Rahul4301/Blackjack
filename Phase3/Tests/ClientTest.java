package Tests;

import Client.Client;
import Enums.GameState;
import Enums.MessageType;
import Message.Message;
import Server.Account;
import Shared.CardView;
import Shared.DealerView;
import Shared.PlayerView;
import Shared.TableSnapshot;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ClientTest {

    /** Minimal concrete Account for testing login/deposit logic. */
    static class TestAccount extends Account {
        public TestAccount(String username) {
            this.username = username;
        }
    }

    // ---------- Helpers ----------

    /** Build an ObjectInputStream that will return the given Messages in order. */
    private ObjectInputStream makeInStream(Message... messages) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(buf);
        for (Message m : messages) {
            oos.writeObject(m);
        }
        oos.flush();
        return new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()));
    }

    private Message ok(Object payload) {
        return new Message(
                UUID.randomUUID().toString(),
                MessageType.OK,
                "SERVER",
                "CLIENT",
                payload,
                LocalDateTime.now()
        );
    }

    private Message err(String msg) {
        return new Message(
                UUID.randomUUID().toString(),
                MessageType.ERROR,
                "SERVER",
                "CLIENT",
                msg,
                LocalDateTime.now()
        );
    }

    // ---------- TESTS ----------

    @Test
    void deposit_error_returns_false() throws Exception {
        ByteArrayOutputStream sent = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(sent);

        TestAccount acc = new TestAccount("sam");

        // First response: LOGIN -> OK with Account
        // Second response: DEPOSIT -> ERROR
        ObjectInputStream in = makeInStream(
                ok(acc),
                err("Deposit amount must not exceed 1000 per transaction")
        );

        Client client = new Client(out, in);

        // Simulate real usage: login first, then deposit
        assertTrue(client.login("sam", "pw"));
        boolean result = client.deposit(2000.0);

        assertFalse(result);

        // Optional: verify that the second message we sent was of type DEPOSIT
        ObjectInputStream sentReader =
                new ObjectInputStream(new ByteArrayInputStream(sent.toByteArray()));
        Message loginSent = (Message) sentReader.readObject();
        Message depositSent = (Message) sentReader.readObject();

        assertEquals(MessageType.LOGIN, loginSent.getMessageType());
        assertEquals(MessageType.DEPOSIT, depositSent.getMessageType());
    }

    @Test
    void deposit_success_returns_true() throws Exception {
        ByteArrayOutputStream sent = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(sent);

        TestAccount acc = new TestAccount("sam");

        // First response: LOGIN -> OK with Account
        // Second response: DEPOSIT -> OK with success string
        ObjectInputStream in = makeInStream(
                ok(acc),
                ok("Deposit successful: 100.0")
        );

        Client client = new Client(out, in);

        assertTrue(client.login("sam", "pw"));
        boolean result = client.deposit(100.0);

        assertTrue(result);

        // Verify the second outbound message is a DEPOSIT
        ObjectInputStream sentReader =
                new ObjectInputStream(new ByteArrayInputStream(sent.toByteArray()));
        Message loginSent = (Message) sentReader.readObject();
        Message depositSent = (Message) sentReader.readObject();

        assertEquals(MessageType.LOGIN, loginSent.getMessageType());
        assertEquals(MessageType.DEPOSIT, depositSent.getMessageType());
    }

    @Test
    void joinTable_returns_snapshot() throws Exception {
        ByteArrayOutputStream sent = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(sent);

        TestAccount acc = new TestAccount("sam");

        // Build a minimal, valid TableSnapshot
        DealerView dealerView = new DealerView(List.of(), false);
        PlayerView playerView = new PlayerView(
                "sam", 0.0, 0, true, true, false,
                List.of(), 1000.0
        );
        TableSnapshot snapshot = new TableSnapshot(
                "T1",
                GameState.BETTING,
                "sam",
                dealerView,
                List.of(playerView)
        );

        // First response: LOGIN -> OK with Account
        // Second response: JOIN_TABLE -> OK with TableSnapshot
        ObjectInputStream in = makeInStream(
                ok(acc),
                ok(snapshot)
        );

        Client client = new Client(out, in);

        assertTrue(client.login("sam", "pw"));

        TableSnapshot result = client.joinTable("T1");
        assertNotNull(result);
        assertEquals("T1", result.getTableId());

        // Check that second outbound message was JOIN_TABLE
        ObjectInputStream sentReader =
                new ObjectInputStream(new ByteArrayInputStream(sent.toByteArray()));
        Message loginSent = (Message) sentReader.readObject();
        Message joinSent = (Message) sentReader.readObject();

        assertEquals(MessageType.LOGIN, loginSent.getMessageType());
        assertEquals(MessageType.JOIN_TABLE, joinSent.getMessageType());
    }
}
