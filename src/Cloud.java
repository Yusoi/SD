import Client.Client;
import Client.User;
import Server.Rent;

import java.util.Collections;
import java.util.HashMap;

public class Cloud {

    public HashMap<String,Client> clients;
    //Guardar Servidores
    public HashMap<String,Integer> freeServers;
    //Guarda rents por identificador
    public HashMap<Integer, Rent> rents;
    //String = ServerType
    public HashMap<String, Float> prices;

    public void register(String email, String password){

    }

    public void login(String email, String password) {

    }

    public int rent(Float bid, String serverType, int rentType, User user){



        int id = generateRentId();

        Rent r = new Rent(id,rentType,user);

        return id;
    }

    public int generateRentId(){
        return Collections.max(rents.keySet());
    }



    /*Arrancar com os servidores e clientes*/
    public static void main(String[] args){

    }
}
