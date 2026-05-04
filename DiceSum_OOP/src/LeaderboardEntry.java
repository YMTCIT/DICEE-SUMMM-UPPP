import java.io.Serializable;

public class LeaderboardEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String playerName;
    private final String characterName;
    private final int    battlesWon;
    private final int    coins;
    private final String date;           // yyyy-MM-dd

    public LeaderboardEntry(String playerName, String characterName,
                            int battlesWon, int coins, String date) {
        this.playerName    = playerName;
        this.characterName = characterName;
        this.battlesWon    = battlesWon;
        this.coins         = coins;
        this.date          = date;
    }

    public String getPlayerName()    { return playerName;    }
    public String getCharacterName() { return characterName; }
    public int    getBattlesWon()    { return battlesWon;    }
    public int    getCoins()         { return coins;         }
    public String getDate()          { return date;          }
}
