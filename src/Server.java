import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    // Game settings
    private Integer[] boundaries = {10, 10, 1000, 1000};

    // GameData setup
    public GameData gameData = new GameData(boundaries);

    public synchronized void updateGameData(GameData newGameData){
        gameData = newGameData;
    }

    public void start(){
        // Create new client handler for each new connected client
        try(ServerSocket serverSocket = new ServerSocket(5000)){
            while(true){
                new ClientHandler(serverSocket.accept(), this).start();
            }

        } catch(IOException err){
            System.out.println("Server exception: " + err.getMessage());
        }
    }

    public static void main(String[] args){
        Server gameServer = new Server();
        gameServer.start();
    }
}
