package Server;

import java.io.*;
import java.net.Socket;
import java.util.List;

import Exceptions.*;

public class ClientConnection implements Runnable{

    private Socket clientSocket;
    private Cloud cloud;
    private String email;

    public ClientConnection(Socket clientSocket, Cloud cloud){
        this.clientSocket = clientSocket; //Socket this thread is listening to.
        this.cloud = cloud;
        this.email = null;
    }

    public void operationInterpreter(String message,BufferedReader reader,PrintWriter writer) throws OperationUnsuccessfulException{

        try {

            switch(message){

                case "Login": {

                    String email = reader.readLine();
                    String password = reader.readLine();

                    try {
                        cloud.login(email, password);
                        this.email = email;
                        writer.append("LoginSuccess\n");
                    } catch (WrongCredentialsException e) {
                        writer.append("Unsuccessful\n");
                    }
                    break;
                }

                case "Register": {

                    String email = reader.readLine();
                    String password = reader.readLine();

                    try {
                        cloud.register(email, password);
                        writer.append("RegisterSuccess\n");
                        writer.append("Utilizador registado com sucesso!");
                    } catch (ExistingUserException e) {
                        writer.append("Unsuccessful\n");
                    }
                    break;
                }

                case "Order": {

                    String type = reader.readLine();

                    try {
                        int id = cloud.order(type,email);

                        writer.append("OrderSuccess\n");
                        writer.append(String.valueOf(id)+"\n");
                        writer.append(type+"\n");
                        writer.append("Order was created successfully\n");
                    } catch (NonExistingServerException e ) {
                        writer.append("Unsuccessful\n");
                    } catch (UserNotAutenthicatedException e) {
                        writer.append("Unsuccessful\n");
                    }
                    break;
                }

                case "Auction": {

                    String type = reader.readLine();
                    String bid = reader.readLine();

                    try {
                        int id = cloud.auction(type,Float.valueOf(bid),email);
                        writer.append("AuctionSuccess\n");
                        writer.append(String.valueOf(id)+"\n");
                        writer.append(type+"\n");
                        writer.append("Auction was created successfully\n");
                    } catch (NonExistingServerException e) {
                        writer.append("Unsuccessful\n");
                    } catch (UserNotAutenthicatedException e) {
                        writer.append("Unsuccessful\n");
                    }
                    break;
                }

                case "LeaveServer": {
                    String id = reader.readLine();

                    try {
                        cloud.leaveServer(Integer.valueOf(id));
                        writer.append("LeaveServerSuccess\n");
                        writer.append("Servidor abandonado com sucesso!\n");
                    } catch (NonExistingServerException e) {
                        writer.append("Unsuccessful\n");
                    } catch (UserNotAutenthicatedException e) {
                        writer.append("Unsuccessful\n");
                    }
                    break;
                }

                case "Funds": {
                    try {
                        float funds = cloud.funds(email);
                        writer.append("Funds\n");
                        writer.append(Float.toString(funds) + "\n");
                        writer.append("Funds: " + Float.toString(funds) + "\n");
                        break;
                    } catch(UserNotAutenthicatedException e) {
                        writer.append("Unsuccessful\n");
                    }
                }

                case "Logout": {
                    this.email = null;
                    writer.append("LogoutSuccess\n");
                    writer.append("Logout efetuado com sucesso!\n");
                    break;
                }

                default: {
                    throw (new OperationUnsuccessfulException("Unsuccessful\n"));
                }

            }

        }catch(IOException e){
            writer.append("Unsuccessful\n");
        }

        writer.flush();
    }


    @Override
    public void run(){
        try{
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            PrintWriter writer = new PrintWriter(output);

            String message;

            while(!clientSocket.isClosed()){
                message = reader.readLine();

                try{

                    operationInterpreter(message,reader,writer);

                } catch(OperationUnsuccessfulException e){
                    System.out.println(e.toString());
                }
            }

            clientSocket.shutdownInput();
            clientSocket.shutdownOutput();
            clientSocket.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
