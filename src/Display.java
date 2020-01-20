import javax.swing.*;

public class Display {
    public static void setupMainWindow(JFrame window, GameClient.UI gamePlay) {
        window.setBounds(0,
                0,
                800,
                800);
        window.setTitle("Wezyki");
        window.setVisible(true);

        window.add(gamePlay);
    }
}
