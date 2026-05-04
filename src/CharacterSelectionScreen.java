import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class CharacterSelectionScreen extends JPanel {

    private static final String BG_PATH           = "assets/backgrounds/background_charselect.png";
    private static final String BTN_CONFIRM_PATH  = "assets/buttons/btn_confirm.png";
    private static final String BTN_BACK_PATH     = "assets/buttons/btn_back.png";
    private static final String ARROW_LEFT_PATH   = "assets/ui/arrow_left.png";
    private static final String ARROW_RIGHT_PATH  = "assets/ui/arrow_right.png";
    private static final String CARD_BG_PATH      = "assets/ui/card_bg.png";
    private static final String PLAYER1_IMG_PATH  = "assets/ui/player1.png";
    private static final String PLAYER2_IMG_PATH  = "assets/ui/player2.png";
    private static final String PLAYER_IMG_PATH   = "assets/ui/player.png";
    private static final String COMPUTER_IMG_PATH = "assets/ui/computer.png";

    private static final int BTN_WIDTH    = 250;
    private static final int BTN_HEIGHT   = 55;
    private static final int ARROW_SIZE   = 76;
    private static final int CARD_WIDTH   = 270;
    private static final int CARD_HEIGHT  = 380;
    private static final int BADGE_W      = 80;
    private static final int BADGE_H      = 40;
    private static final int PORTRAIT_AREA_W = 220;
    private static final int PORTRAIT_AREA_H = 150;
    private static final int PORTRAIT_TOP    = 12;
    private static final int NAME_BAR_H      = 28;

    private static final String[][] CHARACTERS = {
            { "Echo",   "Assassin", "Osaka, Japan",       "80",  "4.0x", "Phantom Dance - Dodge next 2 attacks (CD: 5 turns)" },
            { "Zyah",   "Assassin", "Kingston, Jamaica",  "80",  "4.0x", "Dancehall Fever - Guaranteed extra turn (CD: 4 turns)" },
            { "Raze",   "Fighter",  "Seoul, South Korea", "115", "3.0x", "Blazing Combo - +8 damage for 3 attacks (CD: 5 turns)" },
            { "Vibe",   "Fighter",  "Milan, Italy",       "110", "3.0x", "House Foundation - 50% dmg reduction 2 turns (CD: 4 turns)" },
            { "Torque", "Tank",     "Los Angeles, USA",   "150", "2.0x", "Earthquake Stomp - Stun enemy 2 turns (CD: 4 turns)" },
            { "Luma",   "Tank",     "Sao Paulo, Brazil",  "140", "2.0x", "Radiant Burst - Heal 30 HP (CD: 4 turns)" },
            { "Lyric",  "Support",  "Paris, France",      "105", "2.0x", "Healing Freestyle - Heal 35 HP (CD: 3 turns)" },
            { "Ayo",    "Support",  "Lagos, Nigeria",     "100", "2.0x", "Ancestral Call - Revive 50% HP (CD: 5 turns, once/battle)" },
    };

    private static final String[] PORTRAIT_FILES = {
            "assets/characters/portraits/echo.gif",
            "assets/characters/portraits/zyah.gif",
            "assets/characters/portraits/raze.gif",
            "assets/characters/portraits/vibee.gif",
            "assets/characters/portraits/torque.gif",
            "assets/characters/portraits/luma.gif",
            "assets/characters/portraits/lyric.gif",
            "assets/characters/portraits/ayo.gif"
    };

    private static final Color COLOR_ASSASSIN = new Color(180, 50,  50);
    private static final Color COLOR_FIGHTER  = new Color(200, 120, 30);
    private static final Color COLOR_TANK     = new Color(50,  100, 200);
    private static final Color COLOR_SUPPORT  = new Color(50,  180, 100);

    private final GameWindow gameWindow;
    private final String     gameMode;

    // ── NEW: arcade player name (null for PVP / PVC) ─────────────────────────
    private final String arcadePlayerName;

    private int currentPlayer = 1;
    private int p1Choice      = -1;
    private int currentIndex  = 0;

    private BufferedImage bgImage, btnConfirmImg, btnBackImg;
    private BufferedImage arrowLeftImg, arrowRightImg, cardBgImg;
    private BufferedImage player1Img, player2Img, playerImg, computerImg;
    private final Image[] portraitImages = new Image[8];

    private final Rectangle arrowLeftRect  = new Rectangle();
    private final Rectangle arrowRightRect = new Rectangle();
    private final Rectangle confirmRect    = new Rectangle();
    private final Rectangle backRect       = new Rectangle();

    private boolean hoverLeft    = false;
    private boolean hoverRight   = false;
    private boolean hoverConfirm = false;
    private boolean hoverBack    = false;

    // ── Original 2-arg constructor — PVP / PVC (unchanged behaviour) ─────────
    public CharacterSelectionScreen(GameWindow gameWindow, String gameMode) {
        this(gameWindow, gameMode, null);
    }

    // ── NEW 3-arg constructor — Arcade passes a player name ──────────────────
    public CharacterSelectionScreen(GameWindow gameWindow, String gameMode, String playerName) {
        this.gameWindow       = gameWindow;
        this.gameMode         = gameMode;
        this.arcadePlayerName = playerName;
        setLayout(null);
        loadImages();
        addMouseListeners();
    }

    // ── Image loading ─────────────────────────────────────────────────────────
    private void loadImages() {
        bgImage       = loadImage(BG_PATH);
        btnConfirmImg = loadImage(BTN_CONFIRM_PATH);
        btnBackImg    = loadImage(BTN_BACK_PATH);
        arrowLeftImg  = loadImage(ARROW_LEFT_PATH);
        arrowRightImg = loadImage(ARROW_RIGHT_PATH);
        cardBgImg     = loadImage(CARD_BG_PATH);
        player1Img    = loadImage(PLAYER1_IMG_PATH);
        player2Img    = loadImage(PLAYER2_IMG_PATH);
        playerImg     = loadImage(PLAYER_IMG_PATH);
        computerImg   = loadImage(COMPUTER_IMG_PATH);

        for (int i = 0; i < PORTRAIT_FILES.length; i++) {
            File f = new File(PORTRAIT_FILES[i]);
            if (f.exists()) portraitImages[i] = new ImageIcon(PORTRAIT_FILES[i]).getImage();
        }
    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Could not load: " + path);
            return null;
        }
    }

    // ── Paint ─────────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth(), h = getHeight();

        if (bgImage != null) g2.drawImage(bgImage, 0, 0, w, h, null);
        else { g2.setColor(new Color(20, 15, 10)); g2.fillRect(0, 0, w, h); }

        int cardX = (w - CARD_WIDTH)  / 2;
        int cardY = Math.max(115, (h - CARD_HEIGHT) / 2 - 8);

        drawSideBadges(g2, w);
        drawCard(g2, currentIndex, cardX, cardY);

        int arrowY = cardY + 80;
        arrowLeftRect.setBounds(cardX - ARROW_SIZE - 18, arrowY, ARROW_SIZE, ARROW_SIZE);
        arrowRightRect.setBounds(cardX + CARD_WIDTH + 18, arrowY, ARROW_SIZE, ARROW_SIZE);
        drawArrow(g2, arrowLeftImg,  arrowLeftRect,  hoverLeft,  "<");
        drawArrow(g2, arrowRightImg, arrowRightRect, hoverRight, ">");

        String counter = (currentIndex + 1) + "/8";
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(Color.WHITE);
        FontMetrics fmC = g2.getFontMetrics();
        g2.drawString(counter, (w - fmC.stringWidth(counter)) / 2, cardY + CARD_HEIGHT + 18);

        confirmRect.setBounds((w - BTN_WIDTH) / 2, cardY + CARD_HEIGHT + 32, BTN_WIDTH, BTN_HEIGHT);
        drawButton(g2, btnConfirmImg, confirmRect, hoverConfirm, "CONFIRM");

        backRect.setBounds(14, 18, 150, 44);
        drawButton(g2, btnBackImg, backRect, hoverBack, "RETURN");

        // Arcade: show current player name tag at bottom-left
        if ("ARCADE".equals(gameMode) && arcadePlayerName != null) {
            g2.setFont(new Font("Courier New", Font.BOLD, 13));
            g2.setColor(new Color(255, 200, 0));
            g2.drawString("PLAYER: " + arcadePlayerName, 18, h - 18);
        }

        // PVP only: show P1 chosen indicator
        if (currentPlayer == 2 && p1Choice >= 0 && !"ARCADE".equals(gameMode)) {
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.setColor(new Color(110, 180, 255));
            g2.drawString("P1 chose: " + CHARACTERS[p1Choice][0], 18, h - 18);
        }

        g2.dispose();
    }

    // ── All drawing helpers are identical to the original ─────────────────────

    private void drawSideBadges(Graphics2D g2, int panelWidth) {
        int y = 118;
        if ("PVP".equals(gameMode)) {
            drawBadge(g2, player1Img, 70, y, currentPlayer == 1, "P1", new Color(190, 30, 30));
            drawBadge(g2, player2Img, panelWidth - 70 - BADGE_W, y, currentPlayer == 2, "P2", new Color(40, 110, 220));
        } else if ("PVC".equals(gameMode)) {
            drawBadge(g2, playerImg, 70, y, currentPlayer == 1, "P1", new Color(190, 30, 30));
            drawBadge(g2, computerImg, panelWidth - 70 - BADGE_W, y, currentPlayer == 2, "CPU", new Color(40, 110, 220));
        } else {
            // ARCADE — single badge
            drawBadge(g2, playerImg, (panelWidth - BADGE_W) / 2, 88, true, "P1", new Color(190, 30, 30));
        }
    }

    private void drawBadge(Graphics2D g2, BufferedImage img, int x, int y,
                           boolean active, String fallback, Color fallbackColor) {
        Graphics2D gb = (Graphics2D) g2.create();
        gb.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, active ? 1.0f : 0.45f));
        if (img != null) {
            gb.drawImage(img, x, y, BADGE_W, BADGE_H, null);
        } else {
            gb.setColor(fallbackColor);
            gb.fillRoundRect(x, y, BADGE_W, BADGE_H, 8, 8);
            gb.setColor(Color.WHITE);
            gb.setFont(new Font("Monospaced", Font.BOLD, 18));
            FontMetrics fm = gb.getFontMetrics();
            gb.drawString(fallback, x + (BADGE_W - fm.stringWidth(fallback)) / 2,
                    y + (BADGE_H + fm.getAscent() - fm.getDescent()) / 2);
        }
        gb.dispose();
    }

    private void drawCard(Graphics2D g2, int index, int x, int y) {
        String[] data = CHARACTERS[index];
        Color classColor = getClassColor(data[1]);

        if (cardBgImg != null) g2.drawImage(cardBgImg, x, y, CARD_WIDTH, CARD_HEIGHT, null);
        else {
            g2.setColor(new Color(223, 202, 177));
            g2.fillRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 16, 16);
            g2.setColor(new Color(143, 93, 53));
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 16, 16);
        }

        int portraitAreaX = x + (CARD_WIDTH - PORTRAIT_AREA_W) / 2;
        int portraitAreaY = y + PORTRAIT_TOP;
        drawPortraitDirectlyOnCard(g2, portraitImages[index], portraitAreaX, portraitAreaY,
                PORTRAIT_AREA_W, PORTRAIT_AREA_H);

        int nameBarY = y + 162;
        GradientPaint gp = new GradientPaint(x, nameBarY, new Color(194, 194, 194),
                x, nameBarY + NAME_BAR_H, new Color(118, 118, 118));
        g2.setPaint(gp);
        g2.fillRect(x + 2, nameBarY, CARD_WIDTH - 4, NAME_BAR_H);
        g2.setColor(new Color(220, 220, 220, 150));
        g2.drawLine(x + 2, nameBarY, x + CARD_WIDTH - 2, nameBarY);

        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(Color.WHITE);
        FontMetrics fmN = g2.getFontMetrics();
        g2.drawString(data[0], x + (CARD_WIDTH - fmN.stringWidth(data[0])) / 2,
                nameBarY + (NAME_BAR_H + fmN.getAscent() - fmN.getDescent()) / 2);

        int roleBadgeW = 92, roleBadgeH = 20;
        int roleBadgeX = x + (CARD_WIDTH - roleBadgeW) / 2;
        int roleBadgeY = nameBarY + NAME_BAR_H - 2;
        g2.setColor(classColor);
        g2.fillRoundRect(roleBadgeX, roleBadgeY, roleBadgeW, roleBadgeH, 10, 10);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(Color.WHITE);
        FontMetrics fmR = g2.getFontMetrics();
        String roleText = data[1].toUpperCase();
        g2.drawString(roleText, roleBadgeX + (roleBadgeW - fmR.stringWidth(roleText)) / 2,
                roleBadgeY + (roleBadgeH + fmR.getAscent() - fmR.getDescent()) / 2);

        int contentY = roleBadgeY + roleBadgeH + 18;
        int labelX = x + 16, valueX = x + 102;
        g2.setColor(new Color(155, 120, 85, 110));
        g2.drawLine(x + 14, contentY - 10, x + CARD_WIDTH - 14, contentY - 10);

        drawStatRow(g2, labelX, valueX, contentY, "Origin",      data[2]); contentY += 22;
        drawStatRow(g2, labelX, valueX, contentY, "Health",      data[3] + " HP"); contentY += 22;
        drawStatRow(g2, labelX, valueX, contentY, "Base Damage", data[4]); contentY += 28;

        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(classColor);
        g2.drawString("Special Skill", labelX, contentY); contentY += 14;

        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.setColor(new Color(50, 34, 20));
        drawWrappedText(g2, data[5], labelX, contentY, CARD_WIDTH - 32, 13);
    }

    private void drawPortraitDirectlyOnCard(Graphics2D g2, Image image, int x, int y, int areaW, int areaH) {
        if (image == null) return;
        int iw = image.getWidth(this), ih = image.getHeight(this);
        if (iw <= 0 || ih <= 0) { g2.drawImage(image, x, y, areaW, areaH, this); return; }
        double scale = Math.min((double) areaW / iw, (double) areaH / ih);
        int drawW = Math.max(1, (int) Math.round(iw * scale));
        int drawH = Math.max(1, (int) Math.round(ih * scale));
        int drawX = x + (areaW - drawW) / 2;
        int drawY = y + areaH - drawH;
        g2.setColor(new Color(0, 0, 0, 35));
        g2.fillOval(x + 30, y + areaH - 10, areaW - 60, 12);
        g2.drawImage(image, drawX, drawY, drawW, drawH, this);
    }

    private void drawStatRow(Graphics2D g2, int lx, int vx, int y, String label, String value) {
        g2.setFont(new Font("Arial", Font.BOLD,  12)); g2.setColor(new Color(65, 45, 25));
        g2.drawString(label + ":", lx, y);
        g2.setFont(new Font("Arial", Font.PLAIN, 12)); g2.setColor(new Color(28, 20, 10));
        g2.drawString(value, vx, y);
    }

    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxW, int lineH) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(test) > maxW && line.length() > 0) {
                g2.drawString(line.toString(), x, y); y += lineH; line = new StringBuilder(word);
            } else { if (line.length() > 0) line.append(" "); line.append(word); }
        }
        if (line.length() > 0) g2.drawString(line.toString(), x, y);
    }

    private Color getClassColor(String cls) {
        switch (cls) {
            case "Assassin": return COLOR_ASSASSIN;
            case "Fighter":  return COLOR_FIGHTER;
            case "Tank":     return COLOR_TANK;
            case "Support":  return COLOR_SUPPORT;
            default:         return Color.GRAY;
        }
    }

    private void drawArrow(Graphics2D g2, BufferedImage img, Rectangle rect, boolean hover, String fb) {
        if (img != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hover ? 1.0f : 0.80f));
            g2.drawImage(img, rect.x + (hover ? -2 : 0), rect.y + (hover ? -2 : 0),
                    rect.width, rect.height, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            g2.setColor(hover ? new Color(255, 90, 90) : new Color(200, 55, 55));
            int[] px, py;
            if ("<".equals(fb)) { px = new int[]{rect.x+rect.width,rect.x,rect.x+rect.width}; py = new int[]{rect.y,rect.y+rect.height/2,rect.y+rect.height}; }
            else                { px = new int[]{rect.x,rect.x+rect.width,rect.x};            py = new int[]{rect.y,rect.y+rect.height/2,rect.y+rect.height}; }
            g2.fillPolygon(px, py, 3);
        }
    }

    private void drawButton(Graphics2D g2, BufferedImage img, Rectangle rect, boolean hover, String label) {
        Rectangle r = hover ? expandRect(rect, 3) : rect;
        if (img != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hover ? 1.0f : 0.88f));
            g2.drawImage(img, r.x, r.y, r.width, r.height, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            g2.setColor(hover ? new Color(200, 200, 255) : new Color(150, 150, 220));
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, r.x + (r.width - fm.stringWidth(label)) / 2,
                    r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2);
        }
    }

    // ── Mouse listeners ───────────────────────────────────────────────────────
    private void addMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverLeft    = arrowLeftRect.contains(e.getPoint());
                hoverRight   = arrowRightRect.contains(e.getPoint());
                hoverConfirm = confirmRect.contains(e.getPoint());
                hoverBack    = backRect.contains(e.getPoint());
                setCursor((hoverLeft || hoverRight || hoverConfirm || hoverBack)
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (arrowLeftRect.contains(e.getPoint())) {
                    currentIndex = (currentIndex - 1 + CHARACTERS.length) % CHARACTERS.length;
                    repaint();
                } else if (arrowRightRect.contains(e.getPoint())) {
                    currentIndex = (currentIndex + 1) % CHARACTERS.length;
                    repaint();
                } else if (confirmRect.contains(e.getPoint())) {
                    onConfirmClicked();
                } else if (backRect.contains(e.getPoint())) {
                    onBackClicked();
                }
            }
        });
    }

    // ── Confirm logic ─────────────────────────────────────────────────────────
    private void onConfirmClicked() {
        // ── ARCADE: single-pick → hand off to ArcadeManager with player name ──
        if ("ARCADE".equals(gameMode)) {
            int chosenIndex = currentIndex;
            // Pass playerName so ArcadeManager can record it for the leaderboard
            ArcadeManager manager = new ArcadeManager(gameWindow, chosenIndex, arcadePlayerName);
            manager.startNext();
            return;
        }

        // ── PVP / PVC: original two-player selection (unchanged) ──────────────
        if (currentPlayer == 1) {
            p1Choice      = currentIndex;
            currentPlayer = 2;
            currentIndex  = (p1Choice + 1) % CHARACTERS.length;
            repaint();
        } else {
            if (currentIndex == p1Choice) return; // same character — silent block
            int p2Choice = currentIndex;
            String p1Label = "PVP".equals(gameMode) ? "Player 1" : "Player";
            String p2Label = "PVP".equals(gameMode) ? "Player 2" : "Computer";
            gameWindow.switchScreen(
                    new VersusScreen(gameWindow, p1Choice, p2Choice, gameMode, p1Label, p2Label));
        }
    }

    // ── Back logic ────────────────────────────────────────────────────────────
    private void onBackClicked() {
        // ── ARCADE back → return to name entry ────────────────────────────────
        if ("ARCADE".equals(gameMode)) {
            gameWindow.switchScreen(new ArcadeNameEntryScreen(gameWindow));
            return;
        }

        // ── PVP / PVC back: original logic (unchanged) ────────────────────────
        if (currentPlayer == 2) {
            currentPlayer = 1;
            currentIndex  = p1Choice;
            p1Choice      = -1;
            repaint();
        } else {
            gameWindow.switchScreen(new GameModeScreen(gameWindow));
        }
    }

    private Rectangle expandRect(Rectangle r, int amount) {
        return new Rectangle(r.x - amount, r.y - amount,
                r.width + amount * 2, r.height + amount * 2);
    }
}
