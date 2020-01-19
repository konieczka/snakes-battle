import javafx.geometry.Pos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

public class Gameplay extends JPanel implements KeyListener, ActionListener {
    public Timer gameClock = new Timer(10, this);
    private Boolean play = false;

    private float speed = 2;
    private float direction = 1;
    private float directionChangeRate = 5;

    public float playerX;
    public float playerY;

    private int selfPlayerId;
    private java.util.List<PositionRegistry> playerPositionsList = new ArrayList<PositionRegistry>();

    private GameData serverData;


    public Gameplay(float x, float y) {
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        playerX = x;
        playerY = y;
    }

    public void startTheGame() {
        gameClock.start();
        play = true;
    }

    private void screenCollisionChecker() {
        if (playerX > 1000 || playerX < 0) {
            System.out.println("OUT OF SCREEN");
        }
        if (playerY > 1000 || playerY < 0) {
            System.out.println("OUT OF SCREEN");
        }
    }

    private void selfCollisionChecker() {
        if (playerPositionsList.size() > 10) {
            for (PositionRegistry temp : playerPositionsList.subList(0, playerPositionsList.size() - 10)) {
                if (Math.abs(temp.X - playerX) <= 5 && Math.abs(temp.Y - playerY) <= 5) {
                    System.out.println("COLLISION");
                }
            }
        }
    }


    private void movement() {
        if (direction > 0 && direction <= 90) {
            playerX += speed * ((direction % 90) / 90);
            playerY -= speed * ((90 - (direction % 90)) / 90);
        } else if (direction > 90 && direction <= 180) {
            playerX += speed * ((90 - (direction % 90)) / 90);
            playerY += speed * ((direction % 90) / 90);
        } else if (direction > 180 && direction <= 270) {
            playerX -= speed * ((direction % 90) / 90);
            playerY += speed * ((90 - (direction % 90)) / 90);
        } else if (direction > 270 && direction <= 360) {
            playerX -= speed * ((90 - (direction % 90)) / 90);
            playerY -= speed * ((direction % 90) / 90);
        }
//        playerPositionsList.add(new PositionRegistry(playerX, playerY));
//        if (playerPositionsList.size() > 700) {
//            playerPositionsList = playerPositionsList.subList(playerPositionsList.size() - 700, playerPositionsList.size());
//        }
    }

    public void paint(Graphics g) {
        Graphics2D gg = (Graphics2D) g;

        // Background
        g.setColor(Color.BLACK);
        g.fillRect(1, 1, 1000, 1000);

        g.setColor(Color.GREEN);
        // Draw previous positions of this player
        if (playerPositionsList.size() > 0) {
            for (PositionRegistry temp : playerPositionsList) {
                Ellipse2D.Double previousPosition = new Ellipse2D.Double(temp.X,
                        temp.Y,
                        10,
                        10);
                gg.draw(previousPosition);
            }

        }

        // Player
        Ellipse2D.Double player = new Ellipse2D.Double(playerX, playerY, 10, 10);

        gg.draw(player);
        //g.dispose();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            direction += directionChangeRate;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            direction -= directionChangeRate;
        }
        if (direction > 360) {
            direction = 0;
        }
        if (direction < 0) {
            direction = 360;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        gameClock.start();
        if (play) {
            movement();
            selfCollisionChecker();
            screenCollisionChecker();
        }

        repaint();
    }

    public double getPlayerX() {
        return playerX;
    }

    public double getPlayerY() {
        return playerY;
    }
}
