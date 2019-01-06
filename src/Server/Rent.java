package Server;

public class Rent {

    public int id;
    public int rentType; //0 é alugar direto(pagando o preço nominal) 1 é ir a leilão(auction)
    public float pricePerHour;
    public User user; //Utilizador que alugou o servidor
    public Server server; //Servidor que foi alugado

    public Rent(int i, int r, float pph, User u, Server s){
        this.id = i;
        this.rentType = r;
        this.pricePerHour = pph;
        this.user = u;
        this.server = s;
    }

    public int getId() {
        return this.id;
    }

    public int getRentType() {
        return rentType;
    }

    public float getPricePerHour() { return pricePerHour; }

    public Server getServer() {
        return server;
    }

    public User getUser() {
        return user;
    }

    public synchronized void setId(int id) {
        this.id = id;
    }

    public synchronized void setRentType(int rentType) {
        this.rentType = rentType;
    }

    public synchronized void setPricePerHour(int pricePerHour) { this.pricePerHour = pricePerHour; }

    public synchronized void setServer(Server server) {
        this.server = server;
    }

    public synchronized void setUser(User user) {
        this.user = user;
    }
}
