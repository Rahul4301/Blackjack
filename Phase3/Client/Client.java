package Client;

import Enums.MessageType;
import Message.Message;
import Server.Account;
import java.io.*;
import java.net.*;
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
            System.out.println("1 to login, 2 to create account, or 3 to logout");
            Scanner scan = new Scanner(System.in);
            int choice = scan.nextInt();
            scan.nextLine(); // flush scanner
            //test LOGIN
            while(choice != 4){
                switch (choice){

                    case 1:{
                        System.out.print("Enter username: ");
                        String username = scan.nextLine();
                        System.out.print("Enter password: ");
                        String password = scan.nextLine();
                        objectOutputStream.writeObject(login(username, password));
                        objectOutputStream.flush();
                    } break;

                    case 2:{
                        // Create account
                        System.out.print("Enter username: ");
                        String username = scan.nextLine();
                        System.out.print("Enter password: ");
                        String password = scan.nextLine();
                        System.out.print("Enter account type (PLAYER or DEALER): ");
                        String type = scan.nextLine();
                        objectOutputStream.writeObject(register(username, password, type));
                        objectOutputStream.flush();
                    } break;

                    case 3:{
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

    public static Message register(String username, String password, String type){
        String[] regData = {username, password, type};
        return new Message(
            UUID.randomUUID().toString(), 
            MessageType.REGISTER,
            clientUUID,
            "SERVER",
            regData, LocalDateTime.now()); 
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



