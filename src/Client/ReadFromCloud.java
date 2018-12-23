package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Classe Runnable que é criada como Thread por um Cliente quando este se inicia.
 * Esta tem como função ler as informações provenientes da Cloud para o Cliente e
 * escreve-las no terminal.
 */
public class ReadFromCloud implements Runnable{

    private Socket socket;
    private User user;

    public ReadFromCloud(Socket socket, User user) {
        this.socket = socket;
        this.user = user;
    }

    /**
     * Método executado sempre que a Thread é criada.
     * Esta cria um BufferedReader para ler as informações provenientes da Cloud.
     * Consoante as informações recebidas, esta escreve mensagems para o terminal do Cliente.
     */
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String str, type;
            int id;
            boolean logedOut = false;

            while((str=in.readLine())!=null && !logedOut){
                switch (str) {
                    case "RegisterSuccess":
                        System.out.println(in.readLine()); //mensagem mais especifica para o cliente tipo "O registo ocorreu com sucesso.\n"
                        break;

                    case "LoginSuccess":
                        this.user.setEmail(in.readLine());
                        this.user.setPassword(in.readLine());
                        this.user.setFunds(Float.valueOf(in.readLine()));

                        int qt_ids = Integer.parseInt(in.readLine());
                        for(int i=0; i<qt_ids; i++){
                            id = Integer.parseInt(in.readLine());
                            type = in.readLine();
                            this.user.putIdType(id,type);
                        }

                        System.out.println(in.readLine());
                        break;

                    case "OrderSuccess":
                        id = Integer.parseInt(in.readLine());
                        type = in.readLine();
                        this.user.putIdType(id, type);
                        System.out.println(in.readLine());
                        break;

                    case "AuctionSuccess":
                        id = Integer.parseInt(in.readLine());
                        type = in.readLine();
                        this.user.putIdType(id, type);
                        System.out.println(in.readLine());
                        break;

                    case "LeaveServerSuccess":
                        id = Integer.parseInt(in.readLine());
                        this.user.remId(id);
                        System.out.println(in.readLine());
                        break;

                    case "Funds":
                        this.user.setFunds(Float.valueOf(in.readLine()));
                        System.out.println(in.readLine());
                        break;

                    case "LogoutSuccess":
                        logedOut = true;
                        System.out.println(in.readLine());
                        break;

                    case "CanceledServer":
                        id = Integer.parseInt(in.readLine());
                        this.user.remId(id);
                        System.out.println(in.readLine());
                        break;

                    case "Insuccess":
                        System.out.println(in.readLine()); //todas as mensagens de insucesso tipo "Login failed.\n" OU "Email already exists. Registration failed.\n"
                        break;

                    default:
                        System.out.println("ERROR: There seems to be a problem with the Cloud.\n");
                }

                if(!logedOut)
                    Client.menu(this.user.getEmail());
            }

            socket.shutdownInput();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
