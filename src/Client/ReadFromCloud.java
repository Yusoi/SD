package Client;

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
                switch (str) {
                    //Working
                    case "RegisterSuccess": {
                        System.out.println(in.readLine()); //mensagem mais especifica para o cliente tipo "O registo ocorreu com sucesso.\n"
                        Client.menu(client.getLoggedIn());
                        break;
                    }

                    //Working
                    case "LoginSuccess": {
                        client.setLoggedIn(true);
                        System.out.println(in.readLine());
                        Client.menu(client.getLoggedIn());
                        break;
                    }

                    //Working
                    case "ServerCatalogueSuccess": {
                        System.out.println(in.readLine());
                        break;
                    }

                    //Working
                    case "OrderSuccess": {
                        id = Integer.parseInt(in.readLine());
                        type = in.readLine();
                        System.out.println(in.readLine());
                        Client.menu(client.getLoggedIn());
                        break;
                    }

                    //Still needs testing
                    case "AuctionCatalogueSuccess": {
                        System.out.println(in.readLine());
                        break;
                    }

                    //Still needs testing
                    case "AuctionSuccess": {
                        System.out.println(in.readLine());
                        Client.menu(client.getLoggedIn());
                        break;
                    }

                    //Working
                    case "RentedServersSuccess": {
                        System.out.println(in.readLine());
                        Client.menu(client.getLoggedIn());
                        break;
                    }

                    //Still needs testing
                    case "BiddedAuctionsSuccess": {
                        System.out.println(in.readLine());
                        Client.menu(client.getLoggedIn());
                        break;
                    }

                    //Working
                    case "LeaveServerSuccess": {
                        System.out.println(in.readLine());
                        Client.menu(client.getLoggedIn());
                        break;
                    }

                    //Working
                    case "Funds": {
                        System.out.println(in.readLine());
                        Client.menu(client.getLoggedIn());
                        break;
                    }

                    //Working
                    case "LogoutSuccess": {
                        client.setLoggedIn(false);
                        client.setEmail(null);
                        System.out.println(in.readLine());
                        Client.menu(client.getLoggedIn());
                        break;
                    }

                    case "AuctionCancel": {
                        System.out.println("Auction number "+in.readLine()+" was cancelled by the server");
                        break;
                    }

                    //Working
                    case "Unsuccessful": {
                        System.out.println(in.readLine()); //todas as mensagens de insucesso tipo "Login failed.\n" OU "Email already exists. Registration failed.\n"
                        Client.menu(client.getLoggedIn());
                        break;
                    }

                    default: {
                        System.out.println("ERROR: There seems to be a problem with the Server.Cloud.\n");
                    }
                }
            }

            socket.shutdownInput();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
