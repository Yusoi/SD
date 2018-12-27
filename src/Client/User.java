package Client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class User {

    private String email;
    private String password;
    private float funds;

    // lista de servidores que o user possui atualmente com o ID de reserva associado
    private Map<Integer,String> reservationIDsType;

    ReentrantLock lockEmail, lockPass, lockFunds, lockIDs;

    public User(){
        this.email = null;
        this.password = null;
        this.funds = (float) 0;
        this.reservationIDsType = new HashMap<>();

        this.lockEmail = new ReentrantLock();
        this.lockPass = new ReentrantLock();
        this.lockFunds = new ReentrantLock();
        this.lockIDs = new ReentrantLock();
    }

    public User(String em, String pass, float fund, Map<Integer,String> ids){
        this.email = em;
        this.password = pass;
        this.funds = fund;

        this.reservationIDsType = new HashMap<>();
        for(Map.Entry<Integer,String> e : ids.entrySet()) {
            this.reservationIDsType.put(e.getKey(), e.getValue());
        }

        this.lockEmail = new ReentrantLock();
        this.lockPass = new ReentrantLock();
        this.lockFunds = new ReentrantLock();
        this.lockIDs = new ReentrantLock();
    }

    public float getFunds() {
        float r = 0;
        this.lockFunds.lock();
        r = this.funds;
        this.lockFunds.unlock();
        return r;
    }

    public String getEmail() {
        String s;
        this.lockEmail.lock();
        s = this.email;
        this.lockEmail.unlock();
        return s;
    }

    public String getPassword() {
        String s;
        this.lockPass.lock();
        s = this.password;
        this.lockPass.unlock();
        return s;
    }

    public Map<Integer,String> getReservationIDsType(){
        Map<Integer,String> aux = new HashMap<>();

        this.lockIDs.lock();
        for(Map.Entry<Integer,String> e : this.reservationIDsType.entrySet()) {
            aux.put(e.getKey(), e.getValue());
        }
        this.lockIDs.unlock();

        return aux;
    }

    public void setEmail(String email) {
        this.lockEmail.lock();
        this.email = email;
        this.lockEmail.unlock();
    }

    public void setFunds(Float funds) {
        this.lockFunds.lock();
        this.funds = funds;
        this.lockFunds.unlock();
    }

    public void setPassword(String password) {
        this.lockPass.lock();
        this.password = password;
        this.lockPass.unlock();
    }

    public void setReservationIDsType(Map<Integer,String> ids){
        this.lockIDs.lock();
        this.reservationIDsType.clear();
        for(Map.Entry<Integer,String> e : ids.entrySet()) {
            this.reservationIDsType.put(e.getKey(), e.getValue());
        }
        this.lockIDs.unlock();
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("User:\n");

        this.lockEmail.lock();
        sb.append("Email: ").append(this.email).append("\n");
        this.lockEmail.unlock();

        this.lockPass.lock();
        sb.append("Password: ").append(this.password).append("\n");
        this.lockPass.unlock();

        this.lockFunds.lock();
        sb.append("Funds: ").append(this.funds).append("\n");
        this.lockFunds.unlock();

        this.lockIDs.lock();
        sb.append("Reservation Ids: ").append(this.reservationIDsType.toString()).append("\n");
        this.lockIDs.unlock();

        return sb.toString();
    }

    public String getSpecificId(Integer id){
        String s;

        this.lockIDs.lock();
        s = this.reservationIDsType.get(id);
        this.lockIDs.unlock();

        return s;
    }

    public void putIdType(Integer id, String type){
        this.lockIDs.lock();
        this.reservationIDsType.put(id,type);
        this.lockIDs.unlock();
    }

    public void remId(Integer id){
        this.lockIDs.lock();
        this.reservationIDsType.remove(id);
        this.lockIDs.unlock();
    }


    /**
     * NOTA IMPORTANTE: os fundos são as dividas e não o dinheiro que o utilizador tem para gastar
     *
     * No enunciado diz: "Consultar a sua conta corrente. Um utilizador autenticado terá a possibilidade de consultar o valor
     * em dívida de acordo com a utilização dos recursos da plataforma de computação na nuvem."
     *
     * estas funções "addFunds" e "useFunds" nunca serão usadas, só se a "addFunds" for adicionar números negativos
     */

    public void addFunds(float funds) {
        this.lockFunds.lock();
        this.funds += funds;
        this.lockFunds.unlock();
    }

    //só invocar esta função se ele tiver fundos para isso.
    public void useFunds(float funds) {
        this.lockFunds.lock();
        this.funds -= funds;
        this.lockFunds.unlock();
    }
}
