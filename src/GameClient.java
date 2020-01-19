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
                    client.sendUDP(register);
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
        client.sendUDP(login);
        JFrame gameWindow = new JFrame();
        Display.setupMainWindow(gameWindow, ui);
        ui.initializeEventListeners();
        ui.gameClock.start();

//        while (true) {
//            Network.MoveSnake msg = new Network.MoveSnake();
//            System.out.println();
//            msg.x = ui.goX;
//            msg.y = ui.goY;
//
//            if (msg != null) client.sendUDP(msg);
//        }
    }

    static class UI extends JPanel implements KeyListener, ActionListener {
        HashMap<Integer, Snake> characters = new HashMap();
        HashMap<Integer, java.util.List<PositionRegistry>> playersPositionMap = new HashMap<>();
        public Timer gameClock = new Timer(50, this);
        public Client ref;
        private float speed = 1;
        private int direction = 0;
        private float directionChangeRate = 1;

        Boolean play = false;
        int roomSize = 2;

        int selfId;

        Color[] playerColors = new Color[]{Color.GREEN, Color.RED, Color.YELLOW, Color.ORANGE};


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
            if (characters.size() == 0) {
                selfId = character.id;
            }
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

            System.out.println(character.name + " moved to " + character.x + ", " + character.y);
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
                playersPositionMap.get(character.id).add(new PositionRegistry(character.x, character.y));

                Ellipse2D.Double player = new Ellipse2D.Double(character.x, character.y, 10, 10);

                gg.draw(player);
            }

            g.dispose();
        }

        private boolean screenCollisionChecker() {
            for (Snake character : characters.values()){
                if (character.id != selfId){
                    if (character.x > 800 || character.x < 0) {
                        return true;
                    }
                    if (character.y > 800 || character.y < 0) {
                        return true;
                    }
                }
            }

            return false;
        }

        private boolean snakesCollisionChecker() {
            for (Snake character : characters.values()) {
                if (character.id == selfId) {
                    if (playersPositionMap.get(character.id).size() > 10) {
                        java.util.List<PositionRegistry> temp = playersPositionMap.get(character.id).subList(0, playersPositionMap.get(character.id).size() - 10);
                        for (PositionRegistry previous : temp) {
                            if (Math.abs(previous.X - character.x) <= 5 && Math.abs(previous.Y - character.y) <= 5) {
                                System.out.println("COLLISION WITH SELF");
                                return true;
                            }
                        }
                    }
                } else {
//                    for (PositionRegistry previous : playersPositionMap.get(character.id)) {
//                        if (Math.abs(previous.X - character.x) <= 5 && Math.abs(previous.Y - character.y) <= 5) {
//                            System.out.println("COLLISION WITH SOMEONE ELSE");
//                            return true;
//                        }
//                    }
                    continue;
                }
            }
            return false;
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
                screenCollisionChecker();
                snakesCollisionChecker();
                repaint();
            }

        }

        @Override
        public void keyTyped(KeyEvent keyEvent) {

        }

        private void sendNewPosition(float x, float y) {
            Network.MoveSnake msg = new Network.MoveSnake();
            System.out.println();
            msg.x = x;
            msg.y = y;

            if (msg != null) ref.sendUDP(msg);
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