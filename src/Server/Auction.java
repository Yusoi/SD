package Server;

import Business.Rent;
import Business.Server;
import Business.User;
import Exceptions.BidNotHighEnoughException;
import Exceptions.NoBidException;
import Exceptions.NonExistingServerException;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

public class Auction implements Runnable{

    private int id;
    private User highestBidder; //user ou client? Estava client
    private float value;
    private Server server;
    private ReentrantLock lock;
    private LocalDateTime startingTime;
    private int duration; //Seconds until the auction ends
    private Cloud cloud;

    public Auction(int id, float v, Server s, int duration, Cloud cloud){
        this.id = id;
        this.highestBidder = null;
        this.value = v;
        this.server = s;
        this.lock = new ReentrantLock();
        this.startingTime = LocalDateTime.now();
        this.duration = duration;
        this.cloud = cloud;
    }

    public int getId(){return id;}

    public User getHighestBidder() {
        return highestBidder;
    }

    public float getValue() {
        return value;
    }

    public Server getServer() {
        return server;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setHighestBidder(User highestBidder) {
        this.highestBidder = highestBidder;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    //TODO dar fix
    public void newBid(User newBidder, float newValue) throws BidNotHighEnoughException {
        lock.lock();
        try {
            if (newValue > value) {
                if (newBidder.getFunds() > newValue) {
                    this.setHighestBidder(newBidder);
                    this.setValue(newValue);
                }
            } else {
                throw new BidNotHighEnoughException("O valor não é superior que o valor atual licitado");
            }
        }finally {
            lock.unlock();
        }
    }


     public void closeAuction(){
        Random random = new Random();
        Rent rent = new Rent(random.nextInt(), 1, value , this.highestBidder, this.server);
        lock.lock();
        this.highestBidder.useFunds(value);
        lock.unlock();
    }

    @Override
    public void run(){
        try {
            sleep(duration);
            cloud.endAuction(this.id,this.value,this.server.getServerName(),highestBidder.getEmail());
        }catch(InterruptedException e){
            e.printStackTrace();
        }catch(NoBidException e){
            System.out.println(e.toString()+" on auction "+this.id);
        }
    }
}
