import javax.swing.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameData implements Serializable {
    public int currentPlayersCount = 0;

    public double[][] playersStartingPositions = {{250, 750},{250, 250},{750, 750}, {750, 250}};
    // public double[][] playersCurrentPositions = {{250, 750},{250, 250},{750, 750}, {750, 250}};


}
