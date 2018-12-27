package Client;

import Business.User;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Thread {

    private User user;
    private BufferedWriter out;

    public Client(BufferedWriter out){
        this.user = new User();
        this.out = out;
    }

    public User getUser() {
        return user;
    }

    /**
     * Método que envia as informações necessárias de registo para o testeWorker da Server.Cloud associado a este cliente.
     */
    public void register() throws IOException {
        Scanner s = new Scanner(System.in);
        String email = "", password = "";

        System.out.println("E-mail: ");
        if (s.hasNextLine()) {
            email = s.nextLine();
        }

        System.out.println("Password: ");
        if (s.hasNextLine()) {
            password = s.nextLine();
        }

        out.write("Register\n");
        out.write(email+"\n");
        out.write(password+"\n");
        out.flush();
    }

    /**
     * Método que envia as informações necessárias de autenticação para o testeWorker da Server.Cloud associado a este cliente.
     */
    public void login() throws IOException {
        Scanner s = new Scanner(System.in);
        String email = "", password = "";

        System.out.println("E-mail: ");
        if (s.hasNextLine()) {
            email = s.nextLine();
        }

        System.out.println("Password: ");
        if (s.hasNextLine()) {
            password = s.nextLine();
        }

        out.write("Login\n");
        out.write(email+"\n");
        out.write(password+"\n");
        out.flush();
    }

    /**
     * Método que envia as informações necessárias de realização de um pedido de reserva para o testeWorker da Server.Cloud associado a este cliente.
     */
    public void order() throws IOException {
        Scanner s = new Scanner(System.in);
        String type = "";

        System.out.println("Server type: [typeX] [typeY] etc"); //Listar o tipo de servidores que temos disponiveis
        if (s.hasNextLine()) {
            type = s.nextLine();
            if(!(type.equals("typeX") || type.equals("typeY"))){ //Depois mudar
                System.out.println("ERROR: Invalid type.");
                return;
            }
        }

        out.write("Order\n");
        out.write(type+"\n");
        out.flush();
    }

    /**
     * Método que envia as informações necessárias de participação num leilão de reserva para o testeWorker da Server.Cloud associado a este cliente.
     */
    public void auction() throws IOException {
        Scanner s = new Scanner(System.in);
        String type = "", bid = "";

        System.out.println("Server type: [typeX] [typeY]"); //mudar!
        if (s.hasNextLine()) {
            type = s.nextLine();
            if(!type.equals("typeX") || type.equals("typeY")){ 
                System.out.println("ERROR: Invalid type.");
                return;
            }
        }

        System.out.println("Bid: ");
        if (s.hasNextLine()) {
            bid = s.nextLine();
            if(Float.parseFloat(bid)<=0) {
                System.out.println("ERROR: Bid must be greater than zero.");
                return;
            }
        }

        out.write("Auction\n");
        out.write(type+"\n");
        out.write(bid+"\n");
        out.flush();
    }

    /**
     * Método que envia as informações necessárias de libertar um servidor para o testeWorker da Server.Cloud associado a este cliente.
     */
    public void leaveServer() throws IOException {
        Scanner s = new Scanner(System.in);
        String id = "";

        System.out.println("Reservation id: "+ this.user.getReservationIDsType().toString());
        if (s.hasNextLine()) {
            id = s.nextLine();
            if(this.user.getSpecificId(Integer.parseInt(id))==null) {
                System.out.println("ERROR: Invalid id.");
                return;
            }
        }

        out.write("LeaveServer\n");
        out.write(id+"\n");
        out.flush();
    }

    /**
     * Método que envia as informações necessárias de consulta dos fundos para o testeWorker da Server.Cloud associado a este cliente.
     *
     * Como a Server.Cloud não vai mandando msg com o valor atual dos fundos, os fundos obtidos pela autenticação podem já ter mudado.
     * Logo tem de se pedir sempre à Server.Cloud, não se podendo consultar o USER porque pode estar desatualizado.
     */
    public void funds() throws IOException {
        out.write("Funds\n");
        out.flush();
    }

    /**
     * Método que envia as informações necessárias de desautenticação para o testeWorker da Server.Cloud associado a este cliente.
     */
    public void logout() throws IOException {
        out.write("Logout\n");
        out.flush();
    }

    public static void menu(String userMail) {
        String menu1 = "Options:\n\t- Register\n\t- Login\n\t- Quit\n";
        String menu2 = "Options:\n\t- Order\n\t- Auction\n\t- LeaveServer\n\t- Funds\n\t- Logout\n";

        if (userMail==null) System.out.print(menu1);
        else System.out.print(menu2);
    }

    /**
     * Main do Client que inicia um cliente criando um socket para este se conectar com a Server.Cloud.
     *
     * O Client lê do terminal e escreve para a Server.Cloud pedindo as ações solicitadas com a devida informação.
     * A thread ReadFromCloud lê da Server.Cloud respostas a essas ações e escreve-as no terminal do Client.
     */
    public static void main(String[] args) {
        Socket socket = null;

        try {
            socket = new Socket("127.0.0.1",12345);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            Client c = new Client(out);
            Thread t = new Thread(new ReadFromCloud(socket, c.getUser()));
            t.start();

            Scanner s = new Scanner(System.in);
            String str = "";
            boolean logedOut = false;

            menu(c.getUser().getEmail());

            if (s.hasNextLine()) {
                str = s.nextLine();
            }

            while(!(str.equals("Quit") && c.getUser().getEmail()==null) && !logedOut) {
                if(c.getUser().getEmail()==null){
                    switch (str) {
                        case "Register":
                            c.register();
                            break;
                        case "Login":
                            c.login();
                            break;
                        default:
                            System.out.println("ERROR: Invalid operation.");
                    }
                }
                else {
                    switch (str) {
                        case "Order":
                            c.order();
                            break;
                        case "Auction":
                            c.auction();
                            break;
                        case "LeaveServer":
                            c.leaveServer();
                            break;
                        case "Funds":
                            c.funds();
                            break;
                        case "Logout":
                            c.logout();
                            logedOut=true;
                            break;
                        default:
                            System.out.println("ERROR: Invalid operation.");
                    }
                }

                if (!logedOut && s.hasNextLine())
                    str = s.nextLine();
            }

            System.out.println("Turning off.");
            socket.shutdownOutput();
            t.join();
            socket.close();

        } catch (IOException e) {
            System.out.println("ERROR: Problem connecting to cloud.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
