package Server;

import Client.User;

public class Rent {

    public int id;
    public int rentType; //0 é alugar direto(pagando o preço nominal) 1 é ir a leilão(bid)
    public User user; //Utilizador que alugou o servidor
    public Server server; //Servidor que foi alugado

    public Rent(int i, int r, User u, Server s){
        this.id = i;
        this.rentType = r;
        this.user = u;
        this.server = s;
    }

    public int getId() {
        return this.id;
    }

    public int getRentType() {
        return rentType;
    }

    public Server getServer() {
        return server;
    }

    public User getUser() {
        return user;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRentType(int rentType) {
        this.rentType = rentType;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
