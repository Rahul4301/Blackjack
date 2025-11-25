package Server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import Message.Message;

public class Server {
    private ArrayList<GameTable> gameTables;
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



            } catch(IOException e) {
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
