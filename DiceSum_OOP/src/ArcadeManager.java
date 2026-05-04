public class ArcadeManager {
    private final GameWindow gameWindow;
    private final int        playerIndex;
    // ── NEW: player name for leaderboard ──────────────────────────────────────
    private final String     playerName;

    // ── Boss data [name, class, hp, damage, special, gifPath] ────────────────
    public static final String[][] MINIBOSSES = {
            { "Adji",       "Support",  "125", "2.0", "Healing Aura – Heals 20 HP (CD: 3 turns)",         "assets/miniboss/adji_idle.gif"  },
            { "Dextereous", "Assassin", "100", "4.0", "Shadow Strike – Dodge next attack (CD: 4 turns)",  "assets/miniboss/dex_idle.gif"   },
            { "Cromel",     "Fighter",  "135", "3.0", "Flame Burst – +8 damage bonus (CD: 5 turns)",      "assets/miniboss/cromel.gif"     },
            { "Selos",      "Assassin", "100", "4.0", "Phantom Step – Extra turn (CD: 4 turns)",          "assets/miniboss/kenz_idle.gif"  },
            { "Kenz",       "Tank",     "170", "2.0", "Ground Slam – Stun 2 turns (CD: 4 turns)",         "assets/miniboss/selos_idle.gif" },
    };

    public static final String[] MINIFINAL_BOSS = {
            "Akhai", "Assassin", "250", "4.0",
            "Death Dance – Dodge + counter attack (CD: 5 turns)",
            "assets/boss/khaigu.gif"
    };

    public static final String[] FINAL_BOSS = {
            "KhaiGu", "Master", "200", "3.0",
            "HAGBONG KA SAKEN BOI – +5 damage every 4 turns",
            "assets/boss/khaigu.gif"
    };

    private int     currentBossIndex  = 0;
    private int     coins             = 0;
    private int     battlesWon        = 0;
    private int     playerHp;
    private int     playerMaxHp;
    private boolean finalDefeated     = false;

    private int[] shopInventory = new int[8];

    // ── Original 2-arg constructor (kept for safety) ──────────────────────────
    public ArcadeManager(GameWindow gameWindow, int playerIndex) {
        this(gameWindow, playerIndex, "PLAYER");
    }

    // ── NEW 3-arg constructor used by CharacterSelectionScreen ────────────────
    public ArcadeManager(GameWindow gameWindow, int playerIndex, String playerName) {
        this.gameWindow  = gameWindow;
        this.playerIndex = playerIndex;
        this.playerName  = (playerName != null && !playerName.isBlank()) ? playerName : "PLAYER";

        String[][] chars = ArcadeBattleScreen.CHARACTERS;
        playerMaxHp = Integer.parseInt(chars[playerIndex][2]);
        playerHp    = playerMaxHp;
    }

    public void startNext() {
        if (currentBossIndex < 5) {
            gameWindow.switchScreen(new ArcadeBattleScreen(
                    gameWindow, this, playerIndex, MINIBOSSES[currentBossIndex], false, false));
        } else if (currentBossIndex == 5) {
            gameWindow.switchScreen(new ArcadeBattleScreen(
                    gameWindow, this, playerIndex, MINIFINAL_BOSS, true, false));
        } else {
            gameWindow.switchScreen(new ArcadeBattleScreen(
                    gameWindow, this, playerIndex, FINAL_BOSS, false, true));
        }
    }

    public void onBattleWon() {
        battlesWon++;
        currentBossIndex++;

        int earned;
        if (currentBossIndex <= 5) {
            int[] rewards = {50, 85, 130, 190, 265, 355};
            earned = rewards[Math.min(battlesWon - 1, rewards.length - 1)];
        } else {
            earned = 1000;
        }
        coins += earned;

        playerHp = Math.min(playerMaxHp, playerHp + 15);

        if (currentBossIndex > 6) {
            finalDefeated = true;
            // ── Pass manager to VictoryScreen so it can write the leaderboard ─
            gameWindow.switchScreen(new ArcadeVictoryScreen(gameWindow, this));
        } else {
            gameWindow.switchScreen(new ArcadeShopScreen(gameWindow, this));
        }
    }

    public void onBattleLost() {
        gameWindow.switchScreen(new ArcadeGameOverScreen(gameWindow, coins, battlesWon));
    }

    public void onShopDone() { startNext(); }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public String getPlayerName()       { return playerName;    }
    public int    getCoins()            { return coins;         }
    public void   addCoins(int amt)     { coins += amt;         }
    public void   spendCoins(int amt)   { coins -= amt;         }
    public int    getBattlesWon()       { return battlesWon;    }
    public int    getPlayerHp()         { return playerHp;      }
    public void   setPlayerHp(int hp)   { playerHp = Math.min(playerMaxHp, Math.max(0, hp)); }
    public int    getPlayerMaxHp()      { return playerMaxHp;   }
    public void   setPlayerMaxHp(int v) { playerMaxHp = v;      }
    public int    getPlayerIndex()      { return playerIndex;   }
    public int    getCurrentBossIndex() { return currentBossIndex; }
    public int[]  getShopInventory()    { return shopInventory; }
}
