package Server;

import java.io.*;
import java.net.Socket;
import java.util.List;

import Exceptions.ExistingUserException;
import Exceptions.OperationUnsuccessfulException;
import Exceptions.WrongCredentialsException;

public class ClientConnection implements Runnable{

    private Socket clientSocket;
    private Cloud cloud;
    private String email;

    public ClientConnection(Socket clientSocket, Cloud cloud){
        this.clientSocket = clientSocket; //Socket this thread is listening to.
        this.cloud = cloud;
        this.email = null;
    }



    public String operationInterpreter(String message,BufferedReader reader) throws OperationUnsuccessfulException{
        String answer = null;

        try {
            if (message.equals("Login")) {

                String email = reader.readLine();
                String password = reader.readLine();

                try {
                    cloud.login(email, password);
                    this.email = email;
                    answer = "LoginSuccess\n";
                }catch(WrongCredentialsException e){
                    return e.toString();
                }

            } else if(message.equals("Register")) {

                String email = reader.readLine();
                String password = reader.readLine();

                try{
                    cloud.register(email,password);
                    answer = "RegisterSuccess\n";
                }catch(ExistingUserException e){
                    return e.toString();
                }

            } else if(message.equals("Order")) {

                String type = reader.readLine();

                //TODO
                try{
                    answer = "OrderSuccess\n";
                }catch(){

                }

            } else if(message.equals("Auction")) {

                String type = reader.readLine();
                String bid = reader.readLine();

                //TODO
                try{
                    answer = "AuctionSuccess\n";
                }catch(){

                }

            } else if(message.equals("LeaveServer")) {
                String id = reader.readLine();

                //TODO
                try{
                    answer = "LeaveServerSuccess\n";
                }catch(){

                }
            } else if(message.equals("Funds")) {
                //TODO
                try{
                    answer = "Funds\n";
                }catch(){

                }
            } else if(message.equals("Logout")) {

                this.email = null;
                try{
                    answer = "AuctionSuccess\n";
                }catch(){

                }

            } else {
                throw (new OperationUnsuccessfulException("Unsuccess\n"));
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        return answer;
    }


    @Override
    public void run(){
        try{
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            PrintWriter writer = new PrintWriter(output);

            String message = null;
            String answer = null;

            while(!clientSocket.isClosed()){
                message = reader.readLine();

                try{

                    answer = operationInterpreter(message,reader);
                    writer.write(answer);
                    writer.flush();

                }catch(OperationUnsuccessfulException e){
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
