import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ArcadeVictoryScreen extends JPanel {

    private final GameWindow    gameWindow;
    private final ArcadeManager arcadeManager;

    private Rectangle leaderboardRect = new Rectangle();
    private Rectangle menuRect        = new Rectangle();
    private boolean   hoverBoard      = false;
    private boolean   hoverMenu       = false;

    public ArcadeVictoryScreen(GameWindow gameWindow, ArcadeManager arcadeManager) {
        this.gameWindow    = gameWindow;
        this.arcadeManager = arcadeManager;
        setLayout(null);
        addMouseListeners();
        MusicManager.get().playBGM(MusicManager.BGM_VICTORY);

        // ── Save to leaderboard (only KhaiGu victors reach this screen) ───────
        saveToLeaderboard();
    }

    private void saveToLeaderboard() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // Character name from ArcadeBattleScreen constants via playerIndex
        String charName = ArcadeBattleScreen.CHARACTERS[arcadeManager.getPlayerIndex()][0];
        LeaderboardManager.saveEntry(new LeaderboardEntry(
                arcadeManager.getPlayerName(),
                charName,
                arcadeManager.getBattlesWon(),
                arcadeManager.getCoins(),
                today
        ));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int w = getWidth(), h = getHeight();

        // Deep dark-green bg
        g2.setColor(new Color(8, 18, 8));
        g2.fillRect(0, 0, w, h);

        // Radial glow
        RadialGradientPaint rgp = new RadialGradientPaint(
                w / 2f, h / 2f, Math.max(w, h) / 2f,
                new float[]{0f, 1f},
                new Color[]{new Color(30, 80, 30, 100), new Color(0, 0, 0, 0)});
        g2.setPaint(rgp);
        g2.fillRect(0, 0, w, h);

        // ── Main title ────────────────────────────────────────────────────────
        g2.setFont(new Font("Arial", Font.BOLD, 60));
        g2.setColor(new Color(255, 220, 50));
        FontMetrics fm = g2.getFontMetrics();
        String t = "YOU WIN!";
        // shadow
        g2.setColor(new Color(120, 80, 0));
        g2.drawString(t, (w - fm.stringWidth(t)) / 2 + 3, h / 2 - 105);
        g2.setColor(new Color(255, 220, 50));
        g2.drawString(t, (w - fm.stringWidth(t)) / 2, h / 2 - 108);

        // ── Stats block ───────────────────────────────────────────────────────
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(Color.WHITE);
        FontMetrics fm2 = g2.getFontMetrics();
        String[] lines = {
                "♛  ARCADE MODE COMPLETED!  ♛",
                "Player: " + arcadeManager.getPlayerName(),
                "Battles Won: " + arcadeManager.getBattlesWon(),
                "Total Coins: " + arcadeManager.getCoins(),
                "Sir KhaiGu has been defeated!"
        };

        int ly = h / 2 - 50;
        for (int i = 0; i < lines.length; i++) {
            if (i == 0) g2.setColor(new Color(255, 200, 0));
            else if (i == 1) g2.setColor(new Color(130, 220, 255));
            else if (i == 4) g2.setColor(new Color(255, 100, 100));
            else g2.setColor(Color.WHITE);
            g2.drawString(lines[i], (w - fm2.stringWidth(lines[i])) / 2, ly);
            ly += 36;
        }

        // ── Buttons ───────────────────────────────────────────────────────────
        int gap    = 24;
        int totalW = 240 + gap + 200;
        int bx     = (w - totalW) / 2;
        int by     = ly + 20;

        leaderboardRect.setBounds(bx, by, 240, 52);
        menuRect.setBounds(bx + 240 + gap, by, 200, 52);

        drawVictoryBtn(g2, leaderboardRect, hoverBoard, "VIEW LEADERBOARD",
                new Color(255, 200, 0), new Color(50, 40, 0));
        drawVictoryBtn(g2, menuRect, hoverMenu, "MAIN MENU",
                new Color(80, 180, 80), new Color(10, 30, 10));
    }

    private void drawVictoryBtn(Graphics2D g2, Rectangle r, boolean hover,
                                String label, Color borderCol, Color fillCol) {
        Rectangle dr = hover
                ? new Rectangle(r.x - 4, r.y - 4, r.width + 8, r.height + 8) : r;
        g2.setColor(hover ? borderCol.brighter() : borderCol);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(dr.x, dr.y, dr.width, dr.height, 12, 12);
        g2.setColor(fillCol);
        g2.fillRoundRect(dr.x + 1, dr.y + 1, dr.width - 2, dr.height - 2, 11, 11);
        g2.setFont(new Font("Courier New", Font.BOLD, 15));
        g2.setColor(hover ? Color.WHITE : borderCol);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label,
                dr.x + (dr.width  - fm.stringWidth(label)) / 2,
                dr.y + (dr.height + fm.getAscent() - fm.getDescent()) / 2);
    }

    private void addMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverBoard = leaderboardRect.contains(e.getPoint());
                hoverMenu  = menuRect.contains(e.getPoint());
                setCursor((hoverBoard || hoverMenu)
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (leaderboardRect.contains(e.getPoint())) {
                    // Go to leaderboard, highlighting current player
                    gameWindow.switchScreen(
                            new LeaderboardScreen(gameWindow, arcadeManager.getPlayerName()));
                } else if (menuRect.contains(e.getPoint())) {
                    gameWindow.switchScreen(new HomeScreen(gameWindow));
                }
            }
        });
    }
}
