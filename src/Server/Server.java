package Server;

import java.util.concurrent.locks.Condition;

public class Server extends Thread{

    public String name;
    public Float precoNominal;

    public Server(String name, float preco){
        this.name = name;
        this.precoNominal = preco;
    }

    public String getServerName() {
        return name;
    }

    public Float getPrecoNominal() {
        return precoNominal;
    }

}
