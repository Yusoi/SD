package Server;

import java.net.Socket;

public class ClientConnection implements Runnable{

    private Socket clientSocket;

    public ClientConnection(Socket clientSocket){
        this.clientSocket = clientSocket; //Socket this thread is listening to.
    }


    @Override
    public void run(){

    }

}
