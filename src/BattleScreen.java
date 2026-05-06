import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.awt.Toolkit;

public class BattleScreen extends JPanel {

    // ── Asset paths ───────────────────────────────────────────────────────────
    private static final String BG_PATH           = "assets/backgrounds/background_battlescreen.png";
    private static final String ARROW_PATH        = "assets/ui/arrow_indicator.png";
    private static final String BTN_ATTACK_PATH   = "assets/buttons/btn_attack.png";
    private static final String BTN_SPECIAL_PATH  = "assets/buttons/btn_special.png";
    private static final String BTN_DEFEND_PATH   = "assets/buttons/btn_defend.png";
    private static final String BTN_WILDCARD_PATH = "assets/buttons/btn_wildcard.png";
    private static final String HP_0_PATH         = "assets/healthbar/health_bar_0.png";
    private static final String HP_20_PATH        = "assets/healthbar/health_bar_20.png";
    private static final String HP_40_PATH        = "assets/healthbar/health_bar_40.png";
    private static final String HP_60_PATH        = "assets/healthbar/health_bar_60.png";
    private static final String HP_80_PATH        = "assets/healthbar/health_bar_80.png";
    private static final String HP_100_PATH       = "assets/healthbar/health_bar_100.png";

    // ── Idle portrait files ───────────────────────────────────────────────────
    private static final String[] SPRITE_FILES = {
            "assets/characters/portraits/echo.gif",
            "assets/characters/portraits/zyah.gif",
            "assets/characters/portraits/raze.gif",
            "assets/characters/portraits/vibee.gif",
            "assets/characters/portraits/torque.gif",
            "assets/characters/portraits/luma.gif",
            "assets/characters/portraits/lyric.gif",
            "assets/characters/portraits/ayo.gif"
    };

    private static final String[][] CHARACTERS = {
            { "Echo",   "Assassin", "80",  "4.0", "Phantom Dance - Dodge next 2 attacks (CD: 5 turns)"         },
            { "Zyah",   "Assassin", "80",  "4.0", "Dancehall Fever - Guaranteed extra turn (CD: 4 turns)"      },
            { "Raze",   "Fighter",  "115", "3.0", "Blazing Combo - +8 damage for 3 attacks (CD: 5 turns)"      },
            { "Vibe",   "Fighter",  "110", "3.0", "House Foundation - 50% dmg reduction 2 turns (CD: 4 turns)" },
            { "Torque", "Tank",     "150", "2.0", "Earthquake Stomp - Stun enemy 2 turns (CD: 4 turns)"        },
            { "Luma",   "Tank",     "140", "2.0", "Radiant Burst - Heal 30 HP (CD: 4 turns)"                   },
            { "Lyric",  "Support",  "105", "2.0", "Healing Freestyle - Heal 35 HP (CD: 3 turns)"               },
            { "Ayo",    "Support",  "100", "2.0", "Ancestral Call - Revive 50% HP (CD: 5 turns, once/battle)"  },
    };

    // ── Sprite & panel sizes ──────────────────────────────────────────────────
    private static final int SPRITE_W  = 160;
    private static final int SPRITE_H  = 210;
    private static final int HP_BAR_W  = 220;
    private static final int HP_BAR_H  = 41;
    private static final int PANEL_H   = 230;

    // ── In-place action animation timing ─────────────────────────────────────
    private static final int ANIM_ATTACK_MS   = 900;
    private static final int ANIM_DEFEND_MS   = 900;
    private static final int ANIM_ULT_MS      = 1200;
    private static final int ANIM_WILDCARD_MS = 1000;

    // ── Hit Shake Animation ───────────────────────────────────────────────────
    private int shakingPlayer    = 0;
    private int shakeFrames      = 0;
    private int shakeOffsetX     = 0;
    private int shakeOffsetY     = 0;
    private int spriteFlashAlpha = 0;
    private int shakeDir         = 1;
    private boolean isDefendFlash = false;

    private static final int SHAKE_TOTAL_FRAMES = 40;
    private static final int SHAKE_INTERVAL_MS  = 50;

    private Timer shakeTimer;

    // ── In-place action animation system ─────────────────────────────────────
    private final Map<Integer, Map<String, String>> characterAnimationPaths = new HashMap<>();
    private boolean actionAnimationPlaying = false;
    private Timer actionAnimationTimer;

    private final ImageIcon[] idleSprites = new ImageIcon[8];
    private ImageIcon currentP1Sprite;
    private ImageIcon currentP2Sprite;

    // ── Game state ────────────────────────────────────────────────────────────
    private final GameWindow gameWindow;
    private final String gameMode;
    private final int p1Index;
    private final int p2Index;

    private int p1MaxHp, p2MaxHp;
    private int p1Hp, p2Hp;
    private int p1SkillCd = 0, p2SkillCd = 0;
    private int p1DefendCd = 0, p2DefendCd = 0;
    private boolean p1Defending = false, p2Defending = false;
    private boolean p1Stunned = false, p2Stunned = false;
    private int p1StunTurns = 0, p2StunTurns = 0;

    private int currentTurn = 1;
    private int roundCount = 1;
    private boolean waitingForRoll = false;
    private boolean gameOver = false;
    private String winner = "";

    // ── BUG FIX: global action lock ──────────────────────────────────────────
    // Set to true the moment any action starts; cleared when the full
    // roll-animation-resolve cycle completes or the turn switches back
    // to a human player. Prevents multi-click spam.
    private boolean actionLocked = false;

    // ── Dice state ────────────────────────────────────────────────────────────
    private int die1Val = 0, die2Val = 0;
    private boolean showDice = false;
    private int lastDamage = 0;

    // ── Battle log ────────────────────────────────────────────────────────────
    private String logLine1 = "";
    private String logLine2 = "";
    private String logLine3 = "";

    // ── Wildcard ──────────────────────────────────────────────────────────────
    private String p1Wildcard = null;
    private String p2Wildcard = null;

    // ── Hover states ──────────────────────────────────────────────────────────
    private boolean hoverAttack  = false;
    private boolean hoverSpecial = false;
    private boolean hoverDefend  = false;
    private boolean hoverWild    = false;

