package Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class teste {

    public static void main(String[] args) {
        try {
            ServerSocket sSock = new ServerSocket(12345);

            while (true) {
                Socket clSock = sSock.accept();
                Thread t = new Thread(new testeWorker(clSock));
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
