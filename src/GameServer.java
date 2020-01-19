import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

public class GameServer {
    Server server;
    HashSet<Snake> loggedIn = new HashSet();
    int colorOffset = 0;
    float[][] playerStartingLocation = new float[][]{{200, 200}, {200, 600}, {600, 200}, {600, 600}};


    public GameServer() throws IOException {
        server = new Server() {
            protected Connection newConnection() {
                // By providing our own connection implementation, we can store per
                // connection state without a connection ID to state look up.
                return new SnakeConnection();
            }
        };

        // For consistency, the classes to be sent over the network are
        // registered by the same method for both the client and server.
        Network.register(server);

        server.addListener(new Listener() {
            public void received(Connection c, Object object) {
                // We know all connections for this server are actually SnakeConnections.
                SnakeConnection connection = (SnakeConnection) c;
                Snake character = connection.character;

                if (object instanceof Network.Login) {
                    // Ignore if already logged in.
                    if (character != null) return;

                    // Reject if the name is invalid.
                    String name = ((Network.Login) object).name;
                    if (!isValid(name)) {
                        c.close();
                        return;
                    }

                    // Reject if already logged in.
                    for (Snake other : loggedIn) {
                        if (other.name.equals(name)) {
                            c.close();
                            return;
                        }
                    }

                    character = loadSnake(name);
                    if (colorOffset > 3) {
                        colorOffset = 0;
                    }
                    System.out.println("Offsets:" + colorOffset + " " + character.colorIndex);
                    character.colorIndex = colorOffset;
                    character.x = playerStartingLocation[character.colorIndex][0];
                    character.y = playerStartingLocation[character.colorIndex][1];
                    colorOffset += 1;

                    // Reject if couldn't load character.
                    if (character == null) {
                        c.sendUDP(new Network.RegistrationRequired());
                        return;
                    }

                    loggedIn(connection, character);
                    return;
                }

                if (object instanceof Network.Register) {
                    // Ignore if already logged in.
                    if (character != null) return;

                    Network.Register register = (Network.Register) object;

                    // Reject if the login is invalid.
                    if (!isValid(register.name)) {
                        c.close();
                        return;
                    }

                    // Reject if character alread exists.
                    if (loadSnake(register.name) != null) {
                        c.close();
                        return;
                    }

                    if (colorOffset > 3) {
                        colorOffset = 0;
                    }
                    character = new Snake();
                    character.name = register.name;
                    character.colorIndex = colorOffset;
                    character.x = playerStartingLocation[colorOffset][0];
                    character.y = playerStartingLocation[colorOffset][1];
                    colorOffset += 1;

                    if (!saveSnake(character)) {
                        c.close();
                        return;
                    }

                    loggedIn(connection, character);
                    return;
                }

                if (object instanceof Network.MoveSnake) {
                    // Ignore if not logged in.
                    if (character == null) return;

                    Network.MoveSnake msg = (Network.MoveSnake) object;

                    System.out.println("Received coords: " + msg.x + "; " + msg.y);
                    character.x += msg.x;
                    character.y += msg.y;
                    if (!saveSnake(character)) {
                        connection.close();
                        return;
                    }

                    Network.UpdateSnake update = new Network.UpdateSnake();
                    update.id = character.id;
                    update.x = character.x;
                    update.y = character.y;
                    ;

                    server.sendToAllUDP(update);
                    return;
                }
            }

            private boolean isValid(String value) {
                if (value == null) return false;
                value = value.trim();
                if (value.length() == 0) return false;
                return true;
            }

            public void disconnected(Connection c) {
                SnakeConnection connection = (SnakeConnection) c;
                if (connection.character != null) {
                    loggedIn.remove(connection.character);

                    Network.RemoveSnake removeSnake = new Network.RemoveSnake();
                    removeSnake.id = connection.character.id;
                    server.sendToAllUDP(removeSnake);
                }
            }
        });
        server.bind(Network.portTCP, Network.portUDP);
        server.start();
    }

    void loggedIn(SnakeConnection c, Snake character) {
        c.character = character;

        // Add existing characters to new logged in connection.
        for (Snake other : loggedIn) {
            Network.AddSnake addSnake = new Network.AddSnake();
            addSnake.character = other;
            c.sendUDP(addSnake);
        }

        loggedIn.add(character);

        // Add logged in character to all connections.
        Network.AddSnake addSnake = new Network.AddSnake();
        addSnake.character = character;
        server.sendToAllUDP(addSnake);
    }

    boolean saveSnake(Snake character) {
        File file = new File("characters", character.name.toLowerCase());
        file.getParentFile().mkdirs();

        if (character.id == 0) {
            String[] children = file.getParentFile().list();
            if (children == null) return false;
            character.id = children.length + 1;
        }

        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(file));
            output.writeInt(character.id);
            output.writeFloat(character.x);
            output.writeFloat(character.y);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            try {
                output.close();
            } catch (IOException ignored) {
            }
        }
    }

    Snake loadSnake(String name) {
        File file = new File("characters", name.toLowerCase());
        if (!file.exists()) return null;
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(file));
            Snake character = new Snake();
            character.id = input.readInt();
            character.name = name;
            character.x = input.readFloat();
            character.y = input.readFloat();
            input.close();
            return character;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                if (input != null) input.close();
            } catch (IOException ignored) {
            }
        }
    }

    // This holds per connection state.
    static class SnakeConnection extends Connection {
        public Snake character;
    }

    public static void main(String[] args) throws IOException {
        // Log.set(Log.LEVEL_DEBUG);
        new GameServer();
    }
}