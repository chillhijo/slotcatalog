package page;

import java.util.List;

public class GameAttributesResult{
    private final String gameType;
    private final List<String> attributes;

    public GameAttributesResult(String gameType, List<String> attributes) {
        this.gameType = gameType;
        this.attributes = attributes;
    }

    public String getGameType() {
        return gameType;
    }

    public List<String> getAttributes() {
        return attributes;
    }

}
