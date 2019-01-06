package Server;

import Exceptions.NonExistingServerException;

import java.util.Scanner;

public class CloudMenuHandler implements Runnable{

    private Cloud cloud;

    public CloudMenuHandler(Cloud cloud){
        this.cloud = cloud;
    }

    private static void menu(){
        System.out.println("Options:\n\t- Start Auction\n\t- Cancel Auction\n");
    }

    private void startAuction(){
        String type = "";
        float minimalBid = 0;
        int duration = 0;

        Scanner s = new Scanner(System.in);
        System.out.println("Server catalogue: "+cloud.serverCatalogue());
        System.out.println("Server type:");
        if (s.hasNextLine()) {
            type = s.nextLine();
        }
        System.out.println("Minimal Bid:");
        if (s.hasNextLine()) {
            try {
                minimalBid = Float.parseFloat(s.nextLine());
            }catch(NumberFormatException e){
                System.out.println("Invalid number format");
                s.close();
                return;
            }
        }
        System.out.println("Duration(sec):");
        if (s.hasNextLine()) {
            try {
                duration = Integer.parseInt(s.nextLine());
            }catch(NumberFormatException e){
                System.out.println("Invalid number format");
                s.close();
                return;
            }
        }

        try {
            cloud.createAuction(type, minimalBid, duration);
        }catch(NonExistingServerException e){
            System.out.println("Server does not exist");
        }
        System.out.println("Auction started successfully");
    }

    private void cancelAuction(){
        String id = "";
        Scanner s = new Scanner(System.in);
        System.out.println("Auction catalogue: "+cloud.auctionCatalogue());
        System.out.println("Auction id:");
        if (s.hasNextLine()) {
            id = s.nextLine();
        }
        try{
            cloud.cancelAuction(Integer.parseInt(id));
        }catch(NumberFormatException e){
            System.out.println("Invalid number format");
            s.close();
            return;
        }
        System.out.println("Auction cancelled successfully");
    }

    @Override
    public void run(){
        Scanner s = new Scanner(System.in);
        String str = "";

        menu();

        if (s.hasNextLine()) {
            str = s.nextLine();
        }

        while(!cloud.isStopped()){
            switch(str){
                case "Start Auction":
                    startAuction();
                    break;
                case "Cancel Auction":
                    cancelAuction();
                    break;
                default:
                    System.out.println("Command does not exist");
            }

            menu();

            if (s.hasNextLine())
                str = s.nextLine();
        }
        s.close();
    }
}
