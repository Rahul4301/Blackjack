package Server;

import Enums.MessageType;
import Message.Message;
import java.io.*;
import java.net.*;
//import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.UUID;


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
                manager = new LoginManager();
                manager.loadData();
                ClientHandler clientSock = new ClientHandler(client, manager);
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

    public LoginManager getLoginManager(){
        return manager;
    }

}





    //public void start();
    //public void stop();
    //public void broadcastUpdate();
    //public void handleMessage(Message msg);