    // ── Button rectangles ─────────────────────────────────────────────────────
    private final Rectangle attackRect  = new Rectangle();
    private final Rectangle specialRect = new Rectangle();
    private final Rectangle defendRect  = new Rectangle();
    private final Rectangle wildRect    = new Rectangle();

    // ── Assets ────────────────────────────────────────────────────────────────
    private BufferedImage bgImage;
    private final BufferedImage[] hpBars = new BufferedImage[6];
    private final BufferedImage[] diceImages = new BufferedImage[6];
    private BufferedImage arrowImg;
    private BufferedImage btnAttackImg;
    private BufferedImage btnSpecialImg;
    private BufferedImage btnDefendImg;
    private BufferedImage btnWildcardImg;
    private BufferedImage p1LabelImg;
    private BufferedImage p2LabelImg;

    private final Random rand = new Random();

    // ── Animation action keys ─────────────────────────────────────────────────
    private static final String ANIM_ATTACK   = "attack";
    private static final String ANIM_DEFEND   = "defend";
    private static final String ANIM_SPECIAL  = "ult";
    private static final String ANIM_WILDCARD = "wildcard";

    public BattleScreen(GameWindow gameWindow, int p1Index, int p2Index, String gameMode) {
        this.gameWindow = gameWindow;
        this.p1Index = p1Index;
        this.p2Index = p2Index;
        this.gameMode = gameMode;

        p1MaxHp = Integer.parseInt(CHARACTERS[p1Index][2]);
        p2MaxHp = Integer.parseInt(CHARACTERS[p2Index][2]);
        p1Hp = p1MaxHp;
        p2Hp = p2MaxHp;

        setLayout(null);
        loadImages();
        addMouseListeners();
        determineFirstTurn();

        MusicManager.get().playBGM(MusicManager.BGM_BATTLE);
    }

    // ── Image loading ─────────────────────────────────────────────────────────
    private void loadImages() {
        bgImage        = loadImage(BG_PATH);
        arrowImg       = loadImage(ARROW_PATH);
        btnAttackImg   = loadImage(BTN_ATTACK_PATH);
        btnSpecialImg  = loadImage(BTN_SPECIAL_PATH);
        btnDefendImg   = loadImage(BTN_DEFEND_PATH);
        btnWildcardImg = loadImage(BTN_WILDCARD_PATH);

        if ("PVC".equals(gameMode)) {
            p1LabelImg = loadImage("assets/ui/player1.png");
            p2LabelImg = loadImage("assets/ui/computer.png");
        } else {
            p1LabelImg = loadImage("assets/ui/player1.png");
            p2LabelImg = loadImage("assets/ui/player2.png");
        }

        String[] hpPaths = {
                HP_0_PATH, HP_20_PATH, HP_40_PATH,
                HP_60_PATH, HP_80_PATH, HP_100_PATH
        };

        for (int i = 0; i < SPRITE_FILES.length; i++) {
            File f = new File(SPRITE_FILES[i]);
            if (f.exists()) {
                idleSprites[i] = new ImageIcon(SPRITE_FILES[i]);
                idleSprites[i].setImageObserver(this);
            }
        }

        currentP1Sprite = idleSprites[p1Index];
        currentP2Sprite = idleSprites[p2Index];

        for (int i = 0; i < hpBars.length; i++) {
            hpBars[i] = loadImage(hpPaths[i]);
        }

        for (int i = 0; i < diceImages.length; i++) {
            diceImages[i] = loadImage("assets/dice/dice_" + (i + 1) + ".png");
        }

        loadCharacterAnimations();
    }

    private void loadCharacterAnimations() {
        String[] types = { ANIM_ATTACK, ANIM_DEFEND, ANIM_SPECIAL, ANIM_WILDCARD };

        for (int i = 0; i < CHARACTERS.length; i++) {
            Map<String, String> animMap = new HashMap<>();
            for (String type : types) {
                animMap.put(type, resolveAnimationPath(i, type));
            }
            characterAnimationPaths.put(i, animMap);
        }
    }

    private String resolveAnimationPath(int charIndex, String type) {
        for (String base : getAnimationBaseNames(charIndex)) {
            String path = "assets/animations/" + base + "_" + type + ".gif";
            if (new File(path).exists()) {
                return path;
            }
        }

        System.out.println("Animation not found (will skip): assets/animations/"
                + getAnimationBaseNames(charIndex)[0] + "_" + type + ".gif");
        return null;
    }

    private String[] getAnimationBaseNames(int charIndex) {
        String displayName = CHARACTERS[charIndex][0].toLowerCase();

        if ("vibe".equals(displayName)) {
            return new String[] { "vibe", "vibee" };
        }

        return new String[] { displayName };
    }

    private ImageIcon freshGif(String path) {
        if (path == null) return null;

        ImageIcon icon = new ImageIcon(path);
        icon.setImageObserver(this);

        Image filtered = Toolkit.getDefaultToolkit().createImage(
                new FilteredImageSource(
                        icon.getImage().getSource(),
                        new RGBImageFilter() {
                            @Override
                            public int filterRGB(int x, int y, int rgb) {
                                int r = (rgb >> 16) & 0xFF;
                                int g = (rgb >> 8)  & 0xFF;
                                int b =  rgb        & 0xFF;
                                if (r < 30 && g < 30 && b < 30) {
                                    return 0x00000000;
                                }
                                return rgb;
                            }
                        }
                )
        );

        ImageIcon result = new ImageIcon(filtered);
        result.setImageObserver(this);
        return result;
    }

