import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static String EchoString;
    private static String EchoDate;
    private static Scanner input = new Scanner(System.in);
    private Integer[] windowBoundaries = new Integer[4];

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 5000)) {
            // Get the output and input streams
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            // Create the output and input streams
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            GameData gameData = (GameData) objectInputStream.readObject();

            System.out.println("Boundaries: " + gameData.windowBoundaries[0] +
                    gameData.windowBoundaries[1] +
                    gameData.windowBoundaries[2] +
                    gameData.windowBoundaries[3]);

            JFrame window = new JFrame();
            window.setBounds(gameData.windowBoundaries[0],
                    gameData.windowBoundaries[1],
                    gameData.windowBoundaries[2],
                    gameData.windowBoundaries[3]);
            window.setVisible(true);
        } catch (IOException err) {
            System.out.println("Client exception: " + err.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
