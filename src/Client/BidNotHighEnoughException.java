package Client;

public class BidNotHighEnoughException extends Exception {
    public BidNotHighEnoughException(String message) {
        super(message);
    }
}
