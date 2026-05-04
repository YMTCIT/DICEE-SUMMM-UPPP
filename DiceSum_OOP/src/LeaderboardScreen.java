import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class LeaderboardScreen extends JPanel {

    // ── Layout constants ──────────────────────────────────────────────────────
    private static final int ROW_H      = 44;
    private static final int TOP_N      = 10;
    private static final int BTN_WIDTH  = 220;
    private static final int BTN_HEIGHT = 50;
    private static final int BEZEL      = 34;

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE  = new Font("Courier New", Font.BOLD, 34);
    private static final Font FONT_HDR    = new Font("Courier New", Font.BOLD, 12);
    private static final Font FONT_ROW    = new Font("Courier New", Font.BOLD, 16);
    private static final Font FONT_BTN    = new Font("Courier New", Font.BOLD, 15);
    private static final Font FONT_EMPTY  = new Font("Courier New", Font.BOLD, 18);

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final Color COL_BG       = Color.BLACK;
    private static final Color COL_BEZEL    = new Color(26, 26, 26);
    private static final Color COL_EDGE     = new Color(65, 65, 65);
    private static final Color COL_TITLE    = new Color(255, 200, 0);
    private static final Color COL_SHADOW   = new Color(200, 0,   0);
    private static final Color COL_DIVIDER  = new Color(255, 200, 0, 100);
    private static final Color COL_ROW_EVEN = new Color(18,  18,  55);
    private static final Color COL_ROW_ODD  = new Color(10,  10,  38);
    private static final Color COL_ME       = new Color(42,  42, 105);
    private static final Color COL_RANK     = new Color(255, 220,  50);
    private static final Color COL_RANK1    = new Color(255, 215,   0);
    private static final Color COL_NAME     = Color.WHITE;
    private static final Color COL_CHAR     = new Color(80,  220, 255);
    private static final Color COL_BATTLES  = new Color(255, 165,  50);
    private static final Color COL_COINS    = new Color(80,  255, 120);
    private static final Color COL_DATE     = new Color(140, 140, 140);
    private static final Color COL_HDR      = new Color(120, 120, 120);
    private static final Color COL_ACCENT   = new Color(255,  60,  60);

    // ── State ────────────────────────────────────────────────────────────────
    private final GameWindow           gameWindow;
    private final String               currentPlayer; // null when opened from leaderboard button
    private final List<LeaderboardEntry> entries;
    private int                        myRank = -1;

    // ── Buttons ───────────────────────────────────────────────────────────────
    private Rectangle playAgainRect = new Rectangle();
    private Rectangle backMenuRect  = new Rectangle();
    private boolean   hoverAgain    = false;
    private boolean   hoverBack     = false;

    // ── CRT scanline cache ────────────────────────────────────────────────────
    private BufferedImage scanlines;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor used from ArcadeVictoryScreen (knows who just won)
    // ─────────────────────────────────────────────────────────────────────────
    public LeaderboardScreen(GameWindow gameWindow, String currentPlayer) {
        this.gameWindow    = gameWindow;
        this.currentPlayer = currentPlayer;
        this.entries       = LeaderboardManager.getTopN(TOP_N);
        findMyRank();
        setLayout(null);
        addMouseListeners();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor used from GameModeScreen leaderboard button (no current player)
    // ─────────────────────────────────────────────────────────────────────────
    public LeaderboardScreen(GameWindow gameWindow) {
        this(gameWindow, null);
    }

    private void findMyRank() {
        if (currentPlayer == null) return;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getPlayerName().equals(currentPlayer)) {
                myRank = i;
                break;
            }
        }
    }

    // ── Paint ─────────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // ── Solid black background ────────────────────────────────────────────
        g2.setColor(COL_BG);
        g2.fillRect(0, 0, w, h);

        // ── Cabinet bezel ─────────────────────────────────────────────────────
        g2.setColor(COL_BEZEL);
        g2.fillRoundRect(BEZEL, BEZEL, w - BEZEL * 2, h - BEZEL * 2, 36, 36);
        g2.setColor(COL_EDGE);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(BEZEL, BEZEL, w - BEZEL * 2, h - BEZEL * 2, 36, 36);
        // inner neon ring
        g2.setColor(new Color(255, 200, 0, 28));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(BEZEL + 7, BEZEL + 7,
                w - (BEZEL + 7) * 2, h - (BEZEL + 7) * 2, 28, 28);

        // ── Crown title ───────────────────────────────────────────────────────
        g2.setFont(FONT_TITLE);
        FontMetrics fmT = g2.getFontMetrics();
        String titleText = "LEADERBOARD";
        int titleW = fmT.stringWidth(titleText);

        // Crown glyphs on each side
        g2.setFont(new Font("Arial", Font.BOLD, 30));
        FontMetrics fmCrown = g2.getFontMetrics();
        String crown = "♛";
        int crownW   = fmCrown.stringWidth(crown);

        int totalRowW = crownW + 18 + titleW + 18 + crownW;
        int startX    = (w - totalRowW) / 2;
        int titleY    = BEZEL + 58;

        // Left crown — gold + red shadow
        g2.setColor(new Color(180, 30, 0));
        g2.drawString(crown, startX + 2, titleY + 2);
        g2.setColor(COL_RANK1);
        g2.drawString(crown, startX, titleY);

        // Title text — gold with red shadow
        g2.setFont(FONT_TITLE);
        g2.setColor(COL_SHADOW);
        g2.drawString(titleText, startX + crownW + 18 + 2, titleY + 2);
        g2.setColor(COL_TITLE);
        g2.drawString(titleText, startX + crownW + 18, titleY);

        // Right crown
        g2.setFont(new Font("Arial", Font.BOLD, 30));
        int rightCrownX = startX + crownW + 18 + titleW + 18;
        g2.setColor(new Color(180, 30, 0));
        g2.drawString(crown, rightCrownX + 2, titleY + 2);
        g2.setColor(COL_RANK1);
        g2.drawString(crown, rightCrownX, titleY);

        // Sub-label: only KhaiGu victors
        g2.setFont(new Font("Courier New", Font.PLAIN, 12));
        g2.setColor(new Color(180, 80, 80));
        FontMetrics fmSub = g2.getFontMetrics();
        String sub = "[ Hall of Champions — Sir KhaiGu Defeated ]";
        g2.drawString(sub, (w - fmSub.stringWidth(sub)) / 2, titleY + 20);

        // Divider
        int divY = titleY + 32;
        g2.setColor(COL_DIVIDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(BEZEL + 50, divY, w - BEZEL - 50, divY);

        // ── Table ─────────────────────────────────────────────────────────────
        int tableX = BEZEL + 50;
        int tableW = w - (BEZEL + 50) * 2;
        int hdrY   = divY + 20;

        drawHeaders(g2, tableX, tableW, hdrY);

        int rowStart = hdrY + 8;

        if (entries.isEmpty()) {
            g2.setFont(FONT_EMPTY);
            g2.setColor(new Color(100, 100, 100));
            String empty = "No victors yet. Defeat Sir KhaiGu!";
            FontMetrics fmE = g2.getFontMetrics();
            g2.drawString(empty, (w - fmE.stringWidth(empty)) / 2, rowStart + 80);
        } else {
            for (int i = 0; i < entries.size(); i++) {
                drawRow(g2, i, entries.get(i), tableX, tableW, rowStart + i * ROW_H);
            }
        }

        // ── Buttons ───────────────────────────────────────────────────────────
        int tableBottom = rowStart + Math.max(1, entries.size()) * ROW_H;
        int btnY        = tableBottom + 22;

        // Centre them with a gap
        int gap      = 24;
        int totalBW  = BTN_WIDTH * 2 + gap;
        int btnBaseX = (w - totalBW) / 2;

        // Only show PLAY AGAIN when coming from a victory (currentPlayer not null)
        if (currentPlayer != null) {
            playAgainRect.setBounds(btnBaseX, btnY, BTN_WIDTH, BTN_HEIGHT);
            backMenuRect .setBounds(btnBaseX + BTN_WIDTH + gap, btnY, BTN_WIDTH, BTN_HEIGHT);
            drawNeonButton(g2, playAgainRect, hoverAgain, "PLAY AGAIN");
        } else {
            // Just one centred back button
            backMenuRect.setBounds((w - BTN_WIDTH) / 2, btnY, BTN_WIDTH, BTN_HEIGHT);
            playAgainRect.setBounds(0, 0, 0, 0);
        }
        drawNeonButton(g2, backMenuRect, hoverBack, "MAIN MENU");

        // ── CRT scanlines ─────────────────────────────────────────────────────
        drawScanlines(g2, w, h);
    }

    // ── Column headers ────────────────────────────────────────────────────────
    private void drawHeaders(Graphics2D g2, int tableX, int tableW, int y) {
        g2.setFont(FONT_HDR);
        g2.setColor(COL_HDR);

        // Column widths — must match drawRow()
        int rankW    = 46;
        int nameW    = 160;
        int charW    = 120;
        int battlesW = 100;
        int coinsW   = 90;
        int dateW    = tableW - rankW - nameW - charW - battlesW - coinsW;

        int cx = tableX;
        g2.drawString("RANK",     cx, y);                         cx += rankW;
        g2.drawString("PLAYER",   cx, y);                         cx += nameW;
        g2.drawString("CHARACTER",cx, y);                         cx += charW;
        g2.drawString("BATTLES",  cx, y);                         cx += battlesW;
        FontMetrics fm = g2.getFontMetrics();
        String coinsHdr = "COINS";
        g2.drawString(coinsHdr,
                tableX + rankW + nameW + charW + battlesW + coinsW - fm.stringWidth(coinsHdr), y);
        String dateHdr = "DATE";
        g2.drawString(dateHdr,
                tableX + tableW - fm.stringWidth(dateHdr), y);
    }

    // ── Single row ────────────────────────────────────────────────────────────
    private void drawRow(Graphics2D g2, int rank, LeaderboardEntry e,
                         int tableX, int tableW, int rowY) {
        boolean isMe = (rank == myRank && currentPlayer != null);

        int rankW    = 46;
        int nameW    = 160;
        int charW    = 120;
        int battlesW = 100;
        int coinsW   = 90;

        // Row background
        Color bg = isMe ? COL_ME
                : (rank % 2 == 0 ? COL_ROW_EVEN : COL_ROW_ODD);
        g2.setColor(bg);
        g2.fillRoundRect(tableX - 6, rowY, tableW + 12, ROW_H - 4, 8, 8);

        // Gold shimmer for #1
        if (rank == 0) {
            g2.setColor(new Color(255, 215, 0, 22));
            g2.fillRoundRect(tableX - 6, rowY, tableW + 12, ROW_H - 4, 8, 8);
        }

        // Red left accent for current player
        if (isMe) {
            g2.setColor(COL_ACCENT);
            g2.fillRoundRect(tableX - 6, rowY, 4, ROW_H - 4, 3, 3);
        }

        // Vertical centre of text
        g2.setFont(FONT_ROW);
        FontMetrics fm = g2.getFontMetrics();
        int textY = rowY + (ROW_H - 4 + fm.getAscent() - fm.getDescent()) / 2;

        // Rank
        String rankStr = "#" + (rank + 1);
        g2.setColor(rank == 0 ? COL_RANK1 : COL_RANK);
        g2.drawString(rankStr,
                tableX + rankW - fm.stringWidth(rankStr),
                textY);

        // Player name
        g2.setColor(isMe ? new Color(255, 180, 80) : COL_NAME);
        g2.drawString(clamp(e.getPlayerName(), fm, nameW - 8), tableX + rankW, textY);

        // Character
        g2.setColor(COL_CHAR);
        g2.drawString(clamp(e.getCharacterName(), fm, charW - 8),
                tableX + rankW + nameW, textY);

        // Battles won
        g2.setColor(COL_BATTLES);
        g2.drawString(String.valueOf(e.getBattlesWon()),
                tableX + rankW + nameW + charW, textY);

        // Coins (right-aligned within column)
        g2.setColor(COL_COINS);
        String coinsStr = String.valueOf(e.getCoins());
        g2.drawString(coinsStr,
                tableX + rankW + nameW + charW + battlesW + coinsW - fm.stringWidth(coinsStr),
                textY);

        // Date (right-aligned)
        g2.setColor(COL_DATE);
        g2.setFont(new Font("Courier New", Font.PLAIN, 12));
        FontMetrics fmSm = g2.getFontMetrics();
        g2.drawString(e.getDate(),
                tableX + tableW - fmSm.stringWidth(e.getDate()),
                textY);
    }

    /** Truncate with ellipsis if text exceeds maxPx. */
    private String clamp(String text, FontMetrics fm, int maxPx) {
        if (fm.stringWidth(text) <= maxPx) return text;
        while (text.length() > 1 && fm.stringWidth(text + "…") > maxPx)
            text = text.substring(0, text.length() - 1);
        return text + "…";
    }

    // ── Neon arcade button ────────────────────────────────────────────────────
    private void drawNeonButton(Graphics2D g2, Rectangle rect, boolean hover, String label) {
        if (rect.width == 0) return;
        Rectangle r = hover
                ? new Rectangle(rect.x - 4, rect.y - 4, rect.width + 8, rect.height + 8)
                : rect;
        g2.setColor(hover ? COL_TITLE : new Color(180, 140, 0));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        g2.setColor(hover ? new Color(50, 40, 0) : new Color(22, 18, 0));
        g2.fillRoundRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 9, 9);
        g2.setFont(FONT_BTN);
        g2.setColor(hover ? COL_TITLE : new Color(200, 155, 25));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label,
                r.x + (r.width  - fm.stringWidth(label)) / 2,
                r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2);
    }

    // ── CRT scanlines ─────────────────────────────────────────────────────────
    private void drawScanlines(Graphics2D g2, int w, int h) {
        if (scanlines == null || scanlines.getWidth() != w || scanlines.getHeight() != h) {
            scanlines = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D sg = scanlines.createGraphics();
            sg.setColor(new Color(0, 0, 0, 20));
            for (int y = 0; y < h; y += 4) sg.drawLine(0, y, w, y);
            sg.dispose();
        }
        g2.drawImage(scanlines, 0, 0, null);
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────
    private void addMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverAgain = playAgainRect.contains(e.getPoint());
                hoverBack  = backMenuRect.contains(e.getPoint());
                setCursor((hoverAgain || hoverBack)
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (playAgainRect.contains(e.getPoint())) {
                    gameWindow.switchScreen(new ArcadeNameEntryScreen(gameWindow));
                } else if (backMenuRect.contains(e.getPoint())) {
                    gameWindow.switchScreen(new HomeScreen(gameWindow));
                }
            }
        });
    }
}
