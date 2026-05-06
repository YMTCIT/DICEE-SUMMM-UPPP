import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class SettingsScreen extends JPanel {

    private static final String BG_PATH       = "assets/backgrounds/background_settings.png";
    private static final String BTN_MUTE_PATH = "assets/buttons/btn_mute.png";
    private static final String BTN_BACK_PATH = "assets/buttons/btn_back.png";

    private static final double BTN_CENTER_X = 0.50;
    private static final double MUTE_Y       = 0.50;
    private static final double KHAI_Y       = 0.575;   // sits between MUTE and BACK
    private static final double BACK_Y       = 0.65;

    private static final int BTN_WIDTH  = 250;
    private static final int BTN_HEIGHT = 55;

    // Volume slider settings
    private static final int SLIDER_WIDTH  = 300;
    private static final int SLIDER_HEIGHT = 20;

    private final GameWindow gameWindow;

    private BufferedImage bgImage;
    private BufferedImage btnMuteImg;
    private BufferedImage btnBackImg;

    private Rectangle muteRect     = new Rectangle();
    private Rectangle khaiModeRect = new Rectangle();
    private Rectangle backRect     = new Rectangle();

    private boolean hoverMute  = false;
    private boolean hoverKhai  = false;
    private boolean hoverBack  = false;
    private boolean isMuted    = false;

    // KhaiMode — synced with the singleton on construction
    private boolean khaiMode = GameSettings.get().isKhaiMode();

    // Volume slider (0 to 100)
    private int volume        = 75;
    private Rectangle sliderTrack  = new Rectangle();
    private Rectangle sliderHandle = new Rectangle();
    private boolean draggingSlider = false;
    private FontMetrics fm;

    public SettingsScreen(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(null);
        loadImages();
        addMouseListeners();
    }

    private void loadImages() {
        bgImage    = loadImage(BG_PATH);
        btnMuteImg = loadImage(BTN_MUTE_PATH);
        btnBackImg = loadImage(BTN_BACK_PATH);
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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        // ── Background ────────────────────────────────────────────────────────
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, w, h, null);
        } else {
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(0, 0, w, h);
        }

        // ── Volume label ──────────────────────────────────────────────────────
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(new Color(0x93, 0x26, 0x00));
        String volLabel = "MUSIC VOLUME: " + volume + "%";
        fm = g2.getFontMetrics();
        g2.drawString(volLabel, (w - fm.stringWidth(volLabel)) / 2, (int)(h * 0.30));

        // ── Volume slider track ───────────────────────────────────────────────
        int sx = (w - SLIDER_WIDTH) / 2;
        int sy = (int)(h * 0.38);
        sliderTrack.setBounds(sx, sy, SLIDER_WIDTH, SLIDER_HEIGHT);

        g2.setColor(new Color(80, 80, 80, 180));
        g2.fillRoundRect(sx, sy, SLIDER_WIDTH, SLIDER_HEIGHT, 10, 10);

        int filledWidth = (int)(SLIDER_WIDTH * (volume / 100.0));
        g2.setColor(new Color(200, 120, 50));
        g2.fillRoundRect(sx, sy, filledWidth, SLIDER_HEIGHT, 10, 10);

        int hx = sx + filledWidth - 10;
        int hy = sy - 5;
        sliderHandle.setBounds(hx, hy, 20, SLIDER_HEIGHT + 10);
        g2.setColor(Color.WHITE);
        g2.fillOval(hx, hy, 20, SLIDER_HEIGHT + 10);

        // ── Button positions ──────────────────────────────────────────────────
        int bx = (int)(w * BTN_CENTER_X) - BTN_WIDTH / 2;
        updateRect(muteRect,     bx, (int)(h * MUTE_Y));
        updateRect(khaiModeRect, bx, (int)(h * KHAI_Y));
        updateRect(backRect,     bx, (int)(h * BACK_Y));

        // ── MUTE button ───────────────────────────────────────────────────────
        String muteLabel = isMuted ? "UNMUTE" : "MUTE";
        drawButton(g2, btnMuteImg, muteRect, hoverMute, muteLabel);

        // ── KhaiMode button (neon gold when ON, grey when OFF) ────────────────
        drawKhaiButton(g2, khaiModeRect, hoverKhai);

        // ── BACK button ───────────────────────────────────────────────────────
        drawButton(g2, btnBackImg, backRect, hoverBack, "RETURN");
    }

    // ── Standard image/fallback button ────────────────────────────────────────
    private void drawButton(Graphics2D g2, BufferedImage img,
                            Rectangle rect, boolean hover, String label) {
        Rectangle r = hover ? expandRect(rect, 4) : rect;

        if (img != null) {
            if (!hover) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
            }
            g2.drawImage(img, r.x, r.y, r.width, r.height, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            g2.setColor(hover ? new Color(200, 200, 255) : new Color(150, 150, 220));
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            FontMetrics fm = g2.getFontMetrics();
            int tx = r.x + (r.width  - fm.stringWidth(label)) / 2;
            int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(label, tx, ty);
        }
    }

    // ── KhaiMode neon toggle button ───────────────────────────────────────────
    private void drawKhaiButton(Graphics2D g2, Rectangle rect, boolean hover) {
        Rectangle r = hover ? expandRect(rect, 4) : rect;

        // Border colour: gold when ON, grey when OFF
        Color borderCol = khaiMode
                ? new Color(255, 200, 0)
                : new Color(130, 130, 130);
        Color fillCol = khaiMode
                ? new Color(50, 40, 0, 220)
                : new Color(30, 30, 30, 180);
        Color textCol = khaiMode
                ? (hover ? new Color(255, 230, 80) : new Color(255, 200, 0))
                : (hover ? new Color(200, 200, 200) : new Color(140, 140, 140));

        // Outer glow when ON
        if (khaiMode) {
            g2.setColor(new Color(255, 200, 0, hover ? 60 : 30));
            g2.fillRoundRect(r.x - 4, r.y - 4, r.width + 8, r.height + 8, 16, 16);
        }

        g2.setColor(borderCol);
        g2.setStroke(new BasicStroke(khaiMode ? 2.5f : 1.5f));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);

        g2.setColor(fillCol);
        g2.fillRoundRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 11, 11);

        // Label: "KHAIMODE: ON" / "KHAIMODE: OFF"
        String label = khaiMode ? "KHAIMODE: ON" : "KHAIMODE: OFF";
        g2.setFont(new Font("Courier New", Font.BOLD, 16));
        g2.setColor(textCol);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label,
                r.x + (r.width  - fm.stringWidth(label)) / 2,
                r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2);
    }

    // ── Mouse listeners ───────────────────────────────────────────────────────
    private void addMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverMute = muteRect.contains(e.getPoint());
                hoverKhai = khaiModeRect.contains(e.getPoint());
                hoverBack = backRect.contains(e.getPoint());

                if (hoverMute || hoverKhai || hoverBack) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else if (sliderTrack.contains(e.getPoint())) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggingSlider) {
                    int newX = e.getX();
                    int clampedX = Math.max(sliderTrack.x,
                            Math.min(newX, sliderTrack.x + sliderTrack.width));
                    volume = (int)(((clampedX - sliderTrack.x) / (double) sliderTrack.width) * 100);
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (sliderTrack.contains(e.getPoint()) ||
                        sliderHandle.contains(e.getPoint())) {
                    draggingSlider = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggingSlider = false;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (muteRect.contains(e.getPoint())) {
                    onMuteClicked();
                } else if (khaiModeRect.contains(e.getPoint())) {
                    onKhaiModeClicked();
                } else if (backRect.contains(e.getPoint())) {
                    onBackClicked();
                }
            }
        });
    }

    private void onMuteClicked() {
        isMuted = !isMuted;
        if (isMuted) {
            System.out.println("Muted!");
            // TODO: AudioManager.mute();
        } else {
            System.out.println("Unmuted!");
            // TODO: AudioManager.unmute();
        }
        repaint();
    }

    private void onKhaiModeClicked() {
        khaiMode = !khaiMode;
        GameSettings.get().setKhaiMode(khaiMode);
        System.out.println("KhaiMode: " + (khaiMode ? "ON" : "OFF"));
        repaint();
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
