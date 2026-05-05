import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class ArcadeBattleScreen extends JPanel {

    // ── Asset paths ───────────────────────────────────────────────────────────
    private static final String BG_PATH           = "assets/backgrounds/background_arcade.png";
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

    public static final String[][] CHARACTERS = {
            { "Echo",   "Assassin", "80",  "4.0", "Phantom Dance"    },
            { "Zyah",   "Assassin", "80",  "4.0", "Dancehall Fever"  },
            { "Raze",   "Fighter",  "115", "3.0", "Blazing Combo"    },
            { "Vibe",   "Fighter",  "110", "3.0", "House Foundation" },
            { "Torque", "Tank",     "150", "2.0", "Earthquake Stomp" },
            { "Luma",   "Tank",     "140", "2.0", "Radiant Burst"    },
            { "Lyric",  "Support",  "105", "2.0", "Healing Freestyle"},
            { "Ayo",    "Support",  "100", "2.0", "Ancestral Call"   },
    };

    private static final String[] PLAYER_SPRITE_FILES = {
            "assets/characters/portraits/echo.gif",
            "assets/characters/portraits/zyah.gif",
            "assets/characters/portraits/raze.gif",
            "assets/characters/portraits/vibee.gif",
            "assets/characters/portraits/torque.gif",
            "assets/characters/portraits/luma.gif",
            "assets/characters/portraits/lyric.gif",
            "assets/characters/portraits/ayo.gif"
    };

    // ── Sizes ─────────────────────────────────────────────────────────────────
    private static final int SPRITE_W = 160;
    private static final int SPRITE_H = 200;
    private static final int HP_BAR_W = 220;
    private static final int HP_BAR_H = 41;
    private static final int PANEL_H  = 230;

    // ── Shake animation ───────────────────────────────────────────────────────
    private int     shakingPlayer    = 0;
    private int     shakeFrames      = 0;
    private int     shakeOffsetX     = 0;
    private int     shakeOffsetY     = 0;
    private int     spriteFlashAlpha = 0;
    private int     shakeDir         = 1;
    private boolean isDefendFlash    = false;
    private static final int SHAKE_TOTAL_FRAMES = 40;
    private static final int SHAKE_INTERVAL_MS  = 50;
    private Timer shakeTimer;

    // ── References ────────────────────────────────────────────────────────────
    private final GameWindow    gameWindow;
    private final ArcadeManager arcadeManager;
    private final int           playerIndex;
    private final String[]      bossData;
    private final boolean       isMiniFinal;
    private final boolean       isFinalBoss;

    // ── Stats ─────────────────────────────────────────────────────────────────
    private int playerHp, playerMaxHp;
    private int bossHp,   bossMaxHp;
    private int playerSkillCd  = 0, bossSkillCd  = 0;
    private int playerDefendCd = 0, bossDefendCd = 0;
    private boolean playerDefending = false, bossDefending = false;
    private boolean playerStunned   = false, bossStunned   = false;
    private int playerStunTurns = 0, bossStunTurns = 0;

    // ── Turn / round ──────────────────────────────────────────────────────────
    private int     currentTurn    = 1; // 1 = player, 2 = boss
    private int     roundCount     = 1;
    private boolean waitingForRoll = false;
    private boolean gameOver       = false;
    private boolean playerWon      = false;

    // ── BUG FIX: global action lock ───────────────────────────────────────────
    // Set to true the moment any action starts processing; cleared only when
    // the full roll-animation-resolve cycle completes. This prevents multiple
    // clicks from triggering additional attacks while one is mid-flight.
    private boolean actionLocked   = false;

    // ── Dice ──────────────────────────────────────────────────────────────────
    private int     die1Val = 0, die2Val = 0;
    private boolean showDice   = false;
    private int     lastDamage = 0;

    // ── Log ───────────────────────────────────────────────────────────────────
    private String logLine1 = "", logLine2 = "", logLine3 = "";

    // ── Wildcard ──────────────────────────────────────────────────────────────
    private String playerWildcard = null;

    // ── Hover ─────────────────────────────────────────────────────────────────
    private boolean hoverAttack  = false;
    private boolean hoverSpecial = false;
    private boolean hoverDefend  = false;
    private boolean hoverWild    = false;

    // ── Rects ─────────────────────────────────────────────────────────────────
    private Rectangle attackRect  = new Rectangle();
    private Rectangle specialRect = new Rectangle();
    private Rectangle defendRect  = new Rectangle();
    private Rectangle wildRect    = new Rectangle();

    // ── Assets ────────────────────────────────────────────────────────────────
    private BufferedImage   bgImage;
    private BufferedImage[] hpBars      = new BufferedImage[6];
    private BufferedImage[] diceImages  = new BufferedImage[6];
    private BufferedImage   arrowImg;
    private BufferedImage   btnAttackImg, btnSpecialImg, btnDefendImg, btnWildcardImg;
    private BufferedImage   p1LabelImg;
    private ImageIcon       playerSprite;
    private ImageIcon       bossSprite;

    private final Random rand = new Random();

    // ─────────────────────────────────────────────────────────────────────────
    public ArcadeBattleScreen(GameWindow gameWindow, ArcadeManager arcadeManager,
                              int playerIndex, String[] bossData,
                              boolean isMiniFinal, boolean isFinalBoss) {
        this.gameWindow    = gameWindow;
        this.arcadeManager = arcadeManager;
        this.playerIndex   = playerIndex;
        this.bossData      = bossData;
        this.isMiniFinal   = isMiniFinal;
        this.isFinalBoss   = isFinalBoss;

        playerMaxHp = arcadeManager.getPlayerMaxHp();
        playerHp    = arcadeManager.getPlayerHp();

        bossMaxHp = Integer.parseInt(bossData[2]);
        bossHp    = bossMaxHp;

        setLayout(null);
        loadImages();
        addMouseListeners();
        determineFirstTurn();
    }

    // ── Load images ───────────────────────────────────────────────────────────
    private void loadImages() {
        bgImage        = loadImg(BG_PATH);
        arrowImg       = loadImg(ARROW_PATH);
        btnAttackImg   = loadImg(BTN_ATTACK_PATH);
        btnSpecialImg  = loadImg(BTN_SPECIAL_PATH);
        btnDefendImg   = loadImg(BTN_DEFEND_PATH);
        btnWildcardImg = loadImg(BTN_WILDCARD_PATH);
        p1LabelImg     = loadImg("assets/ui/player_1.png");

        String[] hpPaths = {HP_0_PATH,HP_20_PATH,HP_40_PATH,HP_60_PATH,HP_80_PATH,HP_100_PATH};
        for (int i = 0; i < 6; i++) hpBars[i]    = loadImg(hpPaths[i]);
        for (int i = 0; i < 6; i++) diceImages[i] = loadImg("assets/dice/dice_"+(i+1)+".png");

        File pf = new File(PLAYER_SPRITE_FILES[playerIndex]);
        if (pf.exists()) {
            playerSprite = new ImageIcon(PLAYER_SPRITE_FILES[playerIndex]);
            playerSprite.setImageObserver(this);
        }

        String bossGif = bossData[5];
        File bf = new File(bossGif);
        if (bf.exists()) {
            bossSprite = new ImageIcon(bossGif);
            bossSprite.setImageObserver(this);
        }
    }

    private BufferedImage loadImg(String path) {
        try { return ImageIO.read(new File(path)); }
        catch (IOException e) { System.err.println("Missing: " + path); return null; }
    }

    // ── First turn ────────────────────────────────────────────────────────────
    private void determineFirstTurn() {
        int r1 = rand.nextInt(6) + rand.nextInt(6) + 2;
        int r2 = rand.nextInt(6) + rand.nextInt(6) + 2;
        currentTurn = (r1 >= r2) ? 1 : 2;
        addLog(CHARACTERS[playerIndex][0] + " rolled " + r1 +
                ", " + bossData[0] + " rolled " + r2 + ".");
        addLog((currentTurn == 1 ? CHARACTERS[playerIndex][0] : bossData[0]) + " goes first!");
        if (currentTurn == 2) {
            Timer t = new Timer(1200, e -> { doBossTurn(); repaint(); });
            t.setRepeats(false); t.start();
        }
    }

    // ── Shake animations ──────────────────────────────────────────────────────
    private void startHitAnimation(int who) {
        if (shakeTimer != null && shakeTimer.isRunning()) shakeTimer.stop();
        isDefendFlash = false; shakingPlayer = who;
        shakeFrames = SHAKE_TOTAL_FRAMES; shakeDir = 1; spriteFlashAlpha = 255;
        shakeTimer = new Timer(SHAKE_INTERVAL_MS, e -> {
            if (shakeFrames <= 0) {
                shakeOffsetX = 0; shakeOffsetY = 0;
                spriteFlashAlpha = 0; shakingPlayer = 0;
                shakeTimer.stop(); repaint(); return;
            }
            double p = (double) shakeFrames / SHAKE_TOTAL_FRAMES;
            double eased = p * p;
            shakeOffsetX = (int)(20 * eased * shakeDir);
            shakeOffsetY = (p > 0.5) ? ((shakeFrames%3==0)?-8:(shakeFrames%3==1?4:0)) : 0;
            spriteFlashAlpha = (int)(210 * eased);
            shakeDir = -shakeDir; shakeFrames--; repaint();
        });
        shakeTimer.start();
    }

    private void startDefendAnimation(int who) {
        if (shakeTimer != null && shakeTimer.isRunning()) shakeTimer.stop();
        isDefendFlash = true; shakingPlayer = who;
        shakeFrames = SHAKE_TOTAL_FRAMES; shakeDir = 1; spriteFlashAlpha = 255;
        shakeTimer = new Timer(SHAKE_INTERVAL_MS, e -> {
            if (shakeFrames <= 0) {
                shakeOffsetX = 0; shakeOffsetY = 0;
                spriteFlashAlpha = 0; shakingPlayer = 0; isDefendFlash = false;
                shakeTimer.stop(); repaint(); return;
            }
            double p = (double) shakeFrames / SHAKE_TOTAL_FRAMES;
            double eased = p * p;
            shakeOffsetX = (int)(14 * eased * (who == 1 ? -1 : 1));
            shakeOffsetY = 0;
            spriteFlashAlpha = (int)(190 * eased);
            shakeDir = -shakeDir; shakeFrames--; repaint();
        });
        shakeTimer.start();
    }

    // ── Paint ─────────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth(), h = getHeight();

        if (bgImage != null) g2.drawImage(bgImage, 0, 0, w, h, null);
        else { g2.setColor(new Color(40,20,10)); g2.fillRect(0,0,w,h); }

        drawBossBadge(g2, w);
        drawRoundBadge(g2, w);

        drawHealthBar(g2, playerHp, playerMaxHp, 30, 20, false,
                CHARACTERS[playerIndex][0], p1LabelImg);
        drawHealthBar(g2, bossHp, bossMaxHp, w - 30 - HP_BAR_W, 20, true,
                bossData[0], null);

        int panelY = h - PANEL_H;
        int p1X = (int)(w * 0.08) + (shakingPlayer == 1 ? shakeOffsetX : 0);
        int p1Y = panelY - SPRITE_H   + (shakingPlayer == 1 ? shakeOffsetY : 0);
        int p2X = (int)(w * 0.78)     + (shakingPlayer == 2 ? shakeOffsetX : 0);
        int p2Y = panelY - SPRITE_H   + (shakingPlayer == 2 ? shakeOffsetY : 0);

        drawSpriteIcon(g2, playerSprite, p1X, p1Y, SPRITE_W, SPRITE_H, false, shakingPlayer == 1);
        drawSpriteIcon(g2, bossSprite,   p2X, p2Y, SPRITE_W, SPRITE_H, true,  shakingPlayer == 2);

        if (!gameOver) {
            int arrowX = (currentTurn == 1) ? p1X + SPRITE_W/2 : p2X + SPRITE_W/2;
            drawTurnArrow(g2, arrowX, panelY - SPRITE_H - 12);
        }

        drawActionPanel(g2, 0, panelY, w, h);

        if (gameOver) drawGameOverOverlay(g2, w, h);
    }

    // ── Boss badge ────────────────────────────────────────────────────────────
    private void drawBossBadge(Graphics2D g2, int w) {
        String label = isFinalBoss ? "⚔ FINAL BOSS" :
                isMiniFinal ? "★ MINI-FINAL BOSS" : "MINI BOSS " + (arcadeManager.getCurrentBossIndex() + 1) + "/5";
        Color bg = isFinalBoss ? new Color(180,0,0) :
                isMiniFinal ? new Color(140,0,140) : new Color(60,60,180);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        int bw = fm.stringWidth(label) + 30, bh = 26;
        int bx = (w - bw) / 2, by = 44;
        g2.setColor(bg); g2.fillRoundRect(bx, by, bw, bh, 10, 10);
        g2.setColor(Color.WHITE); g2.drawString(label, bx + 15, by + bh - 7);
    }

    // ── Round badge ───────────────────────────────────────────────────────────
    private void drawRoundBadge(Graphics2D g2, int w) {
        String txt = "ROUND " + roundCount;
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        int bw = fm.stringWidth(txt) + 30, bh = 28;
        int bx = (w - bw) / 2, by = 10;
        g2.setColor(new Color(240,235,210)); g2.fillRoundRect(bx, by, bw, bh, 12, 12);
        g2.setColor(new Color(80,60,30)); g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(bx, by, bw, bh, 12, 12);
        g2.setColor(new Color(60,40,20)); g2.drawString(txt, bx + 15, by + bh - 8);
    }

    // ── Health bar ────────────────────────────────────────────────────────────
    private void drawHealthBar(Graphics2D g2, int hp, int maxHp,
                               int x, int y, boolean flip,
                               String name, BufferedImage labelImg) {
        double pct = (double) hp / maxHp;
        BufferedImage bar = getHpBarImage(pct);
        int labelW = 60, labelH = 25;
        if (bar != null) {
            if (flip) g2.drawImage(bar, x+HP_BAR_W, y, -HP_BAR_W, HP_BAR_H, null);
            else      g2.drawImage(bar, x, y, HP_BAR_W, HP_BAR_H, null);
        } else {
            g2.setColor(new Color(60,60,60,180));
            g2.fillRoundRect(x, y, HP_BAR_W, HP_BAR_H, 10, 10);
            Color bc = pct>0.6?new Color(80,200,80):pct>0.3?new Color(220,180,30):new Color(200,60,60);
            g2.setColor(bc);
            g2.fillRoundRect(x+2, y+2, (int)((HP_BAR_W-4)*pct), HP_BAR_H-4, 8, 8);
        }
        g2.setFont(new Font("Arial", Font.BOLD, 13)); g2.setColor(Color.WHITE);
        String hpTxt = hp + " / " + maxHp;
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hpTxt, x+(HP_BAR_W-fm.stringWidth(hpTxt))/2, y+HP_BAR_H+16);
        g2.setFont(new Font("Arial", Font.BOLD, 14)); g2.setColor(new Color(255,220,100));
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(name.toUpperCase(), x+(HP_BAR_W-fm2.stringWidth(name.toUpperCase()))/2, y+HP_BAR_H+34);
        if (labelImg != null)
            g2.drawImage(labelImg, x+(HP_BAR_W-labelW)/2, y+HP_BAR_H+40, labelW, labelH, null);
    }

    private BufferedImage getHpBarImage(double pct) {
        if (pct<=0.0) return hpBars[0]; if (pct<=0.20) return hpBars[1];
        if (pct<=0.40) return hpBars[2]; if (pct<=0.60) return hpBars[3];
        if (pct<=0.80) return hpBars[4]; return hpBars[5];
    }

    // ── Turn arrow ────────────────────────────────────────────────────────────
    private void drawTurnArrow(Graphics2D g2, int cx, int y) {
        int[] px={cx-10,cx+10,cx}, py={y,y,y+14};
        g2.setColor(Color.BLACK); g2.fillPolygon(px,py,3);
        g2.setStroke(new BasicStroke(1.5f)); g2.drawPolygon(px,py,3);
    }

    // ── Sprite ────────────────────────────────────────────────────────────────
    private void drawSpriteIcon(Graphics2D g2, ImageIcon icon,
                                int x, int y, int w, int h,
                                boolean flipX, boolean isHit) {
        if (icon != null) {
            Image img = icon.getImage();
            if (flipX) g2.drawImage(img, x+w, y, -w, h, this);
            else       g2.drawImage(img, x,   y,  w, h, this);
            if (isHit && spriteFlashAlpha > 0) {
                BufferedImage flash = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D fg = flash.createGraphics();
                if (flipX) fg.drawImage(img, w, 0, -w, h, null);
                else       fg.drawImage(img, 0, 0,  w, h, null);
                fg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, spriteFlashAlpha/255f));
                fg.setColor(isDefendFlash ? new Color(80,160,255) : Color.WHITE);
                fg.fillRect(0, 0, w, h); fg.dispose();
                g2.drawImage(flash, x, y, null);
            }
        } else {
            g2.setColor(flipX ? new Color(220,80,80) : new Color(80,140,255));
            g2.fillRoundRect(x+40, y, 80, 120, 10, 10);
            g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("?", x+74, y+65);
        }
    }

    // ── Dice ──────────────────────────────────────────────────────────────────
    private void drawDice(Graphics2D g2, int x, int y) {
        int s = 55;
        drawSingleDie(g2, x,     y, die1Val, s);
        drawSingleDie(g2, x+s+8, y, die2Val, s);
        g2.setFont(new Font("Arial", Font.BOLD, 14)); g2.setColor(new Color(60,40,10));
        g2.drawString("= "+(die1Val+die2Val), x+s*2+16, y+22);
        g2.setColor(new Color(160,60,0));
        g2.drawString("DMG: "+lastDamage, x+s*2+16, y+42);
    }

    private void drawSingleDie(Graphics2D g2, int x, int y, int val, int s) {
        if (val<1||val>6) return;
        BufferedImage di = diceImages[val-1];
        if (di != null) { g2.drawImage(di, x, y, s, s, null); return; }
        g2.setColor(Color.WHITE); g2.fillRoundRect(x,y,s,s,8,8);
        g2.setColor(new Color(80,80,80)); g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x,y,s,s,8,8);
        g2.setColor(new Color(30,30,30));
        int m=s/2,q=s/4,ds=Math.max(4,s/8);
        int[][] dots = getDots(val,x,y,m,q);
        for (int[] d : dots) g2.fillOval(d[0],d[1],ds,ds);
    }

    private int[][] getDots(int v,int x,int y,int m,int q){
        switch(v){
            case 1: return new int[][]{{x+m-4,y+m-4}};
            case 2: return new int[][]{{x+q-4,y+q-4},{x+3*q-4,y+3*q-4}};
            case 3: return new int[][]{{x+q-4,y+q-4},{x+m-4,y+m-4},{x+3*q-4,y+3*q-4}};
            case 4: return new int[][]{{x+q-4,y+q-4},{x+3*q-4,y+q-4},{x+q-4,y+3*q-4},{x+3*q-4,y+3*q-4}};
            case 5: return new int[][]{{x+q-4,y+q-4},{x+3*q-4,y+q-4},{x+m-4,y+m-4},{x+q-4,y+3*q-4},{x+3*q-4,y+3*q-4}};
            case 6: return new int[][]{{x+q-4,y+q-4},{x+3*q-4,y+q-4},{x+q-4,y+m-4},{x+3*q-4,y+m-4},{x+q-4,y+3*q-4},{x+3*q-4,y+3*q-4}};
            default: return new int[][]{};
        }
    }

    // ── Action panel ──────────────────────────────────────────────────────────
    private void drawActionPanel(Graphics2D g2, int px, int py, int w, int h) {
        g2.setColor(new Color(210,185,140)); g2.fillRect(px, py, w, PANEL_H);
        g2.setColor(new Color(160,130,90)); g2.setStroke(new BasicStroke(3));
        g2.drawRect(px, py, w, PANEL_H);

        int btnW=180, btnH=55;
        int centerX = w/2-btnW-10;
        int row1=py+20, row2=py+85;
        attackRect.setBounds(centerX,          row1,btnW,btnH);
        specialRect.setBounds(centerX+btnW+20, row1,btnW,btnH);
        defendRect.setBounds(centerX,           row2,btnW,btnH);
        wildRect.setBounds(centerX+btnW+20,     row2,btnW,btnH);

        boolean canSpecial = playerSkillCd == 0;
        boolean canDefend  = playerDefendCd == 0;
        boolean hasWild    = playerWildcard != null;

        // BUG FIX: buttons are only active when NO action is in flight
        // actionLocked covers the entire attack-roll-resolve pipeline
        boolean buttonsActive = !gameOver && currentTurn==1
                && !waitingForRoll && !actionLocked && !showDice;

        if (buttonsActive) {
            drawBtn(g2,attackRect, hoverAttack, "▶  ATTACK",true);
            drawBtn(g2,specialRect,hoverSpecial,"SPECIAL",  canSpecial);
            drawBtn(g2,defendRect, hoverDefend, "DEFEND",   canDefend);
            drawBtn(g2,wildRect,   hoverWild,   "WILDCARD", hasWild);
        } else {
            drawBtn(g2,attackRect, false,"▶  ATTACK",false);
            drawBtn(g2,specialRect,false,"SPECIAL",  false);
            drawBtn(g2,defendRect, false,"DEFEND",   false);
            drawBtn(g2,wildRect,   false,"WILDCARD", false);
        }

        int rightX = w*2/3+20, rightW = w/3-40, rightCenterX = rightX+rightW/2;

        if (!logLine2.isEmpty()) {
            g2.setFont(new Font("Arial",Font.PLAIN,12)); g2.setColor(new Color(100,60,20));
            FontMetrics fl2=g2.getFontMetrics();
            g2.drawString(logLine2, rightCenterX-fl2.stringWidth(logLine2)/2, py+22);
        }
        if (!logLine3.isEmpty()) {
            g2.setFont(new Font("Arial",Font.BOLD,12)); g2.setColor(new Color(80,40,0));
            FontMetrics fl3=g2.getFontMetrics();
            g2.drawString(logLine3, rightCenterX-fl3.stringWidth(logLine3)/2, py+40);
        }

        g2.setColor(new Color(160,120,70,120)); g2.setStroke(new BasicStroke(1));
        g2.drawLine(rightX,py+50,rightX+rightW,py+50);

        String sub = actionLocked || waitingForRoll ? "CLICK TO ROLL!" :
                currentTurn==2 ? "BOSS THINKING..." : "";
        if (!sub.isEmpty()) {
            // While action is locked, show "RESOLVING..." instead of "CLICK TO ROLL!"
            if (actionLocked && !waitingForRoll) sub = "RESOLVING...";
            g2.setFont(new Font("Arial",Font.BOLD,14)); g2.setColor(new Color(120,70,10));
            FontMetrics fs=g2.getFontMetrics();
            g2.drawString(sub, rightCenterX-fs.stringWidth(sub)/2, py+68);
        }

        g2.setFont(new Font("Arial",Font.PLAIN,12)); g2.setColor(new Color(100,60,20));
        String cdS = "Special CD: "+(playerSkillCd>0?playerSkillCd:"Ready");
        String cdD = "Defend CD:  "+(playerDefendCd>0?playerDefendCd:"Ready");
        FontMetrics fc=g2.getFontMetrics();
        g2.drawString(cdS, rightCenterX-fc.stringWidth(cdS)/2, py+85);
        g2.drawString(cdD, rightCenterX-fc.stringWidth(cdD)/2, py+102);

        g2.setFont(new Font("Arial",Font.BOLD,13)); g2.setColor(new Color(180,140,0));
        String coinTxt = "Coins: "+arcadeManager.getCoins();
        FontMetrics fco=g2.getFontMetrics();
        g2.drawString(coinTxt, rightCenterX-fco.stringWidth(coinTxt)/2, py+122);

        if (playerWildcard!=null) {
            g2.setFont(new Font("Arial",Font.BOLD,12)); g2.setColor(new Color(140,0,180));
            String wt="WILDCARD: "+playerWildcard;
            FontMetrics fw=g2.getFontMetrics();
            g2.drawString(wt, rightCenterX-fw.stringWidth(wt)/2, py+140);
        }

        if (showDice) drawDice(g2, rightX, py+145);
    }

    // ── Draw button ───────────────────────────────────────────────────────────
    private void drawBtn(Graphics2D g2, Rectangle r, boolean hover, String label, boolean enabled) {
        BufferedImage img = null;
        switch(label) {
            case "▶  ATTACK": img=btnAttackImg;   break;
            case "SPECIAL":   img=btnSpecialImg;  break;
            case "DEFEND":    img=btnDefendImg;   break;
            case "WILDCARD":  img=btnWildcardImg; break;
        }
        if (img!=null) {
            float alpha=enabled?1.0f:0.4f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
            g2.drawImage(img,r.x,r.y,r.width,r.height,null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
        } else {
            Color bg=!enabled?new Color(60,60,60,160):hover?new Color(100,180,80):new Color(30,100,50,200);
            g2.setColor(bg); g2.fillRoundRect(r.x,r.y,r.width,r.height,12,12);
            g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,15));
            FontMetrics fm=g2.getFontMetrics();
            g2.drawString(label,r.x+(r.width-fm.stringWidth(label))/2,
                    r.y+(r.height+fm.getAscent()-fm.getDescent())/2);
        }
        if (hover&&enabled&&arrowImg!=null)
            g2.drawImage(arrowImg,r.x-2,r.y+(r.height-25)/2,25,25,null);
    }

    // ── Game over overlay ─────────────────────────────────────────────────────
    private void drawGameOverOverlay(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0,0,0,160)); g2.fillRect(0,0,w,h);
        g2.setFont(new Font("Arial",Font.BOLD,52));
        g2.setColor(playerWon?new Color(255,220,50):new Color(220,60,60));
        String title=playerWon?CHARACTERS[playerIndex][0]+" WINS!":"DEFEATED!";
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(title,(w-fm.stringWidth(title))/2,h/2-20);
        g2.setFont(new Font("Arial",Font.PLAIN,18)); g2.setColor(new Color(200,200,200));
        String sub=playerWon?"Heading to shop...":"Returning to menu...";
        FontMetrics fm2=g2.getFontMetrics();
        g2.drawString(sub,(w-fm2.stringWidth(sub))/2,h/2+30);
    }

    // ── Log ───────────────────────────────────────────────────────────────────
    private void addLog(String msg) {
        logLine1=logLine2; logLine2=logLine3; logLine3=msg;
    }

    // ── Action chosen ─────────────────────────────────────────────────────────
    private void onActionChosen(String action) {
        // BUG FIX: hard lock — if we're already processing any action, ignore
        if (actionLocked) return;

        String pName = CHARACTERS[playerIndex][0];
        String bName = bossData[0];
        switch(action) {
            case "ATTACK":
                actionLocked = true;   // lock immediately on first click
                waitingForRoll = true;
                addLog(pName+" winds up for an attack!");
                repaint();
                break;
            case "SPECIAL":
                if (playerSkillCd>0){addLog("Special on cooldown!");return;}
                actionLocked = true;
                doPlayerSpecial(pName,bName);
                endTurn();
                break;
            case "DEFEND":
                if (playerDefendCd>0){addLog("Defend on cooldown!");return;}
                actionLocked = true;
                playerDefending=true; playerDefendCd=3;
                addLog(pName+" takes a defensive stance!");
                startDefendAnimation(1);
                endTurn();
                break;
            case "WILDCARD":
                if (playerWildcard==null){addLog("No wildcard!");return;}
                actionLocked = true;
                doWildcard(pName,bName);
                playerWildcard=null;
                endTurn();
                break;
        }
    }

    // ── Player roll ───────────────────────────────────────────────────────────
    private void doRollAndAttack() {
        // BUG FIX: drop any extra clicks that arrived before we cleared waitingForRoll
        if (!waitingForRoll) return;

        waitingForRoll = false;
        // actionLocked is already true (set when "ATTACK" was chosen)

        String pName=CHARACTERS[playerIndex][0];
        String bName=bossData[0];

        die1Val=rand.nextInt(6)+1; die2Val=rand.nextInt(6)+1;
        int total=die1Val+die2Val;
        double mult=Double.parseDouble(CHARACTERS[playerIndex][3]);
        int damage=(int)(total*mult);

        if (bossDefending) {
            damage=Math.max(1,damage/2); bossDefending=false;
            addLog(bName+" blocked! Damage halved.");
        }
        final int fd=damage; lastDamage=fd;
        bossHp=Math.max(0,bossHp-fd); startHitAnimation(2);

        int animMs=SHAKE_TOTAL_FRAMES*SHAKE_INTERVAL_MS;
        Timer t=new Timer(animMs,e->{
            showDice=true;
            addLog(pName+" rolled "+die1Val+"+"+die2Val+" = "+total+" × "+mult+"×");
            addLog(pName+" deals "+fd+" damage to "+bName+"!");
            if (roundCount%3==0&&rand.nextInt(100)<30) {
                String[] wcs={"FREEZE","DOUBLE ROLL","HEAL","SHIELD"};
                playerWildcard=wcs[rand.nextInt(wcs.length)];
                addLog(pName+" drew wildcard: "+playerWildcard+"!");
            }
            repaint();
            Timer hide=new Timer(2000,e2->{
                showDice=false;
                checkGameOver();
                if (!gameOver) {
                    actionLocked = false;  // BUG FIX: only unlock when fully resolved
                    endTurn();
                }
                repaint();
            });
            hide.setRepeats(false); hide.start();
        });
        t.setRepeats(false); t.start();
    }

    // ── Player special ────────────────────────────────────────────────────────
    private void doPlayerSpecial(String pName, String bName) {
        playerSkillCd=5;
        String charName=CHARACTERS[playerIndex][0];
        switch(charName) {
            case "Echo":   addLog(pName+" uses Phantom Dance! Dodges next 2!"); break;
            case "Zyah":
                addLog(pName+" uses Dancehall Fever! Extra turn!");
                // Extra turn: reset lock, set waitingForRoll so player clicks to roll
                actionLocked = false;
                waitingForRoll = true;
                repaint();
                return;
            case "Raze":
                die1Val=rand.nextInt(6)+1; die2Val=rand.nextInt(6)+1;
                int dmg=(int)((die1Val+die2Val)*Double.parseDouble(CHARACTERS[playerIndex][3]))+8;
                lastDamage=dmg; showDice=true;
                bossHp=Math.max(0,bossHp-dmg); startHitAnimation(2);
                addLog(pName+" uses Blazing Combo! +"+dmg+" dmg!"); break;
            case "Vibe":   playerDefending=true; addLog(pName+" uses House Foundation!"); break;
            case "Torque": bossStunned=true; bossStunTurns=2; startHitAnimation(2);
                addLog(pName+" uses Earthquake Stomp! "+bName+" stunned!"); break;
            case "Luma":   playerHp=Math.min(playerMaxHp,playerHp+30);
                addLog(pName+" uses Radiant Burst! +30 HP!"); break;
            case "Lyric":  playerHp=Math.min(playerMaxHp,playerHp+35);
                addLog(pName+" uses Healing Freestyle! +35 HP!"); break;
            case "Ayo":    addLog(pName+" uses Ancestral Call! Will revive at 50%!"); break;
        }
    }

    // ── Wildcard ──────────────────────────────────────────────────────────────
    private void doWildcard(String pName, String bName) {
        switch(playerWildcard) {
            case "FREEZE":
                bossStunned=true; bossStunTurns=1; startHitAnimation(2);
                addLog(pName+" used FREEZE! "+bName+" loses next turn!"); break;
            case "DOUBLE ROLL":
                addLog(pName+" used DOUBLE ROLL!");
                // Similar to Zyah extra turn — let player click to roll
                actionLocked = false;
                waitingForRoll = true;
                repaint();
                return;
            case "HEAL":
                playerHp=Math.min(playerMaxHp,playerHp+20);
                addLog(pName+" used HEAL! +20 HP!"); break;
            case "SHIELD":
                playerDefending=true;
                addLog(pName+" used SHIELD! Next hit -50%!"); break;
        }
    }

    // ── Boss turn ─────────────────────────────────────────────────────────────
    private void doBossTurn() {
        if (gameOver) return;
        String bName=bossData[0];
        String pName=CHARACTERS[playerIndex][0];
        int dec=rand.nextInt(100);
        if (bossSkillCd==0&&dec<30) {
            doBossSpecial(bName,pName); bossSkillCd=5;
        } else if (bossDefendCd==0&&bossHp<bossMaxHp*0.4&&dec<50) {
            bossDefending=true; bossDefendCd=3;
            addLog(bName+" takes a defensive stance!"); startDefendAnimation(2);
        } else {
            addLog(bName+" attacks "+pName+"!"); doBossRoll(); return;
        }
        checkGameOver(); if (!gameOver) endTurn();
    }

    // ── Boss special ──────────────────────────────────────────────────────────
    private void doBossSpecial(String bName, String pName) {
        String cls=bossData[1];
        switch(cls) {
            case "Support":
                bossHp=Math.min(bossMaxHp,bossHp+20);
                addLog(bName+" uses Healing Aura! +20 HP!"); break;
            case "Assassin":
                bossStunned=false;
                addLog(bName+" uses Shadow Strike! Dodges next!"); break;
            case "Fighter":
                die1Val=rand.nextInt(6)+1; die2Val=rand.nextInt(6)+1;
                int dmg=(int)((die1Val+die2Val)*Double.parseDouble(bossData[3]))+8;
                lastDamage=dmg; showDice=true;
                playerHp=Math.max(0,playerHp-dmg); startHitAnimation(1);
                addLog(bName+" uses Flame Burst! +"+dmg+" dmg!"); break;
            case "Tank":
                playerStunned=true; playerStunTurns=2; startHitAnimation(1);
                addLog(bName+" uses Ground Slam! "+pName+" stunned!"); break;
            case "Master":
                die1Val=rand.nextInt(6)+1; die2Val=rand.nextInt(6)+1;
                int fdmg=(int)((die1Val+die2Val)*Double.parseDouble(bossData[3]))+5;
                lastDamage=fdmg;
                playerHp=Math.max(0,playerHp-fdmg); startHitAnimation(1);
                addLog(bName+" uses HAGBONG KA SAKEN BOI! +"+fdmg+" dmg!"); break;
        }
    }

    // ── Boss roll ─────────────────────────────────────────────────────────────
    private void doBossRoll() {
        die1Val=rand.nextInt(6)+1; die2Val=rand.nextInt(6)+1;
        int total=die1Val+die2Val;
        double mult=Double.parseDouble(bossData[3]);
        int damage=(int)(total*mult);
        if (playerDefending) {
            damage=Math.max(1,damage/2); playerDefending=false;
            addLog(CHARACTERS[playerIndex][0]+" blocked! Damage halved.");
        }
        final int fd=damage; lastDamage=fd;
        playerHp=Math.max(0,playerHp-fd); startHitAnimation(1);

        int animMs=SHAKE_TOTAL_FRAMES*SHAKE_INTERVAL_MS;
        Timer t=new Timer(animMs,e->{
            showDice=true;
            addLog(bossData[0]+" rolled "+die1Val+"+"+die2Val+" = "+total+" × "+mult+"×");
            addLog("Deals "+fd+" damage to "+CHARACTERS[playerIndex][0]+"!");
            repaint();
            Timer hide=new Timer(2000,e2->{
                showDice=false; checkGameOver();
                if (!gameOver) endTurn(); repaint();
            });
            hide.setRepeats(false); hide.start();
        });
        t.setRepeats(false); t.start();
    }

    // ── End turn ──────────────────────────────────────────────────────────────
    private void endTurn() {
        if (playerSkillCd>0)  playerSkillCd--;
        if (bossSkillCd>0)    bossSkillCd--;
        if (playerDefendCd>0) playerDefendCd--;
        if (bossDefendCd>0)   bossDefendCd--;

        if (playerStunned){playerStunTurns--;if(playerStunTurns<=0){playerStunned=false;addLog(CHARACTERS[playerIndex][0]+" recovered!");}}
        if (bossStunned)  {bossStunTurns--;  if(bossStunTurns<=0)  {bossStunned=false;  addLog(bossData[0]+" recovered!");}}

        currentTurn=(currentTurn==1)?2:1;
        if (currentTurn==1) roundCount++;

        if (currentTurn==1&&playerStunned){addLog(CHARACTERS[playerIndex][0]+" is stunned!"); currentTurn=2;}
        else if (currentTurn==2&&bossStunned){addLog(bossData[0]+" is stunned!"); currentTurn=1;}

        // BUG FIX: unlock action when it becomes the player's turn again
        // (the boss turn manages its own async flow and doesn't need the lock)
        if (currentTurn == 1) actionLocked = false;

        repaint();
        if (!gameOver&&currentTurn==2) {
            Timer t=new Timer(1200,e->{doBossTurn();repaint();});
            t.setRepeats(false); t.start();
        }
    }

    // ── Check game over ───────────────────────────────────────────────────────
    private void checkGameOver() {
        // BUG FIX: guard against re-entrant calls caused by multi-click spam
        if (gameOver) return;

        if (playerHp<=0||bossHp<=0) {
            gameOver=true;
            playerWon=(bossHp<=0);
            actionLocked=true; // keep buttons dead permanently after game ends
            addLog(playerWon?CHARACTERS[playerIndex][0]+" wins!":bossData[0]+" wins!");
            arcadeManager.setPlayerHp(playerHp);
            repaint();
            Timer t=new Timer(3000,e->{
                if (playerWon) arcadeManager.onBattleWon();
                else           arcadeManager.onBattleLost();
            });
            t.setRepeats(false); t.start();
        }
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────
    private void addMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                hoverAttack  = attackRect.contains(e.getPoint());
                hoverSpecial = specialRect.contains(e.getPoint());
                hoverDefend  = defendRect.contains(e.getPoint());
                hoverWild    = wildRect.contains(e.getPoint());
                setCursor((hoverAttack||hoverSpecial||hoverDefend||hoverWild)
                        ?Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        :Cursor.getDefaultCursor());
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                // BUG FIX: primary gate — reject ALL clicks while locked or game is over
                if (gameOver)      return;
                if (actionLocked && !waitingForRoll) return;

                // "CLICK TO ROLL!" phase: accept exactly one click, then lock again
                if (waitingForRoll) {
                    doRollAndAttack();
                    return;
                }

                if (currentTurn!=1) return;

                if      (attackRect.contains(e.getPoint()))  onActionChosen("ATTACK");
                else if (specialRect.contains(e.getPoint())) onActionChosen("SPECIAL");
                else if (defendRect.contains(e.getPoint()))  onActionChosen("DEFEND");
                else if (wildRect.contains(e.getPoint()))    onActionChosen("WILDCARD");
            }
        });
    }
}
