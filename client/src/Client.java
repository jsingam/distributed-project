import utils.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class Client {

    private Client client;
    private String ip;
    private String port;
    private String tcpPort;
    private String name;
    private Set<Neighbour> neighbours = new HashSet<>();
    private List<String> files;
    private QueryManager queryManager;
    private Map<String, Integer> health= new HashMap<>();

    public String getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(String tcpPort) {
        this.tcpPort = tcpPort;
    }

    public boolean isReg() {
        return reg;
    }

    public void setReg(boolean reg) {
        this.reg = reg;
    }

    private boolean reg;

    public GUI getGui() {
        return gui;
    }

    public void setGui(GUI gui) {
        this.gui = gui;
    }

    GUI gui;

    public Client(String ip, String port, String name,String tcpPort,GUI gui) throws IOException {
        this.ip = ip;
        this.port = port;
        this.name = name;
        this.gui=gui;
        this.tcpPort=tcpPort;

        files=new ArrayList<>();
        for (int i=0;i<5;i++){
            Random rand = new Random();

            int n = rand.nextInt(19);
            files.add(Constants.FILES[n]);
//            File fileFrom= new File("allfiles/"+Constants.FILES[n]);
//            File fileTo = new File("files/"+Constants.FILES[n]);
            Files.copy(Paths.get("allfiles/"+Constants.FILES[n]), Paths.get("files/"+Constants.FILES[n]), StandardCopyOption.REPLACE_EXISTING);
        }
        gui.updateFiles(files);
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Neighbour> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(Set<Neighbour> neighbours) {
        this.neighbours = neighbours;

    }

    public synchronized Neighbour addNeighbour(String ip, int port){
        Neighbour neighbour = new Neighbour(ip,port);
        if (neighbours.contains(neighbour)){
            return null;
        }
        neighbours.add(neighbour);
        gui.updateTable(neighbours);
        System.out.println(neighbours.toString());
        return neighbour;
    }
    public Neighbour removeNeighbour(String ip, int port, boolean status){
        Main.main(null);
        Neighbour tempneighbour = new Neighbour(ip,port);
        Neighbour neighbourToRemove=null;
        if(neighbours.size()<5 && new Random().nextInt(3)==1 && status) {
            queryManager.findANeighbour(ip, port);
        }
        for (Neighbour neighbour:neighbours){
            if (neighbour.equals(tempneighbour)){
                neighbourToRemove=neighbour;
                neighbours.remove(neighbourToRemove);
                break;
            }
        }
        if(neighbours.size()<2 || (new Random().nextInt(5)==3)){
            findANeighbour();
        }
        gui.updateTable(neighbours);
        return neighbourToRemove;


    }

    public void printNeighbours(){
        for(Neighbour neighbour:neighbours){
            System.out.println(neighbour.toString());
        }
    }

    public void printFiles(){
        for(String file:files){
            System.out.println(file);
        }
    }

    public List<String> searchFiles(String file){
        List<String> results= new ArrayList<>();
        String[] searchWords = file.split(" ");
        outerloop:
        for (String f:files){
            String[] temp=f.split(" ");
            List<String> words = new ArrayList<>(Arrays.asList(temp));
            innerloop:
            for(String token: searchWords) {
                if (! words.contains(token)){
                    continue outerloop;
                }
            }
            results.add(f);

        }
        return results;
    }


    public QueryManager getQueryManager() {
        return queryManager;
    }

    public void setQueryManager(QueryManager queryManager) {
        this.queryManager = queryManager;
    }

    public void addHealth(String ip_port){
        health.put(ip_port,100);
    }
    public void reduceHealth(String ip_port){
        //solve reg and unreg
        if(health.containsKey(ip_port)){

            health.put(ip_port,health.get(ip_port)-20);
        }
    }

    public void printHealth(){
        for(String ip_port:health.keySet()){
            System.out.println(ip_port.replace("_",":")+"  "+health.get(ip_port));
        }
    }

    public void removeForceStoppedNeighbour(String ip_port){
        String[] info = ip_port.split("_");
        Neighbour neighbour = new Neighbour(info[0],Integer.parseInt(info[1]));
        neighbours.remove(neighbour);
        gui.updateTable(neighbours);
        health.remove(ip_port);
        Random random = new Random();
        if(neighbours.size()<2 || (random.nextInt(3)==1)){
            findANeighbour();
        }
    }

    public void findANeighbour() {
        queryManager.findANeighbour();
    }

    public int getHealth(String ip_port){
        if(health.containsKey(ip_port)){
            return health.get(ip_port);
        }
        else return 100;
    }

    public Set<Neighbour> getNeighbourCopy() throws CloneNotSupportedException {
        Set<Neighbour> neighs = new HashSet<>();
        for(Neighbour neighbour:neighbours){
            neighs.add(neighbour.clone());
        }
        return neighs;
    }

    public void addFile(String file){
        files.add(file);
        gui.updateFiles(files);
    }
}
