package Business;

import Exceptions.BidNotHighEnoughException;
import Exceptions.NotEnoughMoneyException;

import java.util.Random;

public class Bid {

    private User highestBidder; //user ou client? Estava client
    private float value;
    private Server server;

    public Bid(User bidder, float v, Server s){
        this.highestBidder = bidder;
        this.value = v;
        this.server = s;
    }

    public User getHighestBidder() {
        return highestBidder;
    }

    public float getValue() {
        return value;
    }

    public Server getServer() {
        return server;
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

    synchronized public void newBid(User newBidder, float newValue) throws BidNotHighEnoughException, NotEnoughMoneyException {

        if (newValue > value) {
            if(newBidder.getFunds() > newValue) {
                this.setHighestBidder(newBidder);
                this.setValue(newValue);
            }else{
                throw new NotEnoughMoneyException("O utilizador não tem fundos suficientes");
            }
        }else{
            throw new BidNotHighEnoughException("O valor não é superior que o valor atual licitado");
        }
    }

    synchronized public void closeBid(){
        Random random = new Random();
        Rent rent = new Rent(random.nextInt(), 1, this.highestBidder, this.server);
        this.highestBidder.useFunds(value);

    }
}