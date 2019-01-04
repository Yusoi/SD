import Server.Cloud;

public class Main {

    public static void main (String[] args) {
        int port = 12345;
        Cloud cloud = new Cloud(port);

        Thread cloudThread = new Thread(cloud);

        cloudThread.start();

        try {
            cloudThread.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

}
