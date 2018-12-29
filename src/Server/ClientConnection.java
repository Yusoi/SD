package Server;

import java.io.*;
import java.net.Socket;
import java.util.List;

import Exceptions.OperationUnsuccessfulException;

public class ClientConnection implements Runnable{

    private Socket clientSocket;
    private Cloud cloud;

    public ClientConnection(Socket clientSocket, Cloud cloud){
        this.clientSocket = clientSocket; //Socket this thread is listening to.
        this.cloud = cloud;
    }

    public String operationInterpreter(String message) throws OperationUnsuccessfulException{
        int messageType = -1;

        if(message == "Catalogue"){
            List<String> catalogue = cloud.getServerCatalogue();
        }
        return null;
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
                    answer = operationInterpreter(message);
                }catch(OperationUnsuccessfulException e){
                    e.printStackTrace();
                }



            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
