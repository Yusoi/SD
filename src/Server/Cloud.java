package Server;

import Business.User;
import Exceptions.*;
import Business.Rent;
import Business.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;

public class Cloud implements Runnable{

    //Guardar utilizadores
    public HashMap<String,User> users; //users ou clients again
    //Guardar número de servidores livres de cada tipo
    public HashMap<String,Integer> freeServers;
    //Guarda detalhes sobre um certo tipo de servidor
    public HashMap<String,Server> serverDetails;
    //Guarda rents por identificador
    public HashMap<Integer, Rent> rents;
    //Guarda os leilões ativos
    public HashMap<Integer, Auction> auctions;
    //Guarda preços por tipo de servidor, String = ServerType
    public HashMap<String, Float> prices;

    //Port da ServerSocket
    public int port;
    //O ServerSocket em si
    public ServerSocket serverSocket = null;
    //Server está parado ou não
    public boolean isStopped = false;


    public Cloud(int port){
        this.port = port;
        this.users = new HashMap<>();
        this.freeServers = new HashMap<>();
        this.serverDetails = new HashMap<>();
        this.rents = new HashMap<>();
        this.auctions = new HashMap<>();
        this.prices = new HashMap<>();

        //TODO Aqui está-se a adicionar manualmente os servidores e os dados correspondentes aos mesmos
        freeServers.put("s1.alpha",2);
        freeServers.put("m1.theta",7);
        freeServers.put("m2.rho",4);
        freeServers.put("l1.omega",5);

        serverDetails.put("s1.alpha",new Server("s1.alpha",125));
        serverDetails.put("m1.theta",new Server("m1.theta",250));
        serverDetails.put("m2.rho",new Server("m2.rho",300));
        serverDetails.put("l1.omega",new Server("l1.omega",500));
    }

    // mandar por socket ao cliente a dizer erro ou certo
    public boolean register(String email, String password) throws ExistingUserException {
        synchronized (users) {
            if (!users.containsKey(email)) {
                User user = new User(email, password, (float) 0);
                users.put(email, user);
            } else throw new ExistingUserException("O utilizador já existe");
        }
        return true; //na "interface", basta ver se é true para imprimir "registado com sucesso" ou algo do genero.
    }

    //Função que realiza o login do utilizador
    public boolean login(String email, String password) throws WrongCredentialsException {
        if (users.containsKey(email) && users.get(email).getPassword().equals(password)){
            return true;
        } else throw new WrongCredentialsException("Nao existe nenhum utilizador com essa combinação de email e password");
    }

    /*
    public void addServer(Server s)  {
        synchronized (freeServers) {
            if (freeServers.containsKey(s.getServerId())) {
                freeServers.put(s.getServerId(), freeServers.get(s.getServerId()) + 1);
            } else {
                freeServers.put(s.getServerId(), 1);
            }
        }
    }*/

    //Função que arrenda um servidor a um utilizador pelo preço nominal
    public int order(String serverType, String email) throws NonExistingServerException, UserNotAutenthicatedException{
        if(email == null){
            throw new UserNotAutenthicatedException("Not logged in");
        }

        //TODO Dar synchronize aos hashmaps individualmente mas cuidado para evitar deadlock
        //TODO Adicionar o servidor ao utilizador
        //TODO Possibilidade de cancelar leilão em caso de não haver servidor disponível
        synchronized(freeServers) {
            if(!freeServers.containsKey(serverType)){
                throw new NonExistingServerException("Server Nonexistent");
            }
            if(freeServers.get(serverType) == 0){
                throw new NonExistingServerException("Not enough servers of that type");
            }

            freeServers.put(serverType,freeServers.get(serverType)+1);
            int rentId = generateRentId();
            Rent rent = new Rent(rentId,0,serverDetails.get(serverType).getPrecoNominal(),users.get(email),serverDetails.get(serverType));

            rents.put(rentId,rent);

            return rentId;
        }
    }

    //Função que deixa ao utilizador licitar num leilão
    public int auction(int auctionId, float bid, String serverType, String email) throws NonExistingServerException, UserNotAutenthicatedException{
        if(email == null){
            throw new UserNotAutenthicatedException("Not logged in");
        }




        return auctionId;
    }

    //Função que liberta o servidor de um utilizador
    public void leaveServer(int id, String email) throws NonExistingServerException, UserNotAutenthicatedException{
        if(email == null){
            throw new UserNotAutenthicatedException("Not logged in");
        }

        //TODO Dar synchronize aos hashmaps individualmente mas cuidado para evitar deadlock
        synchronized(this) {
            Rent rent = rents.get(id);
            if (rent == null) {
                throw new NonExistingServerException("Server Nonexistent");
            }

            String serverName = rent.getServer().getServerName();

            //TODO verificar se o servidor pertence ao utilizador antes de o deixar.
            freeServers.put(serverName,freeServers.get(serverName)+1);
            rents.remove(id);
        }

    }

    //Função que retorna os fundos correspondentes ao utilizador
    public float funds(String email) throws UserNotAutenthicatedException{
        if(email == null){
            throw new UserNotAutenthicatedException("Not logged in");
        }
        return users.get(email).getFunds();
    }

    //Função server side que serve para criar um leilão
    public synchronized void createAuction(String serverType, float minimalBid, String email, int duration) throws NonExistingServerException{
        if(!serverDetails.containsKey(serverType)){
            throw new NonExistingServerException("Server does not exist");
        }

        int auctionId = generateAuctionId();

        Auction auction = new Auction(auctionId,minimalBid,serverDetails.get(serverType),duration,this);
        auctions.put(auctionId,auction);

        new Thread(auction).start();

    }

    //Função que a classe Auction utiliza para atribuir um servidor a um utilizador e acabar com uma auction
    public int endAuction(int auctionId, float highestBid, String serverType, String email) throws NoBidException {

        //TODO Dar synchronize aos hashmaps individualmente mas cuidado para evitar deadlock
        //TODO Adicionar o servidor ao utilizador
        //TODO Possibilidade de cancelar leilão em caso de não haver servidor disponível
        synchronized(freeServers) {

            if(auctions.get(auctionId).getHighestBidder() == null){
                auctions.remove(auctionId);
                throw new NoBidException("No user bid");
            }

            int rentId = generateRentId();
            Rent rent = new Rent(rentId,1,highestBid,users.get(email),serverDetails.get(serverType));
            auctions.remove(auctionId);
            rents.put(rentId,rent);

            return rentId;
        }
    }

    //Gera um id novo para cada aluguer
    public int generateRentId(){
        return Collections.max(rents.keySet())+1;
    }

    public int generateAuctionId(){
        return Collections.max(auctions.keySet())+1;
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
