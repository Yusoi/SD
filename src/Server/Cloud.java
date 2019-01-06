package Server;

import Exceptions.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

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
            } else throw new ExistingUserException("O utilizador já existe\n");
        }
        return true; //na "interface", basta ver se é true para imprimir "registado com sucesso" ou algo do genero.
    }

    //Função que realiza o login do utilizador
    public boolean login(String email, String password) throws WrongCredentialsException {
        if (users.containsKey(email) && users.get(email).getPassword().equals(password)){
            return true;
        } else throw new WrongCredentialsException("Nao existe nenhum utilizador com essa combinação de email e password\n");
    }

    public String serverCatalogue() {
        synchronized (freeServers) {
            return freeServers.entrySet().stream().filter(e -> e.getValue() > 0).map(e -> e.getKey()).collect(Collectors.joining(" "));
        }
    }

    //Função que arrenda um servidor a um utilizador pelo preço nominal
    public int order(String serverType, String email) throws NonExistingServerException, UserNotAuthenticatedException {
        if(email == null){
            throw new UserNotAuthenticatedException("Not logged in\n");
        }

        //TODO Dar synchronize aos hashmaps individualmente mas cuidado para evitar deadlock
        synchronized(freeServers) {
            if(!freeServers.containsKey(serverType)){
                throw new NonExistingServerException("Server Nonexistent\n");
            }
            if(freeServers.get(serverType) == 0){
                Optional<Auction> a = auctions.values().stream().filter(e -> e.getServer().getServerName().equals(serverType)).findAny();

                if(a.isPresent()){
                    cancelAuction(a.get().getId());
                }else{
                    throw new NonExistingServerException("Not enough servers of that type\n");
                }
            }else {
                freeServers.put(serverType, freeServers.get(serverType) - 1);
            }
            int rentId = generateRentId();
            User u = users.get(email);
            Rent rent = new Rent(rentId,0,serverDetails.get(serverType).getPrecoNominal(),u,serverDetails.get(serverType));
            u.setFunds(u.getFunds()+rent.getPricePerHour());
            rents.put(rentId,rent);

            return rentId;
        }
    }

    public String auctionCatalogue() {
        synchronized (auctions) {
            return auctions.values().stream().map(e -> Integer.toString(e.getId())+" - "+e.getServer().getServerName()+" - Highest Bid: "+e.getValue()).collect(Collectors.joining(" "));
        }
    }

    //Função que deixa ao utilizador licitar num leilão
    public int auction(int auctionId, float bid, String email) throws NonExistingServerException, UserNotAuthenticatedException, BidNotHighEnoughException {
        if(email == null){
            throw new UserNotAuthenticatedException("Not logged in\n");
        }

        Auction a;
        User u;

        synchronized(auctions){
            if(auctions.containsKey(auctionId)) {
                a = auctions.get(auctionId);
            } else {
                throw new NonExistingServerException("Auction does not exist\n");
            }
        }

        synchronized(users){
            u = users.get(email);
        }

        try {
            a.newBid(u, bid);
        }catch(BidNotHighEnoughException e){
            throw e;
        }


        return auctionId;
    }

    //Função que liberta o servidor de um utilizador
    public void leaveServer(int id, String email) throws NonExistingServerException, UserNotAuthenticatedException {
        if(email == null){
            throw new UserNotAuthenticatedException("Not logged in\n");
        }

        //TODO Dar synchronize aos hashmaps individualmente mas cuidado para evitar deadlock
        synchronized(this) {
            Rent rent = rents.get(id);
            if (rent == null) {
                throw new NonExistingServerException("Server Nonexistent\n");
            }

            String serverName = rent.getServer().getServerName();

            if(rent.getUser().getEmail().equals(email)){
                freeServers.put(serverName,freeServers.get(serverName)+1);
                rents.remove(id);
            } else {
                throw new NonExistingServerException("Server doesn't belong to this user\n");
            }

        }

    }

    public String rentedServers(String email) throws UserNotAuthenticatedException {
        if (email == null) {
            throw new UserNotAuthenticatedException("Not logged in\n");
        }

        synchronized (rents) {
            return rents.values().stream().filter(r -> r.getUser().getEmail().equals(email)).map(r -> r.getId() + ":" + r.getServer().getServerName()+"-"+r.getPricePerHour()).collect(Collectors.joining(" "));
        }
    }

    public String biddedAuctions(String email) throws UserNotAuthenticatedException {
        if (email == null) {
            throw new UserNotAuthenticatedException("Not logged in\n");
        }

        synchronized (auctions) {
            return auctions.values().stream().filter(r -> r.getHighestBidder().getEmail().equals(email)).map(r -> r.getId() + ":" + r.getServer().getServerName()).collect(Collectors.joining(" "));
        }
    }

    //Função que retorna os fundos correspondentes ao utilizador
    public float funds(String email) throws UserNotAuthenticatedException {
        if(email == null){
            throw new UserNotAuthenticatedException("Not logged in\n");
        }

        synchronized(users) {
            return users.get(email).getFunds();
        }
    }

    //Função server side que serve para criar um leilão
    public synchronized void createAuction(String serverType, float minimalBid, String email, int duration) throws NonExistingServerException{
        //TODO sincronizar
        if(!serverDetails.containsKey(serverType)){
            throw new NonExistingServerException("Server does not exist\n");
        }

        int auctionId = generateAuctionId();

        Auction auction = new Auction(auctionId,minimalBid,serverDetails.get(serverType),duration,this);
        auctions.put(auctionId,auction);

        new Thread(auction).start();

    }

    //Função que a classe Auction utiliza para atribuir um servidor a um utilizador e acabar com uma auction
    public int endAuction(int auctionId, float highestBid, String serverType, String email) throws NoBidException {

        //TODO sincronizar
        //TODO Dar synchronize aos hashmaps individualmente mas cuidado para evitar deadlock
        //TODO Adicionar o servidor ao utilizador
        //TODO Possibilidade de cancelar leilão em caso de não haver servidor disponível
        synchronized(freeServers) {

            if(auctions.get(auctionId).getHighestBidder() == null){
                auctions.remove(auctionId);
                throw new NoBidException("No user bid\n");
            }

            int rentId = generateRentId();
            User u = users.get(email);
            Rent rent = new Rent(rentId,1,highestBid,u,serverDetails.get(serverType));
            u.setFunds(u.getFunds()+rent.getPricePerHour());
            auctions.remove(auctionId);
            rents.put(rentId,rent);

            return rentId;
        }
    }

    public void cancelAuction(int auctionId){

        //TODO sincronizar
        if(!auctions.containsKey(auctionId)){
            System.out.println("Auction "+auctionId+" does not exist");
            return;
        }

        //TODO avisar os clients que a auction foi terminada

        auctions.get(auctionId).setTerminated(true);
        auctions.remove(auctionId);

    }

    //Gera um id novo para cada aluguer
    public int generateRentId(){
        if(rents.isEmpty()){
            return 1;
        }else {
            return Collections.max(rents.keySet()) + 1;
        }
    }

    public int generateAuctionId() {
        if (auctions.isEmpty()) {
            return 1;
        } else {
            return Collections.max(auctions.keySet()) + 1;
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

    public static void main (String[] args) {
        int port = 12345;
        Cloud cloud = new Cloud(port);

        Thread cloudThread = new Thread(cloud);

        cloudThread.start();

        try {
            cloudThread.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

}
