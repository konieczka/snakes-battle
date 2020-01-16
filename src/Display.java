import javax.swing.*;

public class Display {
    public static void setupMainWindow(JFrame window, GameData gameData, Gameplay gamePlay) {
        window.setBounds(gameData.windowBoundaries[0],
                gameData.windowBoundaries[1],
                gameData.windowBoundaries[2],
                gameData.windowBoundaries[3]);
        window.setTitle(gameData.windowTitle);
        window.setVisible(true);
        window.add(gamePlay);
    }
}