    private int getAnimDuration(String type) {
        switch (type) {
            case ANIM_ATTACK:   return ANIM_ATTACK_MS;
            case ANIM_DEFEND:   return ANIM_DEFEND_MS;
            case ANIM_SPECIAL:  return ANIM_ULT_MS;
            case ANIM_WILDCARD: return ANIM_WILDCARD_MS;
            default:            return 1000;
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

    // ── First turn determination ──────────────────────────────────────────────
    private void determineFirstTurn() {
        int roll1 = rand.nextInt(6) + rand.nextInt(6) + 2;
        int roll2 = rand.nextInt(6) + rand.nextInt(6) + 2;
        currentTurn = (roll1 >= roll2) ? 1 : 2;

        addLog(CHARACTERS[p1Index][0] + " rolled " + roll1 +
                ", " + CHARACTERS[p2Index][0] + " rolled " + roll2 + ".");
        addLog((currentTurn == 1 ? CHARACTERS[p1Index][0]
                : CHARACTERS[p2Index][0]) + " goes first!");

        if ("PVC".equals(gameMode) && currentTurn == 2) {
            Timer t = new Timer(1200, e -> {
                doComputerTurn();
                repaint();
            });
            t.setRepeats(false);
            t.start();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  IN-PLACE ACTION ANIMATION
    // ─────────────────────────────────────────────────────────────────────────
    private void playInPlaceAnimation(boolean isP1, String actionType, Runnable onComplete) {
        int charIndex = isP1 ? p1Index : p2Index;
        Map<String, String> animMap = characterAnimationPaths.get(charIndex);
        String path = (animMap != null) ? animMap.get(actionType) : null;

        if (path == null) {
            onComplete.run();
            return;
        }

        if (actionAnimationTimer != null && actionAnimationTimer.isRunning()) {
            actionAnimationTimer.stop();
        }

        ImageIcon actionGif = freshGif(path);
        if (actionGif == null || actionGif.getImage() == null) {
            onComplete.run();
            return;
        }

        actionAnimationPlaying = true;

        if (isP1) currentP1Sprite = actionGif;
        else      currentP2Sprite = actionGif;

        repaint();

        actionAnimationTimer = new Timer(getAnimDuration(actionType), e -> {
            if (isP1) currentP1Sprite = idleSprites[p1Index];
            else      currentP2Sprite = idleSprites[p2Index];

            actionAnimationPlaying = false;
            repaint();
            onComplete.run();
        });
        actionAnimationTimer.setRepeats(false);
        actionAnimationTimer.start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HIT SHAKE ANIMATION
    // ─────────────────────────────────────────────────────────────────────────
    private void startHitAnimation(int player) {
        if (shakeTimer != null && shakeTimer.isRunning()) shakeTimer.stop();

        isDefendFlash = false;
        shakingPlayer = player;
        shakeFrames = SHAKE_TOTAL_FRAMES;
        shakeDir = 1;
        spriteFlashAlpha = 255;

        shakeTimer = new Timer(SHAKE_INTERVAL_MS, e -> {
            if (shakeFrames <= 0) {
                shakeOffsetX = 0;
                shakeOffsetY = 0;
                spriteFlashAlpha = 0;
                shakingPlayer = 0;
                isDefendFlash = false;
                shakeTimer.stop();
                repaint();
                return;
            }

            double progress = (double) shakeFrames / SHAKE_TOTAL_FRAMES;
            double eased = progress * progress;

            shakeOffsetX = (int) (20 * eased * shakeDir);
            if (progress > 0.5) {
                shakeOffsetY = (shakeFrames % 3 == 0) ? -8 : (shakeFrames % 3 == 1 ? 4 : 0);
            } else {
                shakeOffsetY = 0;
            }
            spriteFlashAlpha = (int) (210 * eased);

            shakeDir = -shakeDir;
            shakeFrames--;
            repaint();
        });
        shakeTimer.start();
    }

    private void startDefendAnimation(int player) {
        if (shakeTimer != null && shakeTimer.isRunning()) shakeTimer.stop();

        isDefendFlash = true;
        shakingPlayer = player;
        shakeFrames = SHAKE_TOTAL_FRAMES;
        shakeDir = 1;
        spriteFlashAlpha = 255;

        shakeTimer = new Timer(SHAKE_INTERVAL_MS, e -> {
            if (shakeFrames <= 0) {
                shakeOffsetX = 0;
                shakeOffsetY = 0;
                spriteFlashAlpha = 0;
                shakingPlayer = 0;
                isDefendFlash = false;
                shakeTimer.stop();
                repaint();
                return;
            }

            double progress = (double) shakeFrames / SHAKE_TOTAL_FRAMES;
            double eased = progress * progress;

            shakeOffsetX = (int) (14 * eased * (player == 1 ? -1 : 1));
            shakeOffsetY = 0;
            spriteFlashAlpha = (int) (190 * eased);

            shakeDir = -shakeDir;
            shakeFrames--;
            repaint();
        });
        shakeTimer.start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PAINT
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        if (bgImage != null) g2.drawImage(bgImage, 0, 0, w, h, null);
        else {
            g2.setColor(new Color(100, 100, 100));
            g2.fillRect(0, 0, w, h);
        }

        drawRoundBadge(g2, w);
        drawHealthBar(g2, p1Hp, p1MaxHp, 30, 20, false, CHARACTERS[p1Index][0], p1LabelImg);
        drawHealthBar(g2, p2Hp, p2MaxHp, w - 30 - HP_BAR_W, 20, true, CHARACTERS[p2Index][0], p2LabelImg);

        int panelY = h - PANEL_H;
        int groundY = panelY;

        int p1X = (int)(w * 0.10) + ((shakingPlayer == 1) ? shakeOffsetX : 0);
        int p1Y = groundY - SPRITE_H + ((shakingPlayer == 1) ? shakeOffsetY : 0);
        int p2X = (int)(w * 0.65) + ((shakingPlayer == 2) ? shakeOffsetX : 0);
        int p2Y = groundY - SPRITE_H + ((shakingPlayer == 2) ? shakeOffsetY : 0);

        drawSprite(g2, currentP1Sprite, p1X, p1Y, SPRITE_W, SPRITE_H, false, shakingPlayer == 1);
        drawSprite(g2, currentP2Sprite, p2X, p2Y, SPRITE_W, SPRITE_H, true,  shakingPlayer == 2);

        if (!gameOver && !actionAnimationPlaying) {
            int arrowX = (currentTurn == 1) ? p1X + SPRITE_W / 2 : p2X + SPRITE_W / 2;
            drawTurnArrow(g2, arrowX, groundY - SPRITE_H - 12);
        }

        drawActionPanel(g2, 0, panelY, w, h);

        if (gameOver) drawGameOverOverlay(g2, w, h);

        g2.dispose();
    }

    private void drawSprite(Graphics2D g2, ImageIcon icon,
                            int x, int y, int w, int h,
                            boolean flipX, boolean isHit) {
        if (icon != null) {
            Image img = icon.getImage();

            int imgW = w;
            int imgH = h;
            int drawX = x + (w - imgW) / 2;
            int drawY = y + (h - imgH);

            if (flipX) {
                g2.drawImage(img, drawX + imgW, drawY, -imgW, imgH, this);
            } else {
                g2.drawImage(img, drawX, drawY, imgW, imgH, this);
            }

            if (isHit && spriteFlashAlpha > 0) {
                BufferedImage flash = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D fg = flash.createGraphics();
                if (flipX) fg.drawImage(img, imgW, 0, -imgW, imgH, null);
                else       fg.drawImage(img, 0, 0, imgW, imgH, null);
                fg.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_ATOP, spriteFlashAlpha / 255f));
                fg.setColor(isDefendFlash ? new Color(80, 160, 255) : Color.WHITE);
                fg.fillRect(0, 0, imgW, imgH);
                fg.dispose();
                g2.drawImage(flash, drawX, drawY, null);
            }

        } else {
            Color c = flipX ? new Color(220, 80, 80) : new Color(80, 140, 255);
            g2.setColor(c);
            g2.fillRoundRect(x + 40, y, 80, 120, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("?", x + 74, y + 65);
        }
    }

    private void drawRoundBadge(Graphics2D g2, int w) {
        String roundText = "ROUND " + roundCount;
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        int bw = fm.stringWidth(roundText) + 30;
        int bh = 28;
        int bx = (w - bw) / 2;
        int by = 10;

        g2.setColor(new Color(240, 235, 210));
        g2.fillRoundRect(bx, by, bw, bh, 12, 12);
        g2.setColor(new Color(80, 60, 30));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(bx, by, bw, bh, 12, 12);
        g2.setColor(new Color(60, 40, 20));
        g2.drawString(roundText, bx + 15, by + bh - 8);
    }

    private void drawHealthBar(Graphics2D g2, int hp, int maxHp,
                               int x, int y, boolean flip,
                               String charName, BufferedImage labelImg) {
        double pct = (double) hp / maxHp;
        BufferedImage bar = getHpBarImage(pct);

        int labelW = 60;
        int labelH = 25;

        if (bar != null) {
            if (flip) g2.drawImage(bar, x + HP_BAR_W, y, -HP_BAR_W, HP_BAR_H, null);
            else      g2.drawImage(bar, x, y, HP_BAR_W, HP_BAR_H, null);
        } else {
            g2.setColor(new Color(60, 60, 60, 180));
            g2.fillRoundRect(x, y, HP_BAR_W, HP_BAR_H, 10, 10);

            Color barColor = pct > 0.6 ? new Color(80, 200, 80)
                    : pct > 0.3 ? new Color(220, 180, 30)
                      : new Color(200, 60, 60);

            g2.setColor(barColor);
            g2.fillRoundRect(x + 2, y + 2,
                    (int) ((HP_BAR_W - 4) * pct), HP_BAR_H - 4, 8, 8);
        }

        g2.setFont(new Font("Arial", Font.BOLD, 13));
        g2.setColor(Color.WHITE);
        String hpText = hp + " / " + maxHp;
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hpText,
                x + (HP_BAR_W - fm.stringWidth(hpText)) / 2,
                y + HP_BAR_H + 16);

        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(255, 220, 100));
        FontMetrics fm2 = g2.getFontMetrics();
        String name = charName.toUpperCase();
        g2.drawString(name,
                x + (HP_BAR_W - fm2.stringWidth(name)) / 2,
                y + HP_BAR_H + 34);

        if (labelImg != null) {
            g2.drawImage(labelImg,
                    x + (HP_BAR_W - labelW) / 2,
                    y + HP_BAR_H + 40,
                    labelW, labelH, null);
        } else {
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.setColor(flip ? new Color(220, 80, 80) : new Color(80, 140, 255));
            String fallback = flip ? "P2" : "P1";
            FontMetrics fm3 = g2.getFontMetrics();
            g2.drawString(fallback,
                    x + (HP_BAR_W - fm3.stringWidth(fallback)) / 2,
                    y + HP_BAR_H + 55);
        }
    }

    private BufferedImage getHpBarImage(double pct) {
        if (pct <= 0.0)  return hpBars[0];
        if (pct <= 0.20) return hpBars[1];
        if (pct <= 0.40) return hpBars[2];
        if (pct <= 0.60) return hpBars[3];
        if (pct <= 0.80) return hpBars[4];
        return hpBars[5];
    }

    private void drawTurnArrow(Graphics2D g2, int cx, int y) {
        int[] px = { cx - 10, cx + 10, cx };
        int[] py = { y, y, y + 14 };
        g2.setColor(Color.BLACK);
        g2.fillPolygon(px, py, 3);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawPolygon(px, py, 3);
    }

    private void drawDice(Graphics2D g2, int x, int y) {
        int dieSize = 55;
        drawSingleDie(g2, x, y, die1Val, dieSize);
        drawSingleDie(g2, x + dieSize + 8, y, die2Val, dieSize);

        int total = die1Val + die2Val;
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(60, 40, 10));
        g2.drawString("= " + total, x + dieSize * 2 + 16, y + 22);
        g2.setColor(new Color(160, 60, 0));
        g2.drawString("DMG: " + lastDamage, x + dieSize * 2 + 16, y + 42);
    }

    private void drawSingleDie(Graphics2D g2, int x, int y, int val, int s) {
        if (val < 1 || val > 6) return;
        BufferedImage dieImg = diceImages[val - 1];

        if (dieImg != null) {
            g2.drawImage(dieImg, x, y, s, s, null);
        } else {
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, s, s, 8, 8);
            g2.setColor(new Color(80, 80, 80));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x, y, s, s, 8, 8);
            g2.setColor(new Color(30, 30, 30));
            int[][] dots = getDotPositions(val, x, y, s);
            int dotSize = Math.max(4, s / 8);
            for (int[] dot : dots) g2.fillOval(dot[0], dot[1], dotSize, dotSize);
        }
    }

    private int[][] getDotPositions(int val, int x, int y, int s) {
        int m = s / 2;
        int q = s / 4;

        switch (val) {
            case 1: return new int[][]{ {x + m - 4, y + m - 4} };
            case 2: return new int[][]{ {x + q - 4, y + q - 4}, {x + 3 * q - 4, y + 3 * q - 4} };
            case 3: return new int[][]{ {x + q - 4, y + q - 4}, {x + m - 4, y + m - 4}, {x + 3 * q - 4, y + 3 * q - 4} };
            case 4: return new int[][]{ {x + q - 4, y + q - 4}, {x + 3 * q - 4, y + q - 4},
                    {x + q - 4, y + 3 * q - 4}, {x + 3 * q - 4, y + 3 * q - 4} };
            case 5: return new int[][]{ {x + q - 4, y + q - 4}, {x + 3 * q - 4, y + q - 4}, {x + m - 4, y + m - 4},
                    {x + q - 4, y + 3 * q - 4}, {x + 3 * q - 4, y + 3 * q - 4} };
            case 6: return new int[][]{ {x + q - 4, y + q - 4}, {x + 3 * q - 4, y + q - 4},
                    {x + q - 4, y + m - 4}, {x + 3 * q - 4, y + m - 4},
                    {x + q - 4, y + 3 * q - 4}, {x + 3 * q - 4, y + 3 * q - 4} };
            default: return new int[][]{};
        }
    }

    private void drawActionPanel(Graphics2D g2, int px, int py, int w, int h) {
        boolean isPlayerTurn = (currentTurn == 1) || (currentTurn == 2 && "PVP".equals(gameMode));

        int btnW = 180;
        int btnH = 55;
        int centerX = w / 2 - btnW - 10;
        int row1 = py + 20;
        int row2 = py + 85;

        attackRect.setBounds(centerX, row1, btnW, btnH);
        specialRect.setBounds(centerX + btnW + 20, row1, btnW, btnH);
        defendRect.setBounds(centerX, row2, btnW, btnH);
        wildRect.setBounds(centerX + btnW + 20, row2, btnW, btnH);

        boolean canSpecial = (currentTurn == 1) ? p1SkillCd == 0 : p2SkillCd == 0;
        boolean canDefend  = (currentTurn == 1) ? p1DefendCd == 0 : p2DefendCd == 0;
        boolean hasWild    = (currentTurn == 1) ? p1Wildcard != null : p2Wildcard != null;

        // BUG FIX: actionLocked is included so buttons are greyed out during
        // the entire attack-roll-resolve pipeline and during the computer turn.
        boolean buttonsActive = !gameOver && isPlayerTurn
                && !waitingForRoll && !actionAnimationPlaying
                && !showDice && !actionLocked;

        if (buttonsActive) {
            drawActionBtn(g2, attackRect,  hoverAttack,  "▶  ATTACK", true);
            drawActionBtn(g2, specialRect, hoverSpecial, "SPECIAL",   canSpecial);
            drawActionBtn(g2, defendRect,  hoverDefend,  "DEFEND",    canDefend);
            drawActionBtn(g2, wildRect,    hoverWild,    "WILDCARD",  hasWild);
        } else {
            drawActionBtn(g2, attackRect,  false, "▶  ATTACK", false);
            drawActionBtn(g2, specialRect, false, "SPECIAL",   false);
            drawActionBtn(g2, defendRect,  false, "DEFEND",    false);
            drawActionBtn(g2, wildRect,    false, "WILDCARD",  false);
        }

        if (!gameOver) {
            int rightX = w * 2 / 3 + 20;
            int rightW = w / 3 - 40;
            int rightCenterX = rightX + rightW / 2;

            if (!logLine2.isEmpty()) {
                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                g2.setColor(new Color(100, 60, 20));
                FontMetrics fmLog2 = g2.getFontMetrics();
                g2.drawString(logLine2,
                        rightCenterX - fmLog2.stringWidth(logLine2) / 2,
                        py + 22);
            }

            if (!logLine3.isEmpty()) {
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.setColor(new Color(80, 40, 0));
                FontMetrics fmLog3 = g2.getFontMetrics();
                g2.drawString(logLine3,
                        rightCenterX - fmLog3.stringWidth(logLine3) / 2,
                        py + 40);
            }

            g2.setColor(new Color(160, 120, 70, 120));
            g2.setStroke(new BasicStroke(1));
            g2.drawLine(rightX, py + 50, rightX + rightW, py + 50);

            String subLabel = actionAnimationPlaying ? "ANIMATING..."
                    : waitingForRoll ? "CLICK TO ROLL!"
                      : showDice ? "RESOLVING..."
                        : actionLocked ? "RESOLVING..."
                          : !isPlayerTurn ? "IS THINKING..."
                            : "";

            if (!subLabel.isEmpty()) {
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                g2.setColor(new Color(120, 70, 10));
                FontMetrics fmSub = g2.getFontMetrics();
                g2.drawString(subLabel,
                        rightCenterX - fmSub.stringWidth(subLabel) / 2,
                        py + 68);
            }

            int cdSpecial = (currentTurn == 1) ? p1SkillCd : p2SkillCd;
            int cdDefend  = (currentTurn == 1) ? p1DefendCd : p2DefendCd;

            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.setColor(new Color(100, 60, 20));
            String cdSText = "Special CD: " + (cdSpecial > 0 ? cdSpecial : "Ready");
            String cdDText = "Defend CD:  " + (cdDefend > 0 ? cdDefend : "Ready");
            FontMetrics fm3 = g2.getFontMetrics();
            g2.drawString(cdSText,
                    rightCenterX - fm3.stringWidth(cdSText) / 2,
                    py + 85);
            g2.drawString(cdDText,
                    rightCenterX - fm3.stringWidth(cdDText) / 2,
                    py + 102);

            String wc = (currentTurn == 1) ? p1Wildcard : p2Wildcard;
            if (wc != null) {
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.setColor(new Color(140, 0, 180));
                String wcText = "WILDCARD: " + wc;
                FontMetrics fm4 = g2.getFontMetrics();
                g2.drawString(wcText,
                        rightCenterX - fm4.stringWidth(wcText) / 2,
                        py + 122);
            }

            if (showDice) drawDice(g2, rightX, py + 130);
        }
    }

    private void drawActionBtn(Graphics2D g2, Rectangle r,
                               boolean hover, String label, boolean enabled) {
        BufferedImage btnImg = null;
        switch (label) {
            case "▶  ATTACK": btnImg = btnAttackImg;   break;
            case "SPECIAL":   btnImg = btnSpecialImg;  break;
            case "DEFEND":    btnImg = btnDefendImg;   break;
            case "WILDCARD":  btnImg = btnWildcardImg; break;
        }

        if (btnImg != null) {
            float alpha = enabled ? 1.0f : 0.4f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.drawImage(btnImg, r.x, r.y, r.width, r.height, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            Color bg = !enabled ? new Color(60, 60, 60, 160)
                    : hover ? new Color(100, 180, 80)
                      : new Color(30, 100, 50, 200);
            g2.setColor(bg);
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label,
                    r.x + (r.width - fm.stringWidth(label)) / 2,
                    r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2);
        }

        if (hover && enabled) {
            int arrowW = 25;
            int arrowH = 25;
            int arrowX = r.x - 2;
            int arrowY = r.y + (r.height - arrowH) / 2;
            if (arrowImg != null) {
                g2.drawImage(arrowImg, arrowX, arrowY, arrowW, arrowH, null);
            } else {
                g2.setColor(new Color(255, 220, 50));
                int cx = arrowX + arrowW / 2, cy = arrowY + arrowH / 2;
                int[] px = { cx - 8, cx + 8, cx - 8 };
                int[] py = { cy - 8, cy, cy + 8 };
                g2.fillPolygon(px, py, 3);
            }
        }
    }

    private void drawGameOverOverlay(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, w, h);
        g2.setFont(new Font("Arial", Font.BOLD, 52));
        g2.setColor(new Color(255, 220, 50));
        FontMetrics fm = g2.getFontMetrics();
        String title = winner + " WINS!";
        g2.drawString(title, (w - fm.stringWidth(title)) / 2, h / 2 - 20);
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.setColor(new Color(200, 200, 200));
        String sub = "Returning to menu...";
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(sub, (w - fm2.stringWidth(sub)) / 2, h / 2 + 30);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GAME LOGIC
    // ─────────────────────────────────────────────────────────────────────────
    private void addLog(String msg) {
        logLine1 = logLine2;
        logLine2 = logLine3;
        logLine3 = msg;
    }

    private void onActionChosen(String action) {
        // BUG FIX: hard lock — reject all input while an action is mid-flight
        if (actionLocked) return;
        if (actionAnimationPlaying || showDice) return;

        boolean isP1 = (currentTurn == 1);
        int attackerIdx = isP1 ? p1Index : p2Index;
        String attackerName = CHARACTERS[attackerIdx][0];
        String defenderName = CHARACTERS[isP1 ? p2Index : p1Index][0];

        switch (action) {
            case "ATTACK":
                // BUG FIX: lock immediately on first click
                actionLocked = true;
                playInPlaceAnimation(isP1, ANIM_ATTACK,
                        () -> doAttackLogic(attackerName));
                break;

            case "SPECIAL":
                if ((isP1 && p1SkillCd > 0) || (!isP1 && p2SkillCd > 0)) {
                    addLog("Special is on cooldown!");
                    repaint();
                    return;
                }
                actionLocked = true;
                playInPlaceAnimation(isP1, ANIM_SPECIAL,
                        () -> doSpecialLogic(isP1, attackerName, defenderName));
                break;

            case "DEFEND":
                if ((isP1 && p1DefendCd > 0) || (!isP1 && p2DefendCd > 0)) {
                    addLog("Defend is on cooldown!");
                    repaint();
                    return;
                }
                actionLocked = true;
                playInPlaceAnimation(isP1, ANIM_DEFEND,
                        () -> doDefendLogic(isP1, attackerName));
                break;

            case "WILDCARD":
                String wc = isP1 ? p1Wildcard : p2Wildcard;
                if (wc == null) {
                    addLog("No wildcard available!");
                    repaint();
                    return;
                }
                actionLocked = true;
                playInPlaceAnimation(isP1, ANIM_WILDCARD,
                        () -> doWildcardLogic(isP1, wc, attackerName, defenderName));
                break;
        }
    }

    private void doAttackLogic(String attackerName) {
        addLog(attackerName + " winds up for an attack!");
        waitingForRoll = true;
        repaint();
    }

    private void doDefendLogic(boolean isP1, String attackerName) {
        if (isP1) {
            p1Defending = true;
            p1DefendCd = 3;
        } else {
            p2Defending = true;
            p2DefendCd = 3;
        }
        addLog(attackerName + " takes a defensive stance!");
        startDefendAnimation(isP1 ? 1 : 2);
        endTurn();
    }

    private void doSpecialLogic(boolean isP1, String attacker, String defender) {
        doSpecialSkill(isP1, attacker, defender);
        // Zyah grants an extra turn via waitingForRoll — do not call endTurn in that case
        if (!waitingForRoll) endTurn();
    }

    private void doWildcardLogic(boolean isP1, String wc, String attacker, String defender) {
        doWildcard(isP1, wc, attacker, defender);
        if (isP1) p1Wildcard = null;
        else      p2Wildcard = null;
        // DOUBLE ROLL wildcard sets waitingForRoll — do not advance the turn
        if (!waitingForRoll) endTurn();
    }

    private void doRollAndAttack() {
        // BUG FIX: drop any extra clicks that arrived before we cleared waitingForRoll
        if (!waitingForRoll) return;
        waitingForRoll = false;
        // actionLocked remains true throughout the roll-resolve pipeline

        boolean isP1 = (currentTurn == 1);
        int attackerIdx = isP1 ? p1Index : p2Index;
        int defenderIdx = isP1 ? p2Index : p1Index;
        String attackerName = CHARACTERS[attackerIdx][0];
        String defenderName = CHARACTERS[defenderIdx][0];

        die1Val = rand.nextInt(6) + 1;
        die2Val = rand.nextInt(6) + 1;

        int total = die1Val + die2Val;
        // KhaiMode: read multiplier from GameSettings singleton
        double mult = GameSettings.get().getMultiplier(attackerIdx);
        int damage = (int) (total * mult);

        boolean defending = isP1 ? p2Defending : p1Defending;
        if (defending) {
            damage = Math.max(1, damage / 2);
            if (isP1) p2Defending = false;
            else      p1Defending = false;
            addLog(defenderName + " blocked! Damage halved.");
        }

        final int finalTotal = total;
        final double finalMult = mult;
        final int finalDamage = damage;
        final String finalAttackerName = attackerName;
        final String finalDefenderName = defenderName;
        final boolean finalIsP1 = isP1;

        lastDamage = finalDamage;

        if (finalIsP1) {
            p2Hp = Math.max(0, p2Hp - finalDamage);
            startHitAnimation(2);
        } else {
            p1Hp = Math.max(0, p1Hp - finalDamage);
            startHitAnimation(1);
        }

        int animDuration = SHAKE_TOTAL_FRAMES * SHAKE_INTERVAL_MS;
        Timer showTimer = new Timer(animDuration, e -> {
            showDice = true;

            addLog(finalAttackerName + " rolled " + die1Val + "+" + die2Val +
                    " = " + finalTotal + " x " + finalMult + "x");
            addLog(finalAttackerName + " deals " + finalDamage + " damage to " + finalDefenderName + "!");

            if (roundCount % 3 == 0 && rand.nextInt(100) < 30) {
                String[] wildcards = {"FREEZE", "DOUBLE ROLL", "HEAL", "SHIELD"};
                String wc = wildcards[rand.nextInt(wildcards.length)];
                if (finalIsP1) p1Wildcard = wc;
                else           p2Wildcard = wc;
                addLog(finalAttackerName + " drew wildcard: " + wc + "!");
            }

            repaint();

            Timer hideTimer = new Timer(2000, e2 -> {
                showDice = false;
                checkGameOver();
                if (!gameOver) {
                    // BUG FIX: only unlock when the full pipeline is resolved
                    actionLocked = false;
                    endTurn();
                }
                repaint();
            });
            hideTimer.setRepeats(false);
            hideTimer.start();
        });
        showTimer.setRepeats(false);
        showTimer.start();
    }

    private void doSpecialSkill(boolean isP1, String attacker, String defender) {
        int idx = isP1 ? p1Index : p2Index;
        String charName = CHARACTERS[idx][0];

        if (isP1) p1SkillCd = 5;
        else      p2SkillCd = 5;

        switch (charName) {
            case "Echo":
                addLog(attacker + " uses Phantom Dance! Dodges next 2 attacks!");
                break;

            case "Zyah":
                // Extra turn: unlock and let the player click to roll
                addLog(attacker + " uses Dancehall Fever! Extra turn granted!");
                actionLocked = false;
                waitingForRoll = true;
                repaint();
                return; // doSpecialLogic checks waitingForRoll to skip endTurn

            case "Raze":
                die1Val = rand.nextInt(6) + 1;
                die2Val = rand.nextInt(6) + 1;
                int total = die1Val + die2Val;
                // KhaiMode: use GameSettings multiplier
                double mult = GameSettings.get().getMultiplier(idx);
                int dmg = (int) (total * mult) + 8;
                lastDamage = dmg;
                showDice = true;
                if (isP1) {
                    p2Hp = Math.max(0, p2Hp - dmg);
                    startHitAnimation(2);
                } else {
                    p1Hp = Math.max(0, p1Hp - dmg);
                    startHitAnimation(1);
                }
                addLog(attacker + " uses Blazing Combo! +" + dmg + " dmg!");
                break;

            case "Vibe":
                addLog(attacker + " uses House Foundation! 50% dmg reduction x2!");
                if (isP1) p1Defending = true;
                else      p2Defending = true;
                break;

            case "Torque":
                addLog(attacker + " uses Earthquake Stomp! " + defender + " stunned!");
                startHitAnimation(isP1 ? 2 : 1);
                if (isP1) {
                    p2Stunned = true;
                    p2StunTurns = 2;
                } else {
                    p1Stunned = true;
                    p1StunTurns = 2;
                }
                break;

            case "Luma":
                int healL = 30;
                if (isP1) p1Hp = Math.min(p1MaxHp, p1Hp + healL);
                else      p2Hp = Math.min(p2MaxHp, p2Hp + healL);
                addLog(attacker + " uses Radiant Burst! Healed " + healL + " HP!");
                break;

            case "Lyric":
                int healLy = 35;
                if (isP1) p1Hp = Math.min(p1MaxHp, p1Hp + healLy);
                else      p2Hp = Math.min(p2MaxHp, p2Hp + healLy);
                addLog(attacker + " uses Healing Freestyle! Healed " + healLy + " HP!");
                break;

            case "Ayo":
                addLog(attacker + " uses Ancestral Call! Will revive at 50% HP!");
                break;
        }
    }

    private void doWildcard(boolean isP1, String wc, String attacker, String defender) {
        switch (wc) {
            case "FREEZE":
                if (isP1) {
                    p2Stunned = true;
                    p2StunTurns = 1;
                } else {
                    p1Stunned = true;
                    p1StunTurns = 1;
                }
                startHitAnimation(isP1 ? 2 : 1);
                addLog(attacker + " used FREEZE! " + defender + " loses next turn!");
                break;

            case "DOUBLE ROLL":
                // Extra roll: unlock and let the player click to roll
                addLog(attacker + " used DOUBLE ROLL! Roll again!");
                actionLocked = false;
                waitingForRoll = true;
                break;

            case "HEAL":
                int heal = 20;
                if (isP1) p1Hp = Math.min(p1MaxHp, p1Hp + heal);
                else      p2Hp = Math.min(p2MaxHp, p2Hp + heal);
                addLog(attacker + " used HEAL! Restored " + heal + " HP!");
                break;

            case "SHIELD":
                if (isP1) p1Defending = true;
                else      p2Defending = true;
                addLog(attacker + " used SHIELD! Next hit reduced by 50%!");
                break;
        }
    }

    private void endTurn() {
        if (p1SkillCd > 0)  p1SkillCd--;
        if (p2SkillCd > 0)  p2SkillCd--;
        if (p1DefendCd > 0) p1DefendCd--;
        if (p2DefendCd > 0) p2DefendCd--;

        if (p1Stunned) {
            p1StunTurns--;
            if (p1StunTurns <= 0) {
                p1Stunned = false;
                addLog(CHARACTERS[p1Index][0] + " recovered from stun!");
            }
        }

        if (p2Stunned) {
            p2StunTurns--;
            if (p2StunTurns <= 0) {
                p2Stunned = false;
                addLog(CHARACTERS[p2Index][0] + " recovered from stun!");
            }
        }

        currentTurn = (currentTurn == 1) ? 2 : 1;
        if (currentTurn == 1) roundCount++;

        // BUG FIX: unlock when it's a human player's turn
        // PVP: both turns are human. PVC: only turn 1 is human.
        if (currentTurn == 1 || (currentTurn == 2 && "PVP".equals(gameMode))) {
            actionLocked = false;
        }

        if (currentTurn == 1 && p1Stunned) {
            addLog(CHARACTERS[p1Index][0] + " is stunned and skips their turn!");
            currentTurn = 2;
            actionLocked = false; // still unlock so rendering is correct
        } else if (currentTurn == 2 && p2Stunned) {
            addLog(CHARACTERS[p2Index][0] + " is stunned and skips their turn!");
            currentTurn = 1;
            actionLocked = false;
        }

        repaint();

        if (!gameOver && "PVC".equals(gameMode) && currentTurn == 2) {
            Timer t = new Timer(1200, e -> {
                doComputerTurn();
                repaint();
            });
            t.setRepeats(false);
            t.start();
        }
    }

    private void doComputerTurn() {
        if (gameOver) return;

        String compName = CHARACTERS[p2Index][0];
        String playerName = CHARACTERS[p1Index][0];

        int decision = rand.nextInt(100);

        if (p2SkillCd == 0 && decision < 30) {
            playInPlaceAnimation(false, ANIM_SPECIAL, () -> {
                addLog(compName + " uses their special skill!");
                doSpecialSkill(false, compName, playerName);
                checkGameOver();
                if (!gameOver) endTurn();
            });
        } else if (p2DefendCd == 0 && p2Hp < p2MaxHp * 0.4 && decision < 50) {
            playInPlaceAnimation(false, ANIM_DEFEND, () -> {
                p2Defending = true;
                p2DefendCd = 3;
                addLog(compName + " takes a defensive stance!");
                startDefendAnimation(2);
                endTurn();
            });
        } else {
            playInPlaceAnimation(false, ANIM_ATTACK, () -> {
                addLog(compName + " attacks " + playerName + "!");
                doComputerRoll();
            });
        }
    }

    private void doComputerRoll() {
        die1Val = rand.nextInt(6) + 1;
        die2Val = rand.nextInt(6) + 1;

        int total = die1Val + die2Val;
        // KhaiMode does NOT apply to the computer — use default multiplier from array
        double mult = Double.parseDouble(CHARACTERS[p2Index][3]);
        int damage = (int) (total * mult);

        if (p1Defending) {
            damage = Math.max(1, damage / 2);
            p1Defending = false;
            addLog(CHARACTERS[p1Index][0] + " blocked! Damage halved.");
        }

        final int finalTotal = total;
        final double finalMult = mult;
        final int finalDamage = damage;
        final String finalCpuName = CHARACTERS[p2Index][0];
        final String finalPlayerName = CHARACTERS[p1Index][0];

        lastDamage = finalDamage;

        p1Hp = Math.max(0, p1Hp - finalDamage);
        startHitAnimation(1);

        int animDuration = SHAKE_TOTAL_FRAMES * SHAKE_INTERVAL_MS;
        Timer showTimer = new Timer(animDuration, e -> {
            showDice = true;

            addLog(finalCpuName + " rolled " + die1Val + "+" + die2Val +
                    " = " + finalTotal + " x " + finalMult + "x");
            addLog("Deals " + finalDamage + " damage to " + finalPlayerName + "!");
            repaint();

            Timer hideTimer = new Timer(2000, e2 -> {
                showDice = false;
                checkGameOver();
                if (!gameOver) endTurn();
                repaint();
            });
            hideTimer.setRepeats(false);
            hideTimer.start();
        });
        showTimer.setRepeats(false);
        showTimer.start();
    }

    private void checkGameOver() {
        if (p1Hp <= 0 || p2Hp <= 0) {
            gameOver = true;
            // BUG FIX: permanently lock buttons after game ends
            actionLocked = true;
            winner = (p1Hp > 0) ? CHARACTERS[p1Index][0] : CHARACTERS[p2Index][0];
            addLog(winner + " wins the battle!");
            repaint();

            Timer t = new Timer(4000, e ->
                    gameWindow.switchScreen(new HomeScreen(gameWindow)));
            t.setRepeats(false);
            t.start();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MOUSE LISTENERS
    // ─────────────────────────────────────────────────────────────────────────
    private void addMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverAttack  = attackRect.contains(e.getPoint());
                hoverSpecial = specialRect.contains(e.getPoint());
                hoverDefend  = defendRect.contains(e.getPoint());
                hoverWild    = wildRect.contains(e.getPoint());

                setCursor((hoverAttack || hoverSpecial || hoverDefend || hoverWild)
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameOver) return;
                // BUG FIX: primary gate — reject all clicks while locked
                // unless we are in the "click to roll" phase
                if (actionLocked && !waitingForRoll) return;
                if (actionAnimationPlaying) return;
                if (showDice) return;

                // "CLICK TO ROLL!" phase — accept exactly one click
                if (waitingForRoll) {
                    doRollAndAttack();
                    return;
                }

                boolean isPlayerTurn = (currentTurn == 1) ||
                        (currentTurn == 2 && "PVP".equals(gameMode));
                if (!isPlayerTurn) return;

                if      (attackRect.contains(e.getPoint()))  onActionChosen("ATTACK");
                else if (specialRect.contains(e.getPoint())) onActionChosen("SPECIAL");
                else if (defendRect.contains(e.getPoint()))  onActionChosen("DEFEND");
                else if (wildRect.contains(e.getPoint()))    onActionChosen("WILDCARD");
            }
        });
    }
}
