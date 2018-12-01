import javax.print.DocFlavor;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GUI extends JFrame {
    private JPanel panel1;
    private JLabel Title;
    private JButton REGISTERButton;
    private JButton UNREGISTERButton;
    private JTextArea Search;
    private JButton SEARCHBUTON;
    private JLabel REGStatus;
    private JLabel LOGSPANEL;
    private JLabel FILESLABLE;
    private JLabel TABLE;
    private JLabel SEARCHRESULT;
    private JLabel IP_PORT;
    private JTextField getIP;
    private JTextField getPort;
    private JTextField getFile;
    private JButton GETFILEButton;
    private QueryManager queryManager;
    private List<String> logs = new ArrayList<>();


    public GUI(String ip_port){
        this.setContentPane(panel1);
        UNREGISTERButton.setEnabled(false);
        this.setMinimumSize(new Dimension(1000,700));
        IP_PORT.setText(ip_port);
        REGISTERButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                queryManager.manageQuery("REG");
            }
        });

        UNREGISTERButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                queryManager.manageQuery("UNREG");
            }
        });

        SEARCHBUTON.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String file = Search.getText();
                queryManager.manageQuery("SEARCH \""+file+"\"");
            }
        });
        GETFILEButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String ip=getIP.getText();
                int port = Integer.valueOf(getPort.getText());
                String file = getFile.getText();
                try {
                    queryManager.getFile(ip,port,file);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public void setResultBlank(){
        SEARCHRESULT.setText("");
    }

    public void processREGOK(){
        REGStatus.setText("REGISTERED");
        REGISTERButton.setEnabled(false);
        UNREGISTERButton.setEnabled(true);
    }

    public void processUNREGOK(){
        REGStatus.setText("UNREGISTERED");
        REGISTERButton.setEnabled(true);
        UNREGISTERButton.setEnabled(false);
    }

    public void log(String log){
        if(logs.size()<20){
            logs.add(log);
        }
        else{
            logs.remove(0);
            logs.add(log);
        }
        String out="";
        for(String st:logs){
            out=st+"<br>"+out;
        }
        out="<html>"+out+"<html>";

        LOGSPANEL.setText(out);

    }

    public QueryManager getQueryManager() {
        return queryManager;
    }

    public void setQueryManager(QueryManager queryManager) {
        this.queryManager = queryManager;
    }

    public void updateFiles(List<String> files){
        String out="";
        for(String st:files){
            out=st+"<br>"+out;
        }
        out="<html>"+out+"<html>";

        FILESLABLE.setText(out);
    }

    public void updateTable(Set<Neighbour> table){
        String out="";
        for(Neighbour n:table){
            out=n.getIp()+":"+String.valueOf(n.getPort())+"<br>"+out;
        }
        out="<html>"+out+"<html>";

        TABLE.setText(out);
    }

    public void updateResult(Map<String,Set<String>> result){
        String out="";
        for (String file:result.keySet()) {
            out=out+file+"<br>====================================================================";
            for (String ip_port:result.get(file)) {
                out = out+"<br>"+ip_port.replace("_",":");
            }
        }
        out="<html>"+out+"<html>";

        SEARCHRESULT.setText(out);
    }



}
