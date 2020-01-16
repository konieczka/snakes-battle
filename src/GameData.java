import java.io.Serializable;

public class GameData implements Serializable {
    public Integer[] windowBoundaries;

    public GameData(Integer[] windowBoundaries) {
        this.windowBoundaries = windowBoundaries;
    }
}
