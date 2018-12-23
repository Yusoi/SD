package Client;

import java.io.*;
import java.net.Socket;

public class testeWorker implements Runnable {

    private Socket clSock;

    public testeWorker(Socket clSock) {
        this.clSock = clSock;
    }

    @Override
    public void run() {
        try {
            String str;
            BufferedReader in = new BufferedReader(new InputStreamReader(clSock.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clSock.getOutputStream()));

            while((str = in.readLine())!= null) {
                System.out.println("worker: "+str);
                out.write(str+"\n");
                out.flush();
            }
            clSock.shutdownInput();
            clSock.shutdownOutput();
            clSock.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

