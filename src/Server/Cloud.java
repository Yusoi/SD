package Server;

import Exceptions.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.concurrent.locks.ReentrantLock;

public class Cloud implements Runnable{

    //Guardar utilizadores
    private HashMap<String,User> users; //users ou clients again
    //Guardar número de servidores livres de cada tipo
    private HashMap<String,Integer> freeServers;
    //Guarda detalhes sobre um certo tipo de servidor
    private HashMap<String,Server> serverDetails;
    //Guarda rents por identificador
    private HashMap<Integer, Rent> rents;
    //Guarda os leilões ativos
    private HashMap<Integer, Auction> auctions;
    //Guarda preços por tipo de servidor, String = ServerType

    private ReentrantLock userlock, freelock, detaillock, rentlock, auctionlock;

    //Port da ServerSocket
    private int port;
    //O ServerSocket em si
    private ServerSocket serverSocket = null;
    //Server está parado ou não
    private boolean isStopped = false;


    public Cloud(int port){
        this.port = port;
        this.users = new HashMap<>();
        this.freeServers = new HashMap<>();
        this.serverDetails = new HashMap<>();
        this.rents = new HashMap<>();
        this.auctions = new HashMap<>();
        this.userlock = new ReentrantLock();
        this.freelock = new ReentrantLock();
        this.detaillock = new ReentrantLock();
        this.rentlock = new ReentrantLock();
        this.auctionlock = new ReentrantLock();

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

    public boolean isStopped(){
        return isStopped;
    }

    // mandar por socket ao cliente a dizer erro ou certo
    public boolean register(String email, String password) throws ExistingUserException {
        userlock.lock();
        try{
            if (!users.containsKey(email)) {
                User user = new User(email, password, (float) 0);
                users.put(email, user);
            } else throw new ExistingUserException("O utilizador já existe\n");
        }finally {
            userlock.unlock();
        }
        return true; //na "interface", basta ver se é true para imprimir "registado com sucesso" ou algo do genero.
    }

    //Função que realiza o login do utilizador
    public boolean login(String email, String password) throws WrongCredentialsException {
        userlock.lock();
        try {
            if (users.containsKey(email) && users.get(email).getPassword().equals(password)) {
                return true;
            } else
                throw new WrongCredentialsException("Nao existe nenhum utilizador com essa combinação de email e password\n");
        }finally {
            userlock.unlock();
        }
    }

    public String serverCatalogue() {
        freelock.lock();
        try {
            return freeServers.entrySet().stream().filter(e -> e.getValue() > 0).map(e -> e.getKey()).collect(Collectors.joining(" "));
        }finally {
            freelock.unlock();
        }
    }

    //Função que arrenda um servidor a um utilizador pelo preço nominal
    public int order(String serverType, String email) throws NonExistingServerException, UserNotAuthenticatedException {
        if(email == null){
            throw new UserNotAuthenticatedException("Not logged in\n");
        }

        //TODO Dar synchronize aos hashmaps individualmente mas cuidado para evitar deadlock
        freelock.lock();
        try {
            if (!freeServers.containsKey(serverType)) {
                throw new NonExistingServerException("Server Nonexistent\n");
            }
            if (freeServers.get(serverType) == 0) {
                Optional<Auction> a = auctions.values().stream().filter(e -> e.getServer().getServerName().equals(serverType)).findAny();

                if (a.isPresent()) {
                    cancelAuction(a.get().getId());
                } else {
                    throw new NonExistingServerException("Not enough servers of that type\n");
                    }
                } else {
                freeServers.put(serverType, freeServers.get(serverType) - 1);
                }
            }finally {
                freelock.unlock();
            }

        rentlock.lock();
        try{
            int rentId = generateRentId();
            User u = users.get(email);
            Rent rent = new Rent(rentId,0,serverDetails.get(serverType).getPrecoNominal(),u,serverDetails.get(serverType));
            u.setFunds(u.getFunds()+rent.getPricePerHour());
            rents.put(rentId,rent);

            return rentId;
        }finally {
            rentlock.unlock();
        }
    }

    public String auctionCatalogue() {
        auctionlock.lock();
        try{
            return auctions.values().stream().map(e -> Integer.toString(e.getId())+" - "+e.getServer().getServerName()+" - Minimal Bid: "+e.getMinimalBid()).collect(Collectors.joining(" "));
        }finally {
            auctionlock.unlock();
        }
    }

    //Função que deixa ao utilizador licitar num leilão
    public int auction(int auctionId, float bid, String email) throws NonExistingServerException, UserNotAuthenticatedException, BidNotHighEnoughException {
        if(email == null){
            throw new UserNotAuthenticatedException("Not logged in\n");
        }

        Auction a;
        User u;

        auctionlock.lock();
        try{
            if(auctions.containsKey(auctionId)) {
                a = auctions.get(auctionId);
            } else {
                throw new NonExistingServerException("Auction does not exist\n");
            }
        }finally {
            auctionlock.unlock();
        }

        u = users.get(email);

        try {
            a.newBid(u, bid);
        }catch(BidNotHighEnoughException e) {
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
            Rent rent = rents.get(id);
        synchronized (rent) {
            if (rent == null) {
                throw new NonExistingServerException("Server Nonexistent\n");
            }

            String serverName = rent.getServer().getServerName();

            if (rent.getUser().getEmail().equals(email)) {
                freelock.lock();
                freeServers.put(serverName, freeServers.get(serverName) + 1);
                freelock.unlock();
                rentlock.lock();
                rents.remove(id);
                rentlock.unlock();
            } else {
                throw new NonExistingServerException("Server doesn't belong to this user\n");
            }
        }
    }

    public String rentedServers(String email) throws UserNotAuthenticatedException {
        if (email == null) {
            throw new UserNotAuthenticatedException("Not logged in\n");
        }

        rentlock.lock();
        try {
            return rents.values().stream().filter(r -> r.getUser().getEmail().equals(email)).map(r -> r.getId() + ":" + r.getServer().getServerName()+"-"+r.getPricePerHour()).collect(Collectors.joining(" "));
        }finally {
            rentlock.unlock();
        }
    }

    public String biddedAuctions(String email) throws UserNotAuthenticatedException {
        if (email == null) {
            throw new UserNotAuthenticatedException("Not logged in\n");
        }

        auctionlock.lock();
        try {
            return auctions.values().stream().filter(r -> r.getHighestBidder().getEmail().equals(email)).map(r -> r.getId() + ":" + r.getServer().getServerName()).collect(Collectors.joining(" "));
        }finally {
            auctionlock.unlock();
        }
    }

    //Função que retorna os fundos correspondentes ao utilizador
    public float funds(String email) throws UserNotAuthenticatedException {
        if(email == null){
            throw new UserNotAuthenticatedException("Not logged in\n");
        }

        userlock.lock();
        try {
            return users.get(email).getFunds();
        }finally {
            userlock.unlock();
        }
    }

    //Função server side que serve para criar um leilão
    public synchronized void createAuction(String serverType, float minimalBid, int duration) throws NonExistingServerException{
        //TODO sincronizar
        detaillock.lock();
        try {
            if (!serverDetails.containsKey(serverType) || freeServers.get(serverType) <= 0) {
                throw new NonExistingServerException("Server does not exist\n");
            }
        }finally {
            detaillock.unlock();
        }

        int auctionId = generateAuctionId();

        Auction auction = new Auction(auctionId, minimalBid, serverDetails.get(serverType), duration, this);
        auctionlock.lock();
        auctions.put(auctionId, auction);
        auctionlock.unlock();

        new Thread(auction).start();

    }

    //Função que a classe Auction utiliza para atribuir um servidor a um utilizador e acabar com uma auction
    public int endAuction(int auctionId, float highestBid, String serverType, String email) throws NoBidException {

        //TODO sincronizar
        //TODO Dar synchronize aos hashmaps individualmente mas cuidado para evitar deadlock
        //TODO Adicionar o servidor ao utilizador
        //TODO Possibilidade de cancelar leilão em caso de não haver servidor disponível
        auctionlock.lock();
        try {
            if (auctions.get(auctionId).getHighestBidder() == null) {
                auctions.remove(auctionId);
                throw new NoBidException("No user bid\n");
            }
        }finally {
            auctionlock.unlock();
        }

        int rentId = generateRentId();
            User u = users.get(email);
            synchronized (u) {
                Rent rent = new Rent(rentId, 1, highestBid, u, serverDetails.get(serverType));
                u.setFunds(u.getFunds() + rent.getPricePerHour());
                auctionlock.lock();
                auctions.remove(auctionId);
                auctionlock.unlock();
                rentlock.lock();
                rents.put(rentId, rent);
                rentlock.unlock();
            }

            return rentId;
    }

    public void cancelAuction(int auctionId){

        auctionlock.lock();
        try {
            if (!auctions.containsKey(auctionId)) {
                System.out.println("Auction " + auctionId + " does not exist");
                return;
            }
            //TODO avisar os clients que a auction foi terminada

            auctions.get(auctionId).setTerminated(true);
            auctions.remove(auctionId);
        }finally {
            auctionlock.unlock();
        }
    }

    //Gera um id novo para cada aluguer
    public int generateRentId(){
        rentlock.lock();
        try {

            if (rents.isEmpty()) {
                return 1;
            } else {
                return Collections.max(rents.keySet()) + 1;
            }
        }finally {
            rentlock.unlock();
        }
    }

    public int generateAuctionId() {
        auctionlock.lock();
        try {
            if (auctions.isEmpty()) {
                return 1;
            } else {
                return Collections.max(auctions.keySet()) + 1;
            }
        }finally {
            auctionlock.unlock();
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
        CloudMenuHandler cmh = new CloudMenuHandler(cloud);

        Thread cloudThread = new Thread(cloud);
        Thread cmhThread = new Thread(cmh);

        cloudThread.start();
        cmhThread.start();

        try {
            cloudThread.join();
            cmhThread.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

}
