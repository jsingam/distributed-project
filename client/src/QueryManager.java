import utils.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class QueryManager {
    private DatagramSocket clientSocket;
    private ResponseHandler responseHandler;
    private Client client;
    private Map<String, Set<String>> queryResult;
    private String lastQuery;
    private GUI gui;

    public QueryManager(DatagramSocket clientSocket, ResponseHandler responseHandler, Client client,GUI gui) {
        this.clientSocket = clientSocket;
        this.responseHandler=responseHandler;
        this.client=client;
        client.setQueryManager(this);
        this.gui=gui;
        gui.setQueryManager(this);
    }

    public void manageQuery(String query){
        if (query.equals("REG")){
            registerOnServer();
        } else if (query.equals("UNREG")){
            unregisterFromServer();
        } else if(query.equals("TABLE")){
            client.printNeighbours();
        }else if(query.equals("FILES")){
            client.printFiles();
        }else if(query.equals("HEALTH")){
            client.printHealth();
        }else if(query.split(" ")[0].equals("SEARCH")){
            if(query.split("\"").length>1){
                String filename = query.split("\"")[1];
                searchFile(filename);
            }else {
                System.out.println("Files command : FILES \"FILE_NAME\"");
            }
        }else System.out.println("Command not Found");

    }

    private void searchFile(String filename) {
        gui.setResultBlank();
        queryResult=new HashMap<>();
        String searchCmd = "SER " + client.getIp() + " " + client.getPort() + " \"" + filename+"\" 0";
        searchCmd=formatString(searchCmd);
        lastQuery =searchCmd;
        Random rand = new Random();
        int n = rand.nextInt(client.getNeighbours().size());
        Neighbour neighbour=new ArrayList<>(client.getNeighbours()).get(n);
        nodeCommand(searchCmd,neighbour.getIp(),neighbour.getPort());
    }

    public void updateResults(String[] result){
        String ip_port=result[3]+"_"+result[4];
        int noFiles= Integer.parseInt(result[2]);
        for(int i=0;i<noFiles;i++){
            String file=result[6+i];
            if(queryResult.containsKey(file)){
                queryResult.get(file).add(ip_port);
            } else{
                Set<String> temp=new HashSet<>();
                temp.add(ip_port);
                queryResult.put(file,temp);
            }
        }
        gui.updateResult(queryResult);
        printQueryResult();

    }

    private void printQueryResult(){
        System.out.println("=================================================================");
        System.out.println("Query : "+lastQuery);
        System.out.println("====================RESULTS======================================");
        for (String file:queryResult.keySet()){
            System.out.println(file+"   :     "+queryResult.get(file).toString());
        }
        System.out.println("=================================================================");
    }

    private void registerOnServer(){
        String regCmd = "REG " + client.getIp() + " " + client.getPort() + " " + client.getName();
        serverCommand(formatString(regCmd));

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

    private void serverCommand(String serverCmd) {
        try {
            byte[] bytesToSend = serverCmd.getBytes();
            InetAddress inetAddress = InetAddress.getByName(Constants.BOOTSTRAP_IP);
//            byte[] address = {10, 10, (byte) 9, (byte) 33};
//            InetAddress inetAddress = InetAddress.getByAddress(address);
            DatagramPacket datagramPacket = new DatagramPacket(bytesToSend, bytesToSend.length, inetAddress, Constants.BOOSTRAP_PORT);
            this.clientSocket.send(datagramPacket);
            gui.log("Sent Request to SERVER :  "+serverCmd);
            System.out.println(Constants.BOOTSTRAP_IP);
            System.out.println(Constants.BOOSTRAP_PORT);

//            while (!this.responseHandler.getIsValueSet()){}
//            this.responseHandler.setValueSet(false);
//            return this.responseHandler.getResponse();
        }catch (Exception e){
            System.out.println(e);
        }
    }


//    public Neighbour addToRoutingTable(String ip, int port){
//        return client.addNeighbour(ip,port);
//
//    }

    private String  nodeCommand(String nodeCmd,String ip, int port) {
        try {

            byte[] bytesToSend = nodeCmd.getBytes();
            InetAddress inetAddress = InetAddress.getByName(ip);
            DatagramPacket packet = new DatagramPacket(bytesToSend, bytesToSend.length, inetAddress, port);
            this.clientSocket.send(packet);
            gui.log("Sent Request to NODE :  "+nodeCmd);

//            while (!this.responseHandler.getIsValueSet()){}
//            this.responseHandler.setValueSet(false);
//            return this.responseHandler.getResponse();
        }catch (Exception e){
            System.out.println(e);

        }
        return "";
    }

    private void unregisterFromServer(){
        String unregCmd = "UNREG " + client.getIp() + " " + client.getPort() + " " + client.getName();
        serverCommand(formatString(unregCmd));

    }


    public void findANeighbour() {
        String cmd = "FIND " + client.getIp() + " " + client.getPort() + " " + new Random().nextInt(10);
        cmd=formatString(cmd);
        List<Neighbour> neighbours = new ArrayList<>(client.getNeighbours());
        if(! neighbours.isEmpty()) {
            int i= new Random().nextInt(neighbours.size());
            Neighbour neighbour = neighbours.get(i);
            nodeCommand(cmd,neighbour.getIp(),neighbour.getPort());
        }
    }

    public void findANeighbour(String ip, int port) {
        String cmd = "FIND " + client.getIp() + " " + client.getPort() + " " + new Random().nextInt(10);
        cmd=formatString(cmd);
        nodeCommand(cmd,ip,port);
    }

    public void getFile(String ip,int port,String file) throws IOException {
        ClientTCP.downloadFile(ip,port,file);
        client.addFile(file);
    }
}
