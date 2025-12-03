package Tests;

import Enums.MessageType;
import Message.Message;
import Server.LoginManager;   // IMPORTANT: import the LoginManager in package Server

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {

    private Class<?> handlerClass;
    private ByteArrayOutputStream outBuffer;

    @BeforeEach
    void setUp() throws Exception {
        // Inner class Server.ClientHandler has this binary name:
        handlerClass = Class.forName("Server.Server$ClientHandler");
        outBuffer = new ByteArrayOutputStream();
    }

    private Object newHandler() throws Exception {
        Constructor<?> ctor =
                handlerClass.getDeclaredConstructor(Socket.class, LoginManager.class);
        ctor.setAccessible(true);
        Object handler = ctor.newInstance(new Socket(), new LoginManager());

        ObjectOutputStream oos = new ObjectOutputStream(outBuffer);
        oos.flush();
        setField(handler, "out", oos);
        setField(handler, "connected", true);
        return handler;
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field f = handlerClass.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private void invokeHandlerMethod(Object handler, String name, Message msg) throws Exception {
        Method m = handlerClass.getDeclaredMethod(name, Message.class);
        m.setAccessible(true);
        m.invoke(handler, msg);
    }

    private Message readSingleResponse() throws Exception {
        ObjectInputStream ois =
                new ObjectInputStream(new ByteArrayInputStream(outBuffer.toByteArray()));
        Object obj = ois.readObject();
        assertTrue(obj instanceof Message, "Expected a Message on the output stream");
        return (Message) obj;
    }

    private Message newRequest(MessageType type, Object payload) {
        return new Message(
                UUID.randomUUID().toString(),
                type,
                "CLIENT",
                "SERVER",
                payload,
                LocalDateTime.now()
        );
    }

    @Test
    public void handleDeposit_rejectsNonNumericPayload() throws Exception {
        Object handler = newHandler();
        Message req = newRequest(MessageType.DEPOSIT, "not_a_number");

        invokeHandlerMethod(handler, "handleDeposit", req);
        Message resp = readSingleResponse();

        assertEquals(MessageType.ERROR, resp.getMessageType());
        assertTrue(((String) resp.getPayload()).contains("valid number"));
    }

    @Test
    public void handleDeposit_rejectsNonStringPayload() throws Exception {
        Object handler = newHandler();
        Message req = newRequest(MessageType.DEPOSIT, 123.45); // wrong type

        invokeHandlerMethod(handler, "handleDeposit", req);
        Message resp = readSingleResponse();

        assertEquals(MessageType.ERROR, resp.getMessageType());
        assertTrue(((String) resp.getPayload()).contains("Invalid deposit payload"));
    }

    @Test
    public void handleDeposit_rejectsZeroOrNegative() throws Exception {
        Object handler = newHandler();
        Message req = newRequest(MessageType.DEPOSIT, "0");

        invokeHandlerMethod(handler, "handleDeposit", req);
        Message resp = readSingleResponse();

        assertEquals(MessageType.ERROR, resp.getMessageType());
        assertTrue(((String) resp.getPayload()).contains("greater than zero"));
    }

    @Test
    public void handleDeposit_rejectsTooLargeAmount() throws Exception {
        Object handler = newHandler();
        Message req = newRequest(MessageType.DEPOSIT, "1001");

        invokeHandlerMethod(handler, "handleDeposit", req);
        Message resp = readSingleResponse();

        assertEquals(MessageType.ERROR, resp.getMessageType());
        assertTrue(((String) resp.getPayload()).contains("not exceed 1000"));
    }

    @Test
    public void handleExit_sendsOkAndMarksDisconnected() throws Exception {
        Object handler = newHandler();
        Message req = newRequest(MessageType.EXIT, null);

        invokeHandlerMethod(handler, "handleExit", req);
        Message resp = readSingleResponse();

        assertEquals(MessageType.OK, resp.getMessageType());
        assertTrue(((String) resp.getPayload()).contains("See you next time"));

        Field connectedField = handlerClass.getDeclaredField("connected");
        connectedField.setAccessible(true);
        boolean connected = (boolean) connectedField.get(handler);
        assertFalse(connected, "Client should be marked disconnected after EXIT");
    }
}
