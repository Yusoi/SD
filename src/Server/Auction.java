package Server;

import Exceptions.BidNotHighEnoughException;
import Exceptions.NoBidException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

public class Auction implements Runnable{

    private int id;
    private HashMap<User,Float> bidders;
    private float minimalBid;
    private Server server;
    private ReentrantLock lock, bidlock;
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
        this.bidlock = new ReentrantLock();
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

    public synchronized void addBidder(User bidder,float value) {
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
        bidlock.lock();
        try {
            for (Map.Entry<User, Float> entry : bidders.entrySet()) {
                if (highestBidder == null || entry.getValue().compareTo(highestBidder.getValue()) > 0) {
                    highestBidder = entry;
                }
            }
        }finally {
            bidlock.unlock();
        }


        try {
            return highestBidder.getKey();
        }catch(NullPointerException e){
            return null;
        }
    }

    public void newBid(User newBidder, float newValue) throws BidNotHighEnoughException {
            if(newValue >= minimalBid)
                this.addBidder(newBidder,newValue);
            else
                throw new BidNotHighEnoughException("Bid not high enough");
    }


    @Override
    public void run(){
        try {
            sleep(duration*1000);

            if(!this.terminated) {

                User highestBidder = this.getHighestBidder();
                lock.lock();
                try {
                    if (highestBidder != null)
                        cloud.endAuction(this.id, this.bidders.get(highestBidder), this.server.getServerName(), highestBidder.getEmail());
                }finally {
                    lock.unlock();
                }
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }catch(NoBidException e){
            System.out.println(e.toString()+" on auction "+this.id);
        }
    }
}
