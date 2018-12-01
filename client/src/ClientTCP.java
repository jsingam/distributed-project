import java.net.*;
import java.io.*;

public class ClientTCP {
    public static void main(String[] args) throws IOException {
        downloadFile("127.0.0.1",15123,"Adventures of Tintin-1");
    }

    public static void downloadFile(String ip, int port, String fileName)  throws IOException {
        int filesize = 1022386;
        int bytesRead;
        int currentTot = 0;
        Socket socket = new Socket(ip, port);
        byte[] bytearray = new byte[filesize];
        InputStream is = socket.getInputStream();
        PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
        pw.println(fileName);
        FileOutputStream fos = new FileOutputStream("files/" + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bytesRead = is.read(bytearray, 0, bytearray.length);
        currentTot = bytesRead;
        do {
            bytesRead = is.read(bytearray, currentTot, (bytearray.length - currentTot));
            if (bytesRead >= 0) currentTot += bytesRead;
        } while (bytesRead > -1);
        bos.write(bytearray, 0, currentTot);
        bos.flush();
        bos.close();
        socket.close();
    }

}
