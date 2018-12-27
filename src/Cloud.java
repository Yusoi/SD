import Client.User;
import Server.ExistingUserException;
import Server.Rent;
import Server.Server;
import Server.WrongCredentialsException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;

public class Cloud implements Runnable{

    public HashMap<String,User> users; //users ou clients again
    //Guardar Servidores
    public HashMap<String,Integer> freeServers;
    //Guarda rents por identificador
    public HashMap<Integer, Rent> rents;
    //Guarda preços por tipo de servidor, String = ServerType
    public HashMap<String, Float> prices;

    public static int port = 4242;
    public ServerSocket serverSocket = null;
    public Thread currentThread = null;

    // mandar por socket ao cliente a dizer erro ou certo
    synchronized public boolean register(String email, String password) throws ExistingUserException {
        if (users.containsKey(email)) {
            throw new ExistingUserException("O utilizador já existe");
        }
        User user = new User(email,password, (float) 0);
        users.put(email,user);

        return true; //na "interface", basta ver se é true para imprimir "registado com sucesso" ou algo do genero.
    }


    synchronized public boolean login(String email, String password) throws WrongCredentialsException {
        if (users.containsKey(email)&& users.get(email).getPassword() == password){
            return true;
        } else throw new WrongCredentialsException("Nao existe nenhum utilizador com essa combinação de email e password");

    }

    synchronized public void addServer(Server s)  {
        if(freeServers.containsKey(s.getServerId())){
            freeServers.put(s.getServerId(),freeServers.get(s.getServerId())+1);
        } else {
            freeServers.put(s.getServerId(),1);
        }
    }

    public void createRent(int rentType, User user, Server server){
        int id = generateRentId();
        Rent r = new Rent(id,rentType,user,server);
        rents.put(id,r);
    }

    public int generateRentId(){
        return Collections.max(rents.keySet())+1;
    }


    @Override
    public void run(){

        synchronized(this){
            currentThread = Thread.currentThread();
        }
        openServerSocket();

        Socket clientSocket = null;
        
        try{
            clientSocket = this.serverSocket.accept();
        } catch(IOException e){
            e.printStackTrace();
        }


    }

    public void openServerSocket(){
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /*Arrancar com os servidores e clientes*/
    public static void main(String[] args){

    }
}
