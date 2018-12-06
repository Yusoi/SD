package Server;

import java.util.concurrent.locks.Condition;

public class Server extends Thread{

    public String id;
    public Float precoNominal;
    public Bid leilao;
    public Server.Rent servico;
    public Condition nonOcupated;

    public Server(String id,Float preco){

    }
}
