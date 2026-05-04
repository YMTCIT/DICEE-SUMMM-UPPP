import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ArcadeNameEntryScreen extends JPanel {

    private static final String BG_PATH      = "assets/backgrounds/background_gamemode.png";
    private static final String BTN_BACK_PATH = "assets/buttons/btn_back.png";

    private static final int MAX_NAME   = 12;
    private static final int INPUT_W    = 360;
    private static final int INPUT_H    = 56;
    private static final int BTN_WIDTH  = 230;
    private static final int BTN_HEIGHT = 52;

    private final GameWindow gameWindow;

    private BufferedImage bgImage;
    private BufferedImage btnBackImg;

    private String  playerName    = "";
    private boolean cursorVisible = true;

    private Rectangle confirmRect  = new Rectangle();
    private Rectangle backRect     = new Rectangle();
    private boolean   hoverConfirm = false;
    private boolean   hoverBack    = false;

    private final Timer blinkTimer;

    public ArcadeNameEntryScreen(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(null);
        setFocusable(true);
        loadImages();
        addMouseListeners();
        addKeyboardListener();

        blinkTimer = new Timer(500, e -> {
            cursorVisible = !cursorVisible;
            repaint();
        });
        blinkTimer.start();
    }

    private void loadImages() {
        bgImage    = loadImage(BG_PATH);
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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // ── Background ──────────────────────────────────────────────────────
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, w, h, null);
        } else {
            g2.setColor(new Color(20, 10, 30));
            g2.fillRect(0, 0, w, h);
        }

        // ── Dark center panel ────────────────────────────────────────────────
        int panelW = 500;
        int panelH = 310;
        int panelX = (w - panelW) / 2;
        int panelY = (h - panelH) / 2 - 20;

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 28, 28);
        g2.setColor(new Color(255, 200, 0, 90));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 28, 28);

        // Inner glow line
        g2.setColor(new Color(255, 200, 0, 30));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(panelX + 6, panelY + 6, panelW - 12, panelH - 12, 22, 22);

        // ── Arcade-style title ───────────────────────────────────────────────
        g2.setFont(new Font("Courier New", Font.BOLD, 14));
        g2.setColor(new Color(255, 180, 0, 160));
        FontMetrics fmSub = g2.getFontMetrics();
        String insert = "INSERT COIN TO CONTINUE";
        g2.drawString(insert, (w - fmSub.stringWidth(insert)) / 2, panelY + 28);

        g2.setFont(new Font("Courier New", Font.BOLD, 32));
        g2.setColor(new Color(255, 220, 100));
        FontMetrics fmTitle = g2.getFontMetrics();
        // Shadow
        g2.setColor(new Color(180, 60, 0));
        String title = "ENTER YOUR NAME";
        g2.drawString(title, (w - fmTitle.stringWidth(title)) / 2 + 2, panelY + 70);
        g2.setColor(new Color(255, 220, 100));
        g2.drawString(title, (w - fmTitle.stringWidth(title)) / 2, panelY + 68);

        // ── Sub-label ────────────────────────────────────────────────────────
        g2.setFont(new Font("Courier New", Font.PLAIN, 12));
        g2.setColor(new Color(160, 160, 160));
        FontMetrics fmHint = g2.getFontMetrics();
        String hint = "Max " + MAX_NAME + " chars  •  ENTER or button to confirm";
        g2.drawString(hint, (w - fmHint.stringWidth(hint)) / 2, panelY + 92);

        // ── Input box ────────────────────────────────────────────────────────
        int inputX = (w - INPUT_W) / 2;
        int inputY = panelY + 110;

        g2.setColor(new Color(15, 12, 35, 230));
        g2.fillRoundRect(inputX, inputY, INPUT_W, INPUT_H, 12, 12);
        g2.setColor(new Color(255, 200, 0, 180));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(inputX, inputY, INPUT_W, INPUT_H, 12, 12);

        // Typed text + blinking cursor
        String display = playerName + (cursorVisible ? "█" : " ");
        g2.setFont(new Font("Courier New", Font.BOLD, 28));
        g2.setColor(Color.WHITE);
        FontMetrics fmInput = g2.getFontMetrics();
        g2.drawString(display,
                inputX + (INPUT_W - fmInput.stringWidth(display)) / 2,
                inputY + (INPUT_H + fmInput.getAscent() - fmInput.getDescent()) / 2);

        // Character count
        g2.setFont(new Font("Courier New", Font.PLAIN, 11));
        g2.setColor(playerName.length() >= MAX_NAME
                ? new Color(255, 80, 80) : new Color(120, 120, 120));
        g2.drawString(playerName.length() + "/" + MAX_NAME,
                inputX + INPUT_W - 50, inputY + INPUT_H + 16);

        // ── CONFIRM button ───────────────────────────────────────────────────
        int bx = (w - BTN_WIDTH) / 2;
        int by = inputY + INPUT_H + 30;
        confirmRect.setBounds(bx, by, BTN_WIDTH, BTN_HEIGHT);
        drawArcadeButton(g2, confirmRect, hoverConfirm, "CONFIRM");

        // ── BACK button (top-left) ───────────────────────────────────────────
        backRect.setBounds(18, 18, 150, 42);
        drawBackButton(g2, btnBackImg, backRect, hoverBack);
    }

    private void drawArcadeButton(Graphics2D g2, Rectangle rect, boolean hover, String label) {
        Rectangle r = hover
                ? new Rectangle(rect.x - 4, rect.y - 4, rect.width + 8, rect.height + 8)
                : rect;
        // Neon border
        g2.setColor(hover ? new Color(255, 220, 0) : new Color(200, 150, 0));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        // Dark fill
        g2.setColor(hover ? new Color(50, 40, 0) : new Color(25, 20, 0));
        g2.fillRoundRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 9, 9);
        // Label
        g2.setFont(new Font("Courier New", Font.BOLD, 18));
        g2.setColor(hover ? new Color(255, 220, 0) : new Color(210, 165, 30));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label,
                r.x + (r.width  - fm.stringWidth(label)) / 2,
                r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2);
    }

    private void drawBackButton(Graphics2D g2, BufferedImage img,
                                Rectangle rect, boolean hover) {
        if (img != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    hover ? 1.0f : 0.85f));
            g2.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            g2.setColor(hover ? new Color(200, 200, 255) : new Color(150, 150, 220));
            g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Courier New", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("BACK",
                    rect.x + (rect.width  - fm.stringWidth("BACK")) / 2,
                    rect.y + (rect.height + fm.getAscent() - fm.getDescent()) / 2);
        }
    }

    private void addKeyboardListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    if (!playerName.isEmpty()) {
                        playerName = playerName.substring(0, playerName.length() - 1);
                        repaint();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onConfirm();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (c == KeyEvent.CHAR_UNDEFINED) return;
                if (c < 32 || c > 126)            return; // printable ASCII only
                if (playerName.length() < MAX_NAME) {
                    playerName += c;
                    repaint();
                }
            }
        });
    }

    private void addMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverConfirm = confirmRect.contains(e.getPoint());
                hoverBack    = backRect.contains(e.getPoint());
                setCursor((hoverConfirm || hoverBack)
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
                if (confirmRect.contains(e.getPoint()))      onConfirm();
                else if (backRect.contains(e.getPoint()))    onBack();
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    private void onConfirm() {
        String trimmed = playerName.trim();
        if (trimmed.isEmpty()) {
            playerName = "";
            repaint();
            return;
        }
        blinkTimer.stop();
        gameWindow.switchScreen(new CharacterSelectionScreen(gameWindow, "ARCADE", trimmed));
    }

    private void onBack() {
        blinkTimer.stop();
        gameWindow.switchScreen(new GameModeScreen(gameWindow));
    }
}
