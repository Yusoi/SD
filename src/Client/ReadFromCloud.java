package Client;

import Business.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Classe Runnable que é criada como Thread por um Cliente quando este se inicia.
 * Esta tem como função ler as informações provenientes da Server.Cloud para o Cliente e
 * escreve-las no terminal.
 */
public class ReadFromCloud implements Runnable{

    private Socket socket;
    private Client client;

    public ReadFromCloud(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;
    }

    /**
     * Método executado sempre que a Thread é criada.
     * Esta cria um BufferedReader para ler as informações provenientes da Server.Cloud.
     * Consoante as informações recebidas, esta escreve mensagems para o terminal do Cliente.
     */
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String str, type;
            int id;

            while((str=in.readLine())!=null){
                if(!client.getLoggedIn()) {
                    switch (str) {
                        case "RegisterSuccess":
                            System.out.println(in.readLine()); //mensagem mais especifica para o cliente tipo "O registo ocorreu com sucesso.\n"
                            break;

                        case "LoginSuccess":
                            client.setLoggedIn(true);
                            System.out.println(in.readLine());
                            break;

                        case "Unsuccessful":
                            System.out.println(in.readLine()); //todas as mensagens de insucesso tipo "Login failed.\n" OU "Email already exists. Registration failed.\n"
                            break;

                        default:
                            System.out.println("ERROR: There seems to be a problem with the Server.Cloud.\n");
                    }
                } else {
                    switch(str){
                        case "OrderSuccess":
                            id = Integer.parseInt(in.readLine());
                            type = in.readLine();
                            System.out.println(in.readLine());
                            break;

                        case "AuctionSuccess":
                            id = Integer.parseInt(in.readLine());
                            type = in.readLine();
                            System.out.println(in.readLine());
                            break;

                        case "LeaveServerSuccess":
                            id = Integer.parseInt(in.readLine());
                            System.out.println(in.readLine());
                            break;

                        case "Funds":
                            System.out.println(in.readLine());
                            break;

                        case "LogoutSuccess":
                            client.setLoggedIn(false);
                            System.out.println(in.readLine());
                            break;

                        case "CancelledServer":
                            id = Integer.parseInt(in.readLine());
                            System.out.println(in.readLine());
                            break;

                        case "Unsuccessful":
                            System.out.println(in.readLine()); //todas as mensagens de insucesso tipo "Login failed.\n" OU "Email already exists. Registration failed.\n"
                            break;

                        default:
                            System.out.println("ERROR: There seems to be a problem with the Server.Cloud.\n");
                    }
                }

                client.menu(client.getLoggedIn());
            }

            socket.shutdownInput();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
