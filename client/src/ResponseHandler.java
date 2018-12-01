import utils.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class ResponseHandler implements Runnable {

    private volatile String response;
    private volatile boolean isValueSet = false;


    private DatagramSocket socket;
    private String username;
    private Client client;
    private GUI gui;

    public ResponseHandler(DatagramSocket socket, String username, Client client,GUI gui) {
        this.socket = socket;
        this.username = username;
        this.client=client;
        this.gui=gui;
    }


    public boolean getIsValueSet(){
        return  isValueSet;
    }

    public  void setValueSet(boolean bool){
        isValueSet=bool;
    }

    public String getResponse(){
        return response;
    }

    public void setResponse(String response){
        this.response=response;
    }


    public Neighbour addToRoutingTable(String ip, int port){
        return client.addNeighbour(ip,port);

    }


    public Neighbour removeFromRouteTable(String ip, int port,boolean status){
        return client.removeNeighbour(ip,port,status);
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

    private void sendJoinStatus(Neighbour neighbour,String ip, int port){
        String joinResponse = "";
        DatagramPacket sendPacket;
        try {
            if (neighbour!=null) {
                InetAddress senderAddress = InetAddress.getByName(neighbour.getIp());
                int senderPort = neighbour.getPort();
                joinResponse = "JOINOK 0 ";
                byte[] joinStatus = formatString(joinResponse).getBytes();
                sendPacket = new DatagramPacket(joinStatus, joinStatus.length, senderAddress, senderPort);
            } else {
                InetAddress senderAddress = InetAddress.getByName(ip);
                int senderPort = port;
                joinResponse = "JOINOK 9999 ";
                byte[] joinStatus = formatString(joinResponse).getBytes();
                sendPacket= new DatagramPacket(joinStatus, joinStatus.length, senderAddress, senderPort);
            }


            this.socket.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.isValueSet = false;
            System.out.println("Client Command: ");
        }

    }

    private void sendUnJoinStatus(Neighbour neighbour,String ip, int port){
        String joinResponse = "";
        DatagramPacket sendPacket;
        try {
            if (neighbour!=null) {
                InetAddress senderAddress = InetAddress.getByName(neighbour.getIp());
                int senderPort = neighbour.getPort();
                joinResponse = "LEAVEOK 0 ";
                byte[] joinStatus = formatString(joinResponse).getBytes();
                sendPacket = new DatagramPacket(joinStatus, joinStatus.length, senderAddress, senderPort);
            } else {
                InetAddress senderAddress = InetAddress.getByName(ip);
                int senderPort = port;
                joinResponse = "LEAVEOK 9999 ";
                byte[] joinStatus = formatString(joinResponse).getBytes();
                sendPacket= new DatagramPacket(joinStatus, joinStatus.length, senderAddress, senderPort);
            }


            this.socket.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.isValueSet = false;
            System.out.println("Client Command: ");
        }

    }







    @Override
    public void run() {
        while (true) {
            isValueSet = false;
            byte[] responseFromServer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(responseFromServer, responseFromServer.length);
            try {
                this.socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.response = new String(packet.getData());
            gui.log("Received :  " + response);
//            System.out.println();
            String[] requestOnNode = this.response.split(" ");
            if (requestOnNode.length == 0) continue;
            requestOnNode[requestOnNode.length - 1] = requestOnNode[requestOnNode.length - 1].trim();
            String command = requestOnNode[1].trim();
//            System.out.println(command);
            if((!command.equals("REGOK")) && (!client.isReg())){
                continue;
            }

            if (command.equals("JOIN")) {
                System.out.println("Join");
                String requesterIp = requestOnNode[2].trim();
                for (String i : requestOnNode) {
                    System.out.println(i);
                }
                int requesterPort = Integer.parseInt(requestOnNode[3].trim());
                Neighbour neighbour = addToRoutingTable(requesterIp, requesterPort);
                sendJoinStatus(neighbour, requestOnNode[2], Integer.parseInt(requestOnNode[3].trim()));
//                System.out.println("Routing Table Size : " + Integer.toString(this.routingTable.size()));
//                System.out.println(this.routingTable);
//                System.out.println();
//                isValueSet = true;

//            }else if(command.equals("JOINOK")){
//                System.out.println(response);
//                String requesterIp = packet.getAddress().toString();
//                int requesterPort = packet.getPort();
//                String username = requestOnNode[3].trim();
//                String ipAndPort = requesterIp + " " + requesterPort;
////                addToRoutingTable(username, ipAndPort);
////                System.out.println("Routing Table Size : " + Integer.toString(this.routingTable.size()));
////                System.out.println(this.routingTable);
////                System.out.println();
//                isValueSet = true;

            } else if (command.equals("LEAVE")) {
                System.out.println("Leave");
                String requesterIp = requestOnNode[2].trim();
                int requesterPort = Integer.parseInt(requestOnNode[3].trim());
                Neighbour neighbour = removeFromRouteTable(requesterIp, requesterPort, true);
                sendUnJoinStatus(neighbour, requestOnNode[2], Integer.parseInt(requestOnNode[3].trim()));
            } else if (command.equals("SER")) {
                System.out.println("search");
                Neighbour neighbour = new Neighbour(packet.getAddress().getHostAddress(), packet.getPort());
                processSearch(requestOnNode, neighbour);

            } else if (command.equals("SEROK")) {
                processSearchResults(requestOnNode);

            } else if (command.equals("JOINOK")) {
                if (response.split(" ")[2].trim().equals("0")) {
                    System.out.println("Join Network Successful");
                    addToRoutingTable(packet.getAddress().getHostAddress(), packet.getPort());
                } else {
                    System.out.println("Error while joining network");
                }
            } else if (command.equals("UNROK")) {
                if (requestOnNode[2].equals("0")) {
                    processUNROK(true);
                    gui.processUNREGOK();
                    client.setReg(false);

                } else {
                    processUNROK(false);
                }

            } else if (command.equals("REGOK")) {
                processREGOK(response);
                gui.processREGOK();
                client.setReg(true);
            } else if (command.equals("HEALTH")) {
                processHEALTH(requestOnNode);
            } else if (command.equals("HEALTHOK")) {
                processHEALTHOK(requestOnNode);
            } else if (command.equals("FIND")) {
                System.out.println(response);
                processFIND(requestOnNode);
            } else {
//                System.out.println("No method specified for command "+ command);
            }

        }

    }

    private void processFIND(String[] requestOnNode) {
        System.out.println(requestOnNode[requestOnNode.length-1]);
        if(requestOnNode[requestOnNode.length-1].equals("0")){
            replyFIND(requestOnNode);
        }
        else{
            String cmd="";
            for(int i=1;i<requestOnNode.length-1;i++){
                cmd+=(requestOnNode[i]+" ");
            }
            cmd+=Integer.parseInt(requestOnNode[requestOnNode.length-1])-1;
            List<Neighbour> neighbours = new ArrayList<>(client.getNeighbours());
            if(!(neighbours.size()<2)){
                int rand= new Random().nextInt(neighbours.size());
                Neighbour neighbour= neighbours.get(rand);
                cmd=formatString(cmd);
                System.out.println(cmd+ " " +neighbour.toString());
                nodeCommand(cmd,neighbour.getIp(),neighbour.getPort());
            } else {
                replyFIND(requestOnNode);
            }


        }

    }

    private void replyFIND(String[] requestOnNode) {
        Neighbour neighbour = new Neighbour(requestOnNode[2],Integer.parseInt(requestOnNode[3]));
        if((!client.getNeighbours().contains(neighbour)) && (!neighbour.equals(new Neighbour(client.getIp(),Integer.parseInt(client.getPort()))))){
            joinToNode(neighbour.getIp(),neighbour.getPort());
        }
        else{

        }
    }

    private void processHEALTHOK(String[] requestOnNode) {
        client.addHealth(requestOnNode[2]+"_"+requestOnNode[3]);
    }

    private void processHEALTH(String[] response) {
        String cmd="HEALTHOK "+client.getIp()+" "+client.getPort();
        cmd=formatString(cmd);
        nodeCommand(cmd,response[2],Integer.parseInt(response[3]));
    }

    private void processUNROK(boolean result) {
        if(result){
            unJoin();
            client.setNeighbours(new HashSet<>());
            gui.updateTable(new HashSet<>());
        }

    }

    private  void processREGOK(String response){
        if(response.isEmpty()){
            System.out.println("Error while sending server command to register");
        }else {
            int activeNodeCount = Integer.parseInt(response.split(" ")[2].trim());
            if(activeNodeCount == 0){
                System.out.println("This is the first node in the network");
            }else if(activeNodeCount == 9999){
                System.out.println("Failed, There is some error in the command");
            }else if(activeNodeCount == 9998){
                System.out.println("Failed, Client already registered, Unregister first");
            }else if(activeNodeCount == 9997){
                System.out.println("Failed, Registered to another user, try a different IP and port");
            }else if(activeNodeCount == 9996) {
                System.out.println("Failed, Cannot Register, BS Full");
            }else{
                System.out.println("More than 1 Nodes in the Network");
                String[] serverResponse = response.split(" ");
                for(int i=0; i<=activeNodeCount; i+=2){
                    String nodeIpaddress = serverResponse[3+i].trim();
                    try {
                        int nodePort = Integer.parseInt(serverResponse[4 + i].trim());
                        joinToNode(nodeIpaddress, nodePort);
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                }
            }
        }
    }
    private void processSearchResults(String[] requestOnNode) {
        if(!requestOnNode[2].equals("0")){
            client.getQueryManager().updateResults(requestOnNode);

        }
    }

    private void processSearch(String[] requestOnNode, Neighbour sender) {
        int hop = Integer.parseInt(requestOnNode[requestOnNode.length-1].trim());
        int limit= Constants.HOP_LIMIT;
        String cmd="";
        if(hop<limit){
            for (int i=1;i<requestOnNode.length-1;i++){
                cmd+=requestOnNode[i]+" ";
            }
            cmd+=String.valueOf(hop+1);
            cmd=formatString(cmd);
            Random rand = new Random();
            Neighbour neighbour;
            Neighbour source= new Neighbour(requestOnNode[2],Integer.parseInt(requestOnNode[3]));
            for(int i=0;i<10;i++) {
                int n = rand.nextInt(client.getNeighbours().size());
                neighbour= new ArrayList<>(client.getNeighbours()).get(n);
                if(!( neighbour.equals(sender) || neighbour.equals(source))){
                    nodeCommand(cmd, neighbour.getIp(), neighbour.getPort());
                    break;
                }
            }
            List<String> result=client.searchFiles(cmd.split("\"")[1]);
            sendResults(result,requestOnNode[2],Integer.parseInt(requestOnNode[3]),hop);

        }

    }

    private void sendResults(List<String> result, String ip, int port,int hop) {
        String cmd="";
        if (result.isEmpty()){
            cmd= "SEROK 0 "+client.getIp()+" "+String.valueOf(client.getTcpPort())+" "+String.valueOf(hop);
        }
        else {
            cmd= "SEROK "+String.valueOf(result.size())+" "+client.getIp()+" "+String.valueOf(client.getTcpPort())+" "+String.valueOf(hop);
            for (String file:result){
                cmd+=(" "+file.replace(" ","_"));
            }
        }
        nodeCommand(formatString(cmd),ip,port);
    }


    private String  nodeCommand(String nodeCmd,String ip, int port) {
        try {

            byte[] bytesToSend = nodeCmd.getBytes();
            InetAddress inetAddress = InetAddress.getByName(ip);
            DatagramPacket packet = new DatagramPacket(bytesToSend, bytesToSend.length, inetAddress, port);
            this.socket.send(packet);
            gui.log("Sent Request to NODE :  "+nodeCmd);
            return "";

//            while (!this.responseHandler.getIsValueSet()){}
//            this.responseHandler.setValueSet(false);
//            return this.responseHandler.getResponse();
        }catch (Exception e){
            System.out.println(e);
            return "";
        }
    }

    private void joinToNode(String ipAddress, int port){
        this.setResponse("");
        String joinString = "JOIN " + client.getIp() + " " + client.getPort();
        joinString = formatString(joinString);
        String joinResponse = nodeCommand(joinString, ipAddress, port);
//        if(joinResponse.split(" ")[2].trim().equals("0")) {
//            System.out.println("Join Network Successful");
//            addToRoutingTable(ipAddress,port);
//
//            this.responseHandler.setValueSet(false);
//        }else{
//            System.out.println("Error while joining network");
//            this.responseHandler.setValueSet(false);
//        }
    }

    public void unJoin(){
        this.setResponse("");
        String unJoinCmd = "LEAVE "+ client.getIp() + " " + client.getPort();
        unJoinCmd = formatString(unJoinCmd);
        for (Neighbour neighbour: client.getNeighbours()) {
            System.out.println(unJoinCmd);
            nodeCommand(unJoinCmd,neighbour.getIp(),neighbour.getPort());
//            if (joinResponse.split(" ")[2].trim().equals("0")) {
//                System.out.println("Join Network Successful");
//                addToRoutingTable(ipAddress, port);
//
//                this.responseHandler.setValueSet(false);
//            } else {
//                System.out.println("Error while joining network");
//                this.responseHandler.setValueSet(false);
//            }
        }

    }

    public GUI getGui() {
        return gui;
    }

    public void setGui(GUI gui) {
        this.gui = gui;
    }
}
