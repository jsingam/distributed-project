import utils.Constants;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class ClientIO {

    private static Client client;
    private static QueryManager queryManager;
    private static ResponseHandler responseHandler;
    private static Thread responseHandlerThread;
    private static Thread healthCheckThread;
    private static HealthCheck healthCheck;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the Boot Address");
        String boot = scanner.nextLine();
        Constants.BOOTSTRAP_IP=boot;
        System.out.println("Enter ip");
        String ip = scanner.nextLine();
//        String ip = "10.10.26.90";
        System.out.println("Enter the port number :");
        String port = scanner.nextLine();
        System.out.println("Enter the TCP port number :");
        String tcpPort = scanner.nextLine();
        System.out.println("Enter name :");
        String name = scanner.nextLine();
//        String name ="singam";
        GUI gui = new GUI(ip+":"+port);
        client = new Client(ip,port,name,tcpPort,gui);
        DatagramSocket datagramSocket= new DatagramSocket(Integer.parseInt(port));

        responseHandler = new ResponseHandler(datagramSocket,name,client,gui);
        Server server= new Server(Integer.parseInt(tcpPort));
        Thread tcpThread = new Thread(server);
        tcpThread.start();
        responseHandlerThread = new Thread(responseHandler);
        responseHandlerThread.start();
        queryManager= new QueryManager(datagramSocket, responseHandler,client,gui);
        healthCheck = new HealthCheck(client,datagramSocket);
        healthCheckThread= new Thread(healthCheck);
        healthCheckThread.start();
        gui.setVisible(true);
        getQuery();


    }
    public static void getQuery(){
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.println("Enter the query");
            String query = scanner.nextLine();
            queryManager.manageQuery(query);
        }
    }

    public static Client getClient(){
        return client;
    }
}
