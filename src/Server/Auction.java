package Server;

import Exceptions.BidNotHighEnoughException;
import Exceptions.NoBidException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

public class Auction implements Runnable{

    private int id;
    private HashMap<User,Float> bidders;
    private float minimalBid;
    private Server server;
    private ReentrantLock lock;
    private LocalDateTime startingTime;
    private int duration; //Seconds until the auction ends
    private Cloud cloud;
    private boolean terminated;

    public Auction(int id, float v, Server s, int duration, Cloud cloud){
        this.id = id;
        this.bidders = new HashMap<>();
        this.minimalBid = v;
        this.server = s;
        this.lock = new ReentrantLock();
        this.startingTime = LocalDateTime.now();
        this.duration = duration;
        this.cloud = cloud;
        this.terminated = false;
    }

    public int getId(){return id;}

    public HashMap<User,Float> getBidders() {
        return new HashMap<>(bidders);
    }

    public float getMinimalBid() {
        return minimalBid;
    }

    public Server getServer() {
        return server;
    }

    public boolean isTerminated() { return terminated; }

    public void setId(int id){
        this.id = id;
    }

    public void addBidder(User bidder,float value) {
        this.bidders.put(bidder,value);
    }

    public void setMinimalBid(float value) {
        this.minimalBid = value;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void setTerminated(boolean terminated) { this.terminated = terminated; }

    public User getHighestBidder(){
        Map.Entry<User,Float> highestBidder = null;
        for(Map.Entry<User,Float> entry : bidders.entrySet()){
            if(highestBidder == null || entry.getValue().compareTo(highestBidder.getValue()) > 0){
                highestBidder = entry;
            }
        }

        return highestBidder.getKey();
    }

    public void newBid(User newBidder, float newValue) throws BidNotHighEnoughException {

        lock.lock();
        try {
            if(newValue >= minimalBid)
                this.addBidder(newBidder,newValue);
            else
                throw new BidNotHighEnoughException("Bid not high enough");
        }finally {
            lock.unlock();
        }
    }


    //TODO sincronizar a classe
    @Override
    public void run(){
        try {
            sleep(duration*1000);

            if(!this.terminated) {

                User highestBidder = this.getHighestBidder();

                if(highestBidder != null)
                    cloud.endAuction(this.id, this.bidders.get(highestBidder), this.server.getServerName(), highestBidder.getEmail());
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }catch(NoBidException e){
            System.out.println(e.toString()+" on auction "+this.id);
        }
    }
}
