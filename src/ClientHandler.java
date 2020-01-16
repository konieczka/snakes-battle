import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;

public class ClientHandler extends Thread {
    private int hr;
    private int mn;
    private Socket socket;
    private Server server;

    // Constructor
    public ClientHandler(Socket socket, Server serverRef) {
        this.socket = socket;
        this.server = serverRef;
    }

    private void sendGameData(OutputStream outputStream){
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(server.gameData);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        try {
            // Get the output and input streams
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            // Create the output and input streams
            try {
                //ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

                // Notify about connecting new client
                System.out.println("Client no." + Thread.currentThread().getId() + " connected");

                while (true) {
                    // Listener of client-provided data
                    // String clientResponse = input.readLine();

                    System.out.println("Sending ravens!");

                    sendGameData(outputStream);

                    // output.println("Echo!");
                }
            } catch (Exception err) {
                System.out.println(err.getMessage());
            }


        } catch (IOException err) {
            System.out.println("Client no." + Thread.currentThread().getId() + ": " + err.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException err) {
                System.out.println("Client no." + Thread.currentThread().getId() + ": " + err.getMessage());
            }
        }
    }
}
