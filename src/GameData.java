import javax.swing.*;
import java.io.Serializable;

public class GameData implements Serializable {
    public Integer[] windowBoundaries;
    public String windowTitle = "Wezyki";

    public int playerX = 500;
    public int playerY = 500;



    public GameData(Integer[] windowBoundaries) {
        this.windowBoundaries = windowBoundaries;
    }
}
