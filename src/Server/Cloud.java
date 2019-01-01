package Server;

import Business.User;
import Exceptions.ExistingUserException;
import Business.Rent;
import Business.Server;
import Exceptions.NonExistingServerException;
import Exceptions.UserNotAutenthicatedException;
import Exceptions.WrongCredentialsException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Cloud implements Runnable{

    //Guardar utilizadores
    public HashMap<String,User> users; //users ou clients again
    //Guardar número de servidores livres de cada tipo
    public HashMap<String,Integer> freeServers;
    //Guarda rents por identificador
    public HashMap<Integer, Rent> rents;
    //Guarda preços por tipo de servidor, String = ServerType
    public HashMap<String, Float> prices;

    //Port da ServerSocket
    public int port;
    //O ServerSocket em si
    public ServerSocket serverSocket = null;
    //Server está parado ou não
    public boolean isStopped = true;


    public Cloud(int port){
        this.port = port;
    }

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

    public synchronized void addRent(int rentType, User user, Server server){
        int id = generateRentId();
        Rent r = new Rent(id,rentType,user,server);
        rents.put(id,r);
    }

    public synchronized int order(String serverType, String email) throws NonExistingServerException, UserNotAutenthicatedException{
        Integer typeCheck = freeServers.get(serverType);
        if(typeCheck == 0 || typeCheck == null){
            throw new NonExistingServerException("Server Nonexistent");
        }
        //TODO

        return -1;
    }

    public synchronized int auction(String serverType, float bid , String email) throws NonExistingServerException, UserNotAutenthicatedException{
        Integer typeCheck = freeServers.get(serverType);
        if(typeCheck == 0 || typeCheck == null){
            throw new NonExistingServerException("Server Nonexistent");
        }
        //TODO

        return -1;
    }

    public synchronized void leaveServer(int id) throws NonExistingServerException, UserNotAutenthicatedException{
        Rent rent = rents.get(id);
        if(rent == null){
            throw new NonExistingServerException("Server Nonexistent");
        }
        //TODO
    }

    public float funds(String email) throws UserNotAutenthicatedException{
        return users.get(email).getFunds();
    }

    public int generateRentId(){
        return Collections.max(rents.keySet())+1;
    }

    @Override
    public void run(){

        openServerSocket();

        while(!isStopped) {
            Socket clientSocket = null;

            try {

                clientSocket = this.serverSocket.accept();

                new Thread(new ClientConnection(clientSocket,this)).start();

            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    //Abre a ServerSocket
    public void openServerSocket(){
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
