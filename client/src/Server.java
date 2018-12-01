import java.net.*;
import java.io.*;

public class Server implements Runnable{

    private volatile String response;
    private volatile boolean isValueSet = false;
    private static int port;


    private static ServerSocket serverSocket;
    private static Socket socket;

    public Server(int port) {
        this.port=port;
    }

    private static void runServer(String fileName) throws IOException {

        File transferFile = new File("files/" + fileName);
        byte[] bytearray = new byte[(int) transferFile.length()];
        FileInputStream fin = new FileInputStream(transferFile);
        BufferedInputStream bin = new BufferedInputStream(fin);
        bin.read(bytearray, 0, bytearray.length);
        OutputStream os = socket.getOutputStream();
        System.out.println("Sending Files...");
        os.write(bytearray, 0, bytearray.length);
        os.flush();
        socket.close();
        System.out.println("File transfer complete");
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                System.out.println("waiting");
                socket=this.serverSocket.accept();
                System.out.println("acceted the connection");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String filename=bufferedReader.readLine();
                System.out.println(filename);
                runServer(filename);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
