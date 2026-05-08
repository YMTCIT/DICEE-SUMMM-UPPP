public class GameSettings {

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static GameSettings instance;
    public static GameSettings get() {
        if (instance == null) instance = new GameSettings();
        return instance;
    }


    private static final double[] DEFAULT_MULTIPLIERS = {
            4.0, 4.0, 3.0, 3.0, 2.0, 2.0, 2.0, 2.0
    };
    private static final double KHAI_MULTIPLIER = 999.0;

    private boolean khaiMode = false;

    private GameSettings() {}

    // ── KhaiMode toggle ───────────────────────────────────────────────────────
    public boolean isKhaiMode()            { return khaiMode; }
    public void    setKhaiMode(boolean on) { khaiMode = on;   }

    /**
     * Returns the damage multiplier for the given character index.
     * When KhaiMode is ON every character deals 999x; otherwise the
     * default class multiplier is used.
     */
    public double getMultiplier(int characterIndex) {
        if (characterIndex < 0 || characterIndex >= DEFAULT_MULTIPLIERS.length) return 2.0;
        return khaiMode ? KHAI_MULTIPLIER : DEFAULT_MULTIPLIERS[characterIndex];
    }
}
