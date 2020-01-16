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
    public Timer gameClock = new Timer(8, this);
    private Boolean play = false;
    private double speed = 1;
    private double direction = 1;
    private double playerX = 200;
    private double playerY = 900;
    private java.util.List<PositionRegistry> playerPositionsList = new ArrayList<PositionRegistry>();


    public Gameplay() {
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        gameClock.start();
        play = true;
    }

    private void collisionChecker() {
        if (playerPositionsList.size() > 0) {
            for (PositionRegistry temp : playerPositionsList) {
                if (Math.abs(temp.X - playerX) <= 1 && Math.abs(temp.Y - playerY) <= 1){
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
        playerPositionsList.add(new PositionRegistry(playerX, playerY));
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
            direction += 1;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            direction -= 1;
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
            collisionChecker();
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
