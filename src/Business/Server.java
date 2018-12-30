package Business;

import Business.Bid;
import Business.Rent;

import java.util.concurrent.locks.Condition;

public class Server extends Thread{

    public String serverId;
    public Float precoNominal;
    public Bid leilao;
    public Rent servico;
    public Condition nonOcupated;

    public Server(String id,Float preco){
        this.serverId = id;
        this.precoNominal = preco;
    }

    public String getServerId() {
        return serverId;
    }

    public Float getPrecoNominal() {
        return precoNominal;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

}
