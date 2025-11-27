package Client;

import Message.Message;
import enums.MessageType;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter port #: ");
        int port = sc.nextInt();
        sc.nextLine(); //flush scanner
        OutputStream out = null;
        InputStream in = null;

        try(Socket socket = new Socket("localhost", port)){
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            System.out.print("\nConnection successful.\n");
        
            
        } catch(IOException e) { //\ ClassNotFoundException e
        e.printStackTrace();
        }
    } 

    public Message login(){
        Scanner scan = new Scanner(System.in);
        System.out.print("\nEnter a username: ");
        String username = scan.nextLine();
        System.out.print("\nEnter a password: ");
        String password = scan.nextLine();

        String userpw = (username + "," + password);
        return new Message(MessageType.LOGIN,"CLIENT.ID", "SERVER", userpw, null); 
    }


}



