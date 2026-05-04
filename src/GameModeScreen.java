import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class GameModeScreen extends JPanel {

    private static final String BG_PATH       = "assets/backgrounds/background_gamemode.png";
    private static final String BTN_PVP_PATH  = "assets/buttons/btn_pvp.png";
    private static final String BTN_PVC_PATH  = "assets/buttons/btn_pvc.png";
    private static final String BTN_ARC_PATH  = "assets/buttons/btn_arcade.png";
    private static final String BTN_BACK_PATH = "assets/buttons/btn_back.png";

    // ── Button layout ─────────────────────────────────────────────────────────
    private static final double BTN_CENTER_X = 0.48;
    private static final double PVP_Y        = 0.35;
    private static final double PVC_Y        = 0.45;
    private static final double ARCADE_Y     = 0.55;
    private static final double BACK_Y       = 0.65;

    private static final int BTN_WIDTH  = 250;
    private static final int BTN_HEIGHT = 60;

    // Leaderboard button sits to the RIGHT of the ARCADE button, same Y
    private static final int LB_WIDTH  = 170;
    private static final int LB_HEIGHT = 50;
    private static final int LB_GAP    = 16;   // gap between ARCADE right edge and LB left edge

    private final GameWindow gameWindow;

    private BufferedImage bgImage;
    private BufferedImage btnPvpImg;
    private BufferedImage btnPvcImg;
    private BufferedImage btnArcadeImg;
    private BufferedImage btnBackImg;

    private Rectangle pvpRect         = new Rectangle();
    private Rectangle pvcRect         = new Rectangle();
    private Rectangle arcadeRect      = new Rectangle();
    private Rectangle backRect        = new Rectangle();
    private Rectangle leaderboardRect = new Rectangle();   // NEW

    private boolean hoverPvp         = false;
    private boolean hoverPvc         = false;
    private boolean hoverArcade      = false;
    private boolean hoverBack        = false;
    private boolean hoverLeaderboard = false;              // NEW

    public GameModeScreen(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(null);
        loadImages();
        addMouseListeners();
    }

    private void loadImages() {
        bgImage      = loadImage(BG_PATH);
        btnPvpImg    = loadImage(BTN_PVP_PATH);
        btnPvcImg    = loadImage(BTN_PVC_PATH);
        btnArcadeImg = loadImage(BTN_ARC_PATH);
        btnBackImg   = loadImage(BTN_BACK_PATH);
    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Could not load image: " + path);
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        if (bgImage != null) g2.drawImage(bgImage, 0, 0, w, h, null);
        else { g2.setColor(Color.DARK_GRAY); g2.fillRect(0, 0, w, h); }

        // ── Regular buttons ───────────────────────────────────────────────────
        int bx = (int)(w * BTN_CENTER_X) - BTN_WIDTH / 2;
        updateRect(pvpRect,    bx, (int)(h * PVP_Y));
        updateRect(pvcRect,    bx, (int)(h * PVC_Y));
        updateRect(arcadeRect, bx, (int)(h * ARCADE_Y));
        updateRect(backRect,   bx, (int)(h * BACK_Y));

        drawButton(g2, btnPvpImg,    pvpRect,    hoverPvp,    "PVP");
        drawButton(g2, btnPvcImg,    pvcRect,    hoverPvc,    "PVC");
        drawButton(g2, btnArcadeImg, arcadeRect, hoverArcade, "ARCADE MODE");
        drawButton(g2, btnBackImg,   backRect,   hoverBack,   "BACK");

        // ── Leaderboard button — to the right of ARCADE at the same Y ─────────
        int lbX = arcadeRect.x + arcadeRect.width + LB_GAP;
        int lbY = arcadeRect.y + (BTN_HEIGHT - LB_HEIGHT) / 2;
        leaderboardRect.setBounds(lbX, lbY, LB_WIDTH, LB_HEIGHT);
        drawLeaderboardButton(g2, leaderboardRect, hoverLeaderboard);
    }

    // ── Regular button (reuses existing pattern) ──────────────────────────────
    private void drawButton(Graphics2D g2, BufferedImage img,
                            Rectangle rect, boolean hover, String label) {
        Rectangle r = hover ? expandRect(rect, 4) : rect;
        if (img != null) {
            if (!hover)
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
            g2.drawImage(img, r.x, r.y, r.width, r.height, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            g2.setColor(hover ? new Color(200, 200, 255) : new Color(150, 150, 220));
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label,
                    r.x + (r.width  - fm.stringWidth(label)) / 2,
                    r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2);
        }
    }

    // ── Leaderboard button — arcade-neon style with crown ────────────────────
    private void drawLeaderboardButton(Graphics2D g2, Rectangle rect, boolean hover) {
        Rectangle r = hover
                ? new Rectangle(rect.x - 3, rect.y - 3, rect.width + 6, rect.height + 6)
                : rect;

        // Neon border
        g2.setColor(hover ? new Color(255, 220, 0) : new Color(180, 140, 0));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);

        // Dark fill
        g2.setColor(hover ? new Color(40, 32, 0) : new Color(20, 16, 0));
        g2.fillRoundRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 9, 9);

        // Crown + label
        g2.setFont(new Font("Courier New", Font.BOLD, 13));
        g2.setColor(hover ? new Color(255, 220, 0) : new Color(200, 158, 24));
        FontMetrics fm = g2.getFontMetrics();

        // Crown glyph
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fmCrown = g2.getFontMetrics();
        String crown = "♛";
        int crownW = fmCrown.stringWidth(crown);

        g2.setFont(new Font("Courier New", Font.BOLD, 12));
        FontMetrics fmLabel = g2.getFontMetrics();
        String label = " LEADERBOARD";
        int labelW   = fmLabel.stringWidth(label);

        int totalW = crownW + labelW;
        int startX = r.x + (r.width - totalW) / 2;
        int textY  = r.y + (r.height + fmLabel.getAscent() - fmLabel.getDescent()) / 2;

        // Draw crown in gold
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(hover ? new Color(255, 215, 0) : new Color(200, 160, 20));
        g2.drawString(crown, startX, textY);

        // Draw "LEADERBOARD" text
        g2.setFont(new Font("Courier New", Font.BOLD, 12));
        g2.setColor(hover ? new Color(255, 220, 0) : new Color(190, 148, 18));
        g2.drawString(label, startX + crownW, textY);
    }

    // ── Mouse listeners ───────────────────────────────────────────────────────
    private void addMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverPvp         = pvpRect.contains(e.getPoint());
                hoverPvc         = pvcRect.contains(e.getPoint());
                hoverArcade      = arcadeRect.contains(e.getPoint());
                hoverBack        = backRect.contains(e.getPoint());
                hoverLeaderboard = leaderboardRect.contains(e.getPoint());

                if (hoverPvp || hoverPvc || hoverArcade || hoverBack || hoverLeaderboard) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (pvpRect.contains(e.getPoint())) {
                    onPvpClicked();
                } else if (pvcRect.contains(e.getPoint())) {
                    onPvcClicked();
                } else if (arcadeRect.contains(e.getPoint())) {
                    onArcadeClicked();
                } else if (backRect.contains(e.getPoint())) {
                    onBackClicked();
                } else if (leaderboardRect.contains(e.getPoint())) {
                    onLeaderboardClicked();
                }
            }
        });
    }

    // ── PVP and PVC — completely unchanged ────────────────────────────────────
    private void onPvpClicked() {
        System.out.println("PVP clicked!");
        gameWindow.switchScreen(new CharacterSelectionScreen(gameWindow, "PVP"));
    }

    private void onPvcClicked() {
        System.out.println("PVC clicked!");
        gameWindow.switchScreen(new CharacterSelectionScreen(gameWindow, "PVC"));
    }

    // ── ARCADE — now routes through name entry ────────────────────────────────
    private void onArcadeClicked() {
        System.out.println("Arcade Mode clicked!");
        gameWindow.switchScreen(new ArcadeNameEntryScreen(gameWindow));
    }

    // ── LEADERBOARD — view-only (no current player highlight) ────────────────
    private void onLeaderboardClicked() {
        System.out.println("Leaderboard clicked!");
        gameWindow.switchScreen(new LeaderboardScreen(gameWindow));
    }

    private void onBackClicked() {
        gameWindow.switchScreen(new HomeScreen(gameWindow));
    }

    private void updateRect(Rectangle r, int x, int y) {
        r.setBounds(x, y, BTN_WIDTH, BTN_HEIGHT);
    }

    private Rectangle expandRect(Rectangle r, int amount) {
        return new Rectangle(r.x - amount, r.y - amount,
                r.width + amount * 2, r.height + amount * 2);
    }
}
