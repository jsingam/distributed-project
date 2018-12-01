import utils.Constants;

import javax.sql.DataSource;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Set;

public class HealthCheck implements Runnable {
    Client client;
    DatagramSocket datagramSocket;



    public HealthCheck(Client client, DatagramSocket datagramSocket) {
        this.client = client;
        this.datagramSocket=datagramSocket;
    }

    @Override
    public void run() {
        int k=0;
        int l=150;
        while (true) {
            Set<Neighbour> neighbours = null;
            try {
                neighbours = client.getNeighbourCopy();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            if(neighbours.size()==1 && k<0){
                client.findANeighbour();
                k=5;
            } else k--;
            if(neighbours.size()==0 && l<0 && client.isReg()){
                client.getQueryManager().manageQuery("UNREG");
                client.getQueryManager().manageQuery("REG");
                l=150;
            }
            else{
                l--;
            }
            in:
            for (Neighbour neighbour : neighbours) {


                String ip_port = neighbour.getIp() + "_" + neighbour.getPort();
                if(client.getHealth(ip_port)<20){
                    client.removeForceStoppedNeighbour(ip_port);
                    continue in;
                }
                client.reduceHealth(ip_port);
                String cmd = "HEALTH " + client.getIp() + " " + client.getPort();
                cmd = formatString(cmd);
                nodeCommand(cmd, neighbour.getIp(), neighbour.getPort());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }


    private String  nodeCommand(String nodeCmd,String ip, int port) {
        try {

            byte[] bytesToSend = nodeCmd.getBytes();
            InetAddress inetAddress = InetAddress.getByName(ip);
            DatagramPacket packet = new DatagramPacket(bytesToSend, bytesToSend.length, inetAddress, port);
            this.datagramSocket.send(packet);

//            while (!this.responseHandler.getIsValueSet()){}
//            this.responseHandler.setValueSet(false);
//            return this.responseHandler.getResponse();
        }catch (Exception e){
            System.out.println(e);

        }
        return "";
    }


    private String formatString(String currentString){
        int charactersInCurrentString = Integer.toString(currentString.length()+5).length();
        if(charactersInCurrentString < 4){
            int missingCharacters = 4 - charactersInCurrentString;
            String lengthString = "";
            for(int i=0; i<missingCharacters; i++){
                lengthString = lengthString.concat("0");
            }
            lengthString = lengthString.concat(Integer.toString(currentString.length()+5));
            return lengthString + " "+ currentString;
        }else{
            return Integer.toString(currentString.length()+5) + " " +currentString;
        }
    }
}
