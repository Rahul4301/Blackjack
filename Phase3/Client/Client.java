package Client;

import Enums.MessageType;
import Message.Message;
import Server.Account;
import Enums.MessageType;
import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.UUID;

public class Client {
    private static String clientUUID;
    private static Account account;
    public static void main(String[] args){
        clientUUID = UUID.randomUUID().toString();
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter port #: ");
        int port = sc.nextInt();
        sc.nextLine(); //flush scanner
        OutputStream out = null;
        InputStream in = null;

        boolean connected = true;
        
        try(Socket socket = new Socket("localhost", port)){
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            System.out.print("\nConnection successful.\n");

            //TEST CLIENT / SERVER HANDLING
            //TODO: make server message handler
            System.out.println("1 to login or 2 to logout");
            Scanner scan = new Scanner(System.in);
            int choice = scan.nextInt();
            //test LOGIN
            while(choice != 3){
                switch (choice){

                    case 1:{
                        objectOutputStream.writeObject(login("sam","sam"));
                        objectOutputStream.flush();
                    } break;

                    case 2:{
                    //test LOGOUT
                        objectOutputStream.writeObject(logout());
                        objectOutputStream.flush();
                    } break;

                    default:{
                        System.exit(1);
                    } break;
                    }
                    Message response = (Message) objectInputStream.readObject();
                    System.out.println(response.toString());
                    if(response.getPayload() instanceof Account acc){
                        System.out.println(acc.getUsername() + " is logged in!");
                        account = acc;
                    }
                    choice = scan.nextInt();
            }
            socket.close();
            
        } catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    } 

    public static Message login(String username, String password){
        String[] userpw = {username, password};
        return new Message(
            UUID.randomUUID().toString(), 
            MessageType.LOGIN,
            clientUUID,
            "SERVER",
            userpw, LocalDateTime.now()); 
    }

    public static Message logout(){
        return new Message(
            UUID.randomUUID().toString(), 
            MessageType.LOGOUT, 
            clientUUID,
            "SERVER",
            null,
            LocalDateTime.now());
    }


}



