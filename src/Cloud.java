import Client.User;
import Server.ExistingUserException;
import Server.Rent;
import Server.Server;
import Server.WrongCredentialsException;

import java.util.Collections;
import java.util.HashMap;

public class Cloud {

    public HashMap<String,User> users; //users ou clients again
    //Guardar Servidores
    public HashMap<String,Integer> freeServers;
    //Guarda rents por identificador
    public HashMap<Integer, Rent> rents;
    //String = ServerType
    public HashMap<String, Float> prices;

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


    public int rent(Float bid, String serverType, int rentType, User user, Server server){

        // como assim criar uma rent aqui?

        int id = generateRentId();

        Rent r = new Rent(id,rentType,user,server);

        return id;
    }

    public int generateRentId(){
        return Collections.max(rents.keySet());
    }



    /*Arrancar com os servidores e clientes*/
    public static void main(String[] args){

    }
}
