import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Listener.ThreadedListener;
import com.esotericsoftware.minlog.Log;

public class GameClient {
    UI ui;
    Client client;
    String name;

    public GameClient() throws IOException {
        client = new Client();
        client.start();

        // For consistency, the classes to be sent over the network are
        // registered by the same method for both the client and server.
        Network.register(client);

        // ThreadedListener runs the listener methods on a different thread.
        client.addListener(new ThreadedListener(new Listener() {
            public void connected(Connection connection) {
            }

            public void received(Connection connection, Object object) {
                if (object instanceof Network.RegistrationRequired) {
                    Network.Register register = new Network.Register();
                    register.name = name;
                    client.sendTCP(register);
                }

                if (object instanceof Network.AddSnake) {
                    Network.AddSnake msg = (Network.AddSnake) object;
                    ui.addSnake(msg.character);
                    return;
                }

                if (object instanceof Network.UpdateSnake) {
                    ui.updateSnake((Network.UpdateSnake) object);
                    return;
                }

                if (object instanceof Network.RemoveSnake) {
                    Network.RemoveSnake msg = (Network.RemoveSnake) object;
                    ui.removeSnake(msg.id);
                    return;
                }
            }

            public void disconnected(Connection connection) {
                System.exit(0);
            }
        }));

        ui = new UI();
        ui.ref = client;

        String host = ui.inputHost();
        try {
            client.connect(3000, host, Network.portTCP, Network.portUDP);
            // Server communication after connection can go here, or in Listener#connected().
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        name = ui.inputName();
        Network.Login login = new Network.Login();
        login.name = name;
        client.sendTCP(login);
        JFrame gameWindow = new JFrame();
        Display.setupMainWindow(gameWindow, ui);

        ui.selfName = name;
        ui.initializeEventListeners();
        ui.gameClock.start();

//        while (true) {
//            Network.MoveSnake msg = new Network.MoveSnake();
//            System.out.println();
//            msg.x = ui.goX;
//            msg.y = ui.goY;
//
//            if (msg != null) client.sendTCP(msg);
//        }
    }

    static class UI extends JPanel implements KeyListener, ActionListener {
        HashMap<Integer, Snake> characters = new HashMap();
        HashMap<Integer, java.util.List<PositionRegistry>> playersPositionMap = new HashMap<>();
        public Timer gameClock = new Timer(60, this);
        public Client ref;
        private float speed = 2;
        private int direction = 0;
        private float directionChangeRate = 1;

        Boolean play = false;
        int roomSize = 2;

        String selfName;
        String statusInfo = "";

        Color[] playerColors = new Color[]{Color.GREEN, Color.RED, Color.YELLOW, Color.BLUE};


        public void initializeEventListeners() {
            addKeyListener(this);
            setFocusable(true);
            setFocusTraversalKeysEnabled(false);
        }

        public String inputHost() {
            String input = (String) JOptionPane.showInputDialog(null, "Host:", "Connect to server", JOptionPane.QUESTION_MESSAGE,
                    null, null, "localhost");
            if (input == null || input.trim().length() == 0) System.exit(1);
            return input.trim();
        }

        public String inputName() {
            String input = (String) JOptionPane.showInputDialog(null, "Name:", "Connect to server", JOptionPane.QUESTION_MESSAGE,
                    null, null, "Test");
            if (input == null || input.trim().length() == 0) System.exit(1);
            return input.trim();
        }

        public void addSnake(Snake character) {
            characters.put(character.id, character);
            playersPositionMap.put(character.id, new ArrayList<PositionRegistry>());
            System.out.println(character.name + " added at " + character.x + ", " + character.y);
            if (characters.size() == roomSize) play = true;
        }

        public void updateSnake(Network.UpdateSnake msg) {
            Snake character = characters.get(msg.id);
            if (character == null) return;
            character.x = msg.x;
            character.y = msg.y;
            character.dead = msg.ded;
            if (character.name.equals(selfName)) {
                // System.out.println("coords:" + character.x + "; " + character.y);
            }

        }

        public void removeSnake(int id) {
            Snake character = characters.remove(id);
            if (character != null) System.out.println(character.name + " removed");
        }

        public void paint(Graphics g) {
            Graphics2D gg = (Graphics2D) g;

            // Background
            g.setColor(Color.BLACK);
            g.fillRect(1, 1, 800, 800);


            // Players
            for (Snake character : characters.values()) {
                try {
                    g.setColor(playerColors[character.colorIndex]);
                } catch (IndexOutOfBoundsException err) {
                    g.setColor(Color.GREEN);
                }
                for (PositionRegistry previousPosition : playersPositionMap.get(character.id)) {
                    Ellipse2D.Double previousDraw = new Ellipse2D.Double(previousPosition.X, previousPosition.Y, 10, 10);
                    gg.draw(previousDraw);
                }
                if (character.dead == 0) {
                    playersPositionMap.get(character.id).add(new PositionRegistry(character.x, character.y));
                    Ellipse2D.Double player = new Ellipse2D.Double(character.x, character.y, 10, 10);
                    if (character.name.equals(selfName)) {
                        System.out.println("coords:" + character.x + "; " + character.y);
                    }
                    gg.draw(player);
                }
            }

            g.setColor(Color.WHITE);
            gg.drawString(statusInfo, 400, 400);


            g.dispose();
        }

        private boolean compensateDisruptions(int characterId) {
            int disruptionCounter = 0;
            Snake checkedSnake = characters.get(characterId);
            if (playersPositionMap.get(characterId).size() > 6) {
                java.util.List<PositionRegistry> temp = playersPositionMap.get(characterId).subList(playersPositionMap.get(characterId).size() - 5, playersPositionMap.get(characterId).size());
                for (PositionRegistry previous : temp) {
                    if (Math.abs(previous.X - checkedSnake.x) > 35 || Math.abs(previous.Y - checkedSnake.y) > 35) {
                        disruptionCounter += 1;
                        if (disruptionCounter > 4) {
                            sendLastSafePosition(previous.X, previous.Y);
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        private boolean screenCollisionChecker() {
            for (Snake character : characters.values()) {
                if (compensateDisruptions(character.id)) {
                    if (character.name.equals(selfName)) {
                        if (character.x > 800 || character.x < 0) {
                            System.out.println("collision with screen" + character.x + ": " + character.y);
                            return true;
                        }
                        if (character.y > 800 || character.y < 0) {
                            System.out.println("collision with screen" + character.x + ": " + character.y);
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        private boolean snakesCollisionChecker() {
            for (Snake character : characters.values()) {
                if (compensateDisruptions(character.id)) {
                    if (character.name.equals(selfName) && character.dead == 0) {
                        if (playersPositionMap.get(character.id).size() > 40) {
                            java.util.List<PositionRegistry> temp = playersPositionMap.get(character.id).subList(0, playersPositionMap.get(character.id).size() - 5);
                            for (PositionRegistry previous : temp) {
                                if (Math.abs(previous.X - character.x) <= 3 && Math.abs(previous.Y - character.y) <= 3) {
                                    System.out.println("COLLISION WITH SELF");
                                    return true;
                                }
                            }
                        }
                        for (Snake otherSnake : characters.values()) {
                            if (!otherSnake.name.equals(selfName)) {
                                for (PositionRegistry previous : playersPositionMap.get(otherSnake.id)) {
                                    if (Math.abs(previous.X - character.x) <= 3 && Math.abs(previous.Y - character.y) <= 3) {
                                        System.out.println("COLLISION WITH SOMEONE ELSE");
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }

        private void checkForDead() {
            int deadCounter = 0;

            for (Snake snek : characters.values()) {
                deadCounter += snek.dead;
            }

            if (deadCounter > 0) {
                for (Snake snek : characters.values()) {
                    if (snek.name.equals(selfName)) {
                        if (snek.dead == 1) statusInfo = "You ded :/";
                        else if (snek.dead == 0 && (deadCounter == roomSize - 1 || deadCounter == roomSize))
                            statusInfo = "You won!";
                    }
                }
            }
        }

        private void movement() {
            switch (direction) {
                case 0:
                    sendNewPosition(0, -1 * speed);
                    break;
                case 1:
                    sendNewPosition(speed / 2, -1 * (speed / 2));
                    break;
                case 2:
                    sendNewPosition(speed, 0);
                    break;
                case 3:
                    sendNewPosition(speed / 2, speed / 2);
                    break;
                case 4:
                    sendNewPosition(0, speed);
                    break;
                case 5:
                    sendNewPosition(-1 * (speed / 2), speed / 2);
                    break;
                case 6:
                    sendNewPosition(-1 * speed, 0);
                    break;
                case 7:
                    sendNewPosition(-1 * (speed / 2), -1 * (speed / 2));
                    break;
            }
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            gameClock.start();
            if (play) {
                movement();
                if (screenCollisionChecker() || snakesCollisionChecker()) {
                    snekDied();
                }
                checkForDead();
                repaint();
            }

        }

        @Override
        public void keyTyped(KeyEvent keyEvent) {

        }

        private void snekDied() {
            Network.SnakeDed msg = new Network.SnakeDed();
            msg.ded = 1;

            if (msg != null) ref.sendTCP(msg);
        }

        private void sendNewPosition(float x, float y) {
            Network.MoveSnake msg = new Network.MoveSnake();
            msg.x = x;
            msg.y = y;

            if (msg != null) ref.sendTCP(msg);
        }

        private void sendLastSafePosition(float x, float y) {
            Network.LastSafePosition msg = new Network.LastSafePosition();
            msg.x = x;
            msg.y = y;

            if (msg != null) ref.sendTCP(msg);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                direction += directionChangeRate;
            }
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                direction -= directionChangeRate;
            }
            if (direction > 7) {
                direction = 0;
            }
            if (direction < 0) {
                direction = 7;
            }
        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {

        }
    }

    public static void main(String[] args) throws IOException {
        // Log.set(Log.LEVEL_DEBUG);
        new GameClient();
    }
}