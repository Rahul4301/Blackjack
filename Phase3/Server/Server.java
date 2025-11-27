package Server;

import Enums.MessageType;
import Message.Message;
import java.io.*;
import java.net.*;
//import java.util.ArrayList;


public class Server {
    //private ArrayList<GameTable> gameTables;
    private static LoginManager manager;

    public static void main(String[] args) throws IOException, ClassNotFoundException{
        ServerSocket server = null;
        System.out.println("ServerSocket awaiting connections...");
        try{
            //Change ip / port eventually
            server = new ServerSocket(8080);
            server.setReuseAddress(true);

            while(true){
                Socket client = server.accept();
                System.out.print("\nNew client connected: " + client.getInetAddress().getHostAddress() + "\n");

                ClientHandler clientSock = new ClientHandler(client);
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null){
                try{
                    server.close();
                    System.exit(1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return;
    }

    private static class ClientHandler implements Runnable{
        private final Socket clientSocket;

        public ClientHandler(Socket socket){
            this.clientSocket = socket;
        }

        @Override
        public void run(){
            try{
                ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                objectOutputStream.flush();

                Message clientMessage = (Message) objectInputStream.readObject();
                while((clientMessage.getMessageType() != MessageType.LOGOUT)){
                    switch(clientMessage.getMessageType()){
                        case LOGIN -> {
                                if((clientMessage.getPayload() instanceof String payload)){
                                    String[] tokens = payload.split(",");
                                    Account account = manager.login(tokens[0], tokens[1]); //Assuming payload format is string "username,password".
                                    objectOutputStream.writeObject(new Message(MessageType.OK, "SERVER", "CLIENT", account));
                                    return;
                                }
                        }
                    }
                }

            } catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    System.out.print("\nClient has disconnected.\n");
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //public void start();
    //public void stop();
    //public void broadcastUpdate();
    //public void handleMessage(Message msg);
}
