package tetrcore;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.Titles;

import tetr.core.minecraft.Main;
import tetr.core.minecraft.Room;

public class GameLogic {
    /*
     * abcd abcd abc1 xabc ab1d xabd ab11 xxab a1cd xacd a1c1 xxac a11d xxad a111
     * xxxa 1bcd xbcd 1bc1 xxbc 1b1d xxbd 1b11 xxxb 11cd xxcd 11c1 xxxc 111d xxxd
     * 1111 xxxx
     */

    private enum ClearType {
        SINGLE(100, "Single"), DOUBLE(300, "Double"), TRIPLE(500, "Triple"), QUAD(800, "Quad"),
        SPIN_MINI(100, "Spin Mini"), SPIN(400, "Spin"), SPIN_MINI_SINGLE(200, "Spin Mini Single"),
        SPIN_SINGLE(800, "Spin Single"), SPIN_MINI_DOUBLE(400, "Spin Mini Double"), SPIN_DOUBLE(1200, "Spin Double"),
        SPIN_TRIPLE(1600, "Spin Triple"), SPIN_QUAD(2600, "Spin Quad"), COMBO(50), ALL_CLEAR(3500, "All Clear"), SD(1),
        HD(2);

        private final int score;
        private final String description;

        ClearType(int score, String description) {
            this.score = score;
            this.description = description;
        }

        ClearType(int score) {
            this.score = score;
            this.description = null;
        }

        public int getScore() {
            return score;
        }

        public String getDescription() {
            return description;
        }

    }

    class Property {
        private double base;
        private int delay;
        private double delta;
        private boolean mode;
        private double limit;

        private double workingValue;

        private Property(double base, int delay, double delta, double limit) {
            this.base = base;
            this.delay = delay;
            this.delta = delta;
            if (delta > 0) {
                mode = true;
            } else {
                mode = false;
            }
            this.limit = limit;
        }

        private double getWorkingValue() {
            return workingValue;
        }

        private int getIncreaseDelay() {
            return delay;
        }

        private double getLimit() {
            return limit;
        }

        private void tick() {
            workingValue += delta;
        }

        private void start() {
            workingValue = base;

            Timer timer = new Timer();

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (mode ? (getWorkingValue() < getLimit()) : (getLimit() < getWorkingValue())) {
                                tick();
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            workingValue = limit;
                        }
                    }).start();
                }
            };

            timer.schedule(task, getIncreaseDelay() * 1000);
        }

    }

    private Player player;
    public String magicString = "";
    private int magicStringsActive = 0;

    private final Point[][][] pieces = Pieces.pieces;
    private final Point[][][] kicktable = Kicktable.kicktable_srs_guideline_180;
    private final int[][] garbagetable = Garbagetable.tetrio;

    private boolean gameover = false;

    private ArrayList<Integer> garbageQueue = new ArrayList<Integer>();
    private int garbageHole;
    private Property garbage = new Property(4d, 60, 0.05d, 8d);
    private Property garbageMultiplier = new Property(1d, 30, 0.1d, 8d);

    private double counter = 0;
    private Property gravity = new Property(1d, 30, 0.1d, 20d);

    private Property lockDelay = new Property(2d, 30, -0.02d, 0.5d);

    private int timesMoved = 0;
    private static final int MAXIMUMMOVES = 15;

    private int zonelines;
    private boolean zone;

    public static int STAGESIZEX = 10;
    public static int STAGESIZEY = 40;
    public static int VISIBLEROWS = 24;
    public static int NEXTPIECESMAX = 5;

    private int currentPiece;
    private Point currentPiecePosition;
    private int currentPieceRotation;
    private int heldPiece = -1;
    private boolean held;
    private ArrayList<Integer> nextPieces = new ArrayList<Integer>();

    private boolean currentPieceHasSpun;
    private boolean tSpinMini;
    private int combo;
    private int b2b;
    private long score;
    private long totalLinesCleared = 0;
    private long totalPiecesPlaced = 0;
    private long totalGarbageReceived = 0;
    private int[][] stage = new int[STAGESIZEY][STAGESIZEX];

    private String intToPieceName(int p) {
        switch (p) {
        case 0:
            return "z";
        case 1:
            return "l";
        case 2:
            return "o";
        case 3:
            return "s";
        case 4:
            return "i";
        case 5:
            return "j";
        case 6:
            return "t";
        case 7:
            return "_";
        case 8:
            return "#";
        case 16:
            return "w";
        default:
            return null;
        }
    }

    private void addScore(int n) {
        score += n;
    }

    protected void setScore(long l) {
        score = l;
    }

    public int getCombo() {
        return combo;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getMagicStringsActive() {
        return magicStringsActive;
    }

    public void setMagicStringsActive(int magicStringsActive) {
        this.magicStringsActive = magicStringsActive;
    }

    public boolean getGameover() {
        return gameover;
    }

    public void setGameover(boolean gameover) {
        this.gameover = gameover;
    }

    public ArrayList<Integer> getGarbageQueue() {
        return garbageQueue;
    }

    public int getGarbageHole() {
        return garbageHole;
    }

    public void setGarbageHole(int garbageHole) {
        this.garbageHole = garbageHole;
    }

    public double getCounter() {
        return counter;
    }

    public void setCounter(double counter) {
        this.counter = counter;
    }

    public int getTimesMoved() {
        return timesMoved;
    }

    public void setTimesMoved(int timesMoved) {
        this.timesMoved = timesMoved;
    }

    public int getZonelines() {
        return zonelines;
    }

    public void setZonelines(int zonelines) {
        this.zonelines = zonelines;
    }

    public boolean getZone() {
        return zone;
    }

    public void setZone(boolean zone) {
        this.zone = zone;
    }

    public static int getSTAGESIZEX() {
        return STAGESIZEX;
    }

    public static void setSTAGESIZEX(int sTAGESIZEX) {
        STAGESIZEX = sTAGESIZEX;
    }

    public static int getSTAGESIZEY() {
        return STAGESIZEY;
    }

    public static void setSTAGESIZEY(int sTAGESIZEY) {
        STAGESIZEY = sTAGESIZEY;
    }

    public static int getVISIBLEROWS() {
        return VISIBLEROWS;
    }

    public static void setVISIBLEROWS(int vISIBLEROWS) {
        VISIBLEROWS = vISIBLEROWS;
    }

    public static int getNEXTPIECESMAX() {
        return NEXTPIECESMAX;
    }

    public static void setNEXTPIECESMAX(int nEXTPIECESMAX) {
        NEXTPIECESMAX = nEXTPIECESMAX;
    }

    public Point getCurrentPiecePosition() {
        return currentPiecePosition;
    }

    public void setCurrentPiecePosition(Point currentPiecePosition) {
        this.currentPiecePosition = currentPiecePosition;
    }

    public int getCurrentPieceRotation() {
        return currentPieceRotation;
    }

    public void setCurrentPieceRotation(int currentPieceRotation) {
        this.currentPieceRotation = currentPieceRotation;
    }

    public int getHeldPiece() {
        return heldPiece;
    }

    public void setHeldPiece(int heldPiece) {
        this.heldPiece = heldPiece;
    }

    public boolean isHeld() {
        return held;
    }

    public void setHeld(boolean held) {
        this.held = held;
    }

    public ArrayList<Integer> getNextPieces() {
        return nextPieces;
    }

    public void setNextPieces(ArrayList<Integer> nextPieces) {
        this.nextPieces = nextPieces;
    }

    public boolean istSpin() {
        return currentPieceHasSpun;
    }

    public void settSpin(boolean tSpin) {
        this.currentPieceHasSpun = tSpin;
    }

    public boolean istSpinMini() {
        return tSpinMini;
    }

    public void settSpinMini(boolean tSpinMini) {
        this.tSpinMini = tSpinMini;
    }

    public int getB2b() {
        return b2b;
    }

    private void setB2b(int b2b) {
        this.b2b = b2b;
    }

    public long getScore() {
        return score;
    }

    public long getTotalLinesCleared() {
        return totalLinesCleared;
    }

    public void setTotalLinesCleared(long totalLinesCleared) {
        this.totalLinesCleared = totalLinesCleared;
    }

    public long getTotalPiecesPlaced() {
        return totalPiecesPlaced;
    }

    public void setTotalPiecesPlaced(long totalPiecesPlaced) {
        this.totalPiecesPlaced = totalPiecesPlaced;
    }

    public long getTotalGarbageReceived() {
        return totalGarbageReceived;
    }

    public void setTotalGarbageReceived(long totalGarbageReceived) {
        this.totalGarbageReceived = totalGarbageReceived;
    }

    public int[][] getStage() {
        return stage;
    }

    public void setStage(int[][] stage) {
        this.stage = stage;
    }

    public String getMagicString() {
        return magicString;
    }

    public Point[][][] getPieces() {
        return pieces;
    }

    public Point[][][] getKicktable() {
        return kicktable;
    }

    public int[][] getGarbagetable() {
        return garbagetable;
    }

    public static int getMaximummoves() {
        return MAXIMUMMOVES;
    }

    public void setCurrentPiece(int currentPiece) {
        this.currentPiece = currentPiece;
    }

    public void setCombo(int combo) {
        this.combo = combo;
    }

    public GameLogic(Player player) {
        this.player = player;
    }

    public GameLogic() {
        this.player = null;
    }

    private boolean checkOOBE(int x, int y) {
        if (y < 0 || STAGESIZEY <= y || x < 0 || STAGESIZEX <= x) {
            return true;
        }
        return false;
    }

    
    private ClearType getClearTypeNow(int l) {
        if (l == 1 && currentPieceHasSpun == false) {
            return ClearType.SINGLE;
        } else if (l == 2 && currentPieceHasSpun == false) {
            return ClearType.DOUBLE;
        } else if (l == 3 && currentPieceHasSpun == false) {
            return ClearType.TRIPLE;
        } else if (l == 4 && currentPieceHasSpun == false) {
            return ClearType.QUAD;
        } else if (l == 1 && currentPieceHasSpun == true && tSpinMini == true) {
            return ClearType.SPIN_MINI_SINGLE;
        } else if (l == 1 && currentPieceHasSpun == true) {
            return ClearType.SPIN_SINGLE;
        } else if (l == 2 && currentPieceHasSpun == true && tSpinMini == true) {
            return ClearType.SPIN_MINI_DOUBLE;
        } else if (l == 2 && currentPieceHasSpun == true) {
            return ClearType.SPIN_DOUBLE;
        } else if (l == 3 && currentPieceHasSpun == true) {
            return ClearType.SPIN_TRIPLE;
        } else if (l == 4 && currentPieceHasSpun == true) {
            return ClearType.SPIN_QUAD;
        } else {
            return null;
        }
    }
    
    private int clearTypeToInt(ClearType c) {
        switch(c) {
        case SINGLE:
            return 0;
        case DOUBLE:
            return 1;
        case TRIPLE:
            return 2;
        case QUAD:
            return 3;
        case SPIN_MINI_SINGLE:
            return 4;
        case SPIN_SINGLE:
            return 5;
        case SPIN_MINI_DOUBLE:
            return 6;
        case SPIN_DOUBLE:
            return 7;
        case SPIN_TRIPLE:
            return 8;
        case SPIN_QUAD:
            return 9;
        default:
            return -1;
        }
    }

    private void sendGarbage(int n) {
        int garbageRemaining = n;
        while (!garbageQueue.isEmpty() && garbageRemaining > 0) {
            garbageQueue.set(0, garbageQueue.get(0) - 1);
            if (garbageQueue.get(0) == 0) {
                garbageQueue.remove(0);
                garbageHole = (int) (Math.random() * STAGESIZEX);
            }
            garbageRemaining--;
        }

        if (garbageRemaining > 0) {
            if (this.player == null) {
                receiveGarbage(garbageRemaining);
            } else {
                Main.inwhichroom.get(player).forwardGarbage(garbageRemaining, player);
            }
        }
    }

    public void receiveGarbage(int n) {
        garbageQueue.add(n);
    }

    private void tryToPutGarbage() {
        for (int h = 0; h < garbage.getWorkingValue(); h++) {
            if (!garbageQueue.isEmpty()) {
                putGarbageLine(garbageHole);

                garbageQueue.set(0, garbageQueue.get(0) - 1);
                if (garbageQueue.get(0) == 0) {
                    garbageQueue.remove(0);
                    garbageHole = (int) (Math.random() * STAGESIZEX);
                }
            }
        }
    }

    private void putGarbageLine(int hole) {
        for (int i = 0; i < STAGESIZEY - 1; i++) {
            for (int j = 0; j < STAGESIZEX; j++) {
                stage[i][j] = stage[i + 1][j];
            }
        }
        for (int j = 0; j < STAGESIZEX; j++) {
            if (j == garbageHole) {
                stage[STAGESIZEY - 1][j] = 7;
            } else {
                stage[STAGESIZEY - 1][j] = 8;
            }
        }

        totalGarbageReceived++;
    }

    public Point[] getCurrentPiece() {
        return pieces[currentPiece][currentPieceRotation];
    }

    public int getCurrentPieceInt() {
        return currentPiece;
    }

    public boolean topOutCheck() {
        for (Point point : pieces[currentPiece][currentPieceRotation]) {
            if (stage[point.y + currentPiecePosition.y][point.x + currentPiecePosition.x] != 7) {
                if (zone) {
                    stopZone();
                } else {
                    gameover = true;
                }
                return true;
            }
        }
        return false;
    }

    public void startZone() {
        zone = true;
        magicString = "Zone activated";
    }

    private void stopZone() {
        zone = false;
        for (int i = 0; i < STAGESIZEY; i++) {
            for (int j = 0; j < STAGESIZEX; j++) {
                if (STAGESIZEY - zonelines - 1 - i >= 0) {
                    stage[STAGESIZEY - 1 - i][j] = stage[STAGESIZEY - zonelines - 1 - i][j];
                } else {
                    stage[STAGESIZEY - 1 - i][j] = 7;
                }
            }
        }
        zonelines = 0;
    }

    public boolean checkTSpin() {
        if (currentPiece == 6) {
            boolean corners[] = { true, true, true, true };
            if (!checkOOBE(currentPiecePosition.x, currentPiecePosition.y)) {
                if (stage[currentPiecePosition.y][currentPiecePosition.x] == 7) {
                    corners[0] = false;
                }
            }

            if (!checkOOBE(currentPiecePosition.x + 2, currentPiecePosition.y)) {
                if (stage[currentPiecePosition.y][currentPiecePosition.x + 2] == 7) {
                    corners[1] = false;
                }
            }

            if (!checkOOBE(currentPiecePosition.x, currentPiecePosition.y + 2)) {
                if (stage[currentPiecePosition.y + 2][currentPiecePosition.x] == 7) {
                    corners[2] = false;
                }
            }

            if (!checkOOBE(currentPiecePosition.x + 2, currentPiecePosition.y + 2)) {
                if (stage[currentPiecePosition.y + 2][currentPiecePosition.x + 2] == 7) {
                    corners[3] = false;
                }
            }

            int cornersFilled = 0;
            for (int i = 0; i < 4; i++) {
                if (corners[i] == true) {
                    cornersFilled++;
                }
            }

            if (cornersFilled >= 3) {

                if (player != null) {
                    for (int i = 0; i < 3; i++) {
                        player.playSound(player.getEyeLocation(), XSound.BLOCK_END_PORTAL_FRAME_FILL.parseSound(), 1f,
                                1f);
                    }
                }
                tSpinMini = true;

                switch (currentPieceRotation) {
                case 0:
                    if (corners[0] && corners[1]) {
                        tSpinMini = false;
                    }
                    break;
                case 1:
                    if (corners[1] && corners[3]) {
                        tSpinMini = false;
                    }
                    break;
                case 2:
                    if (corners[3] && corners[2]) {
                        tSpinMini = false;
                    }
                    break;
                case 3:
                    if (corners[2] && corners[0]) {
                        tSpinMini = false;
                    }
                    break;
                }
                return true;
            }
        }
        return false;
    }

    public void initGame() {
        for (int i = 0; i < STAGESIZEY; i++) {
            for (int j = 0; j < STAGESIZEX; j++) {
                stage[i][j] = 7;
            }
        }
        setGameover(false);
        nextPieces.clear();
        setHeldPiece(-1);
        setHeld(false);
        setScore(0);
        setCombo(-1);
        setB2b(-1);

        totalLinesCleared = 0;
        totalPiecesPlaced = 0;
        totalGarbageReceived = 0;

        zone = false;
        zonelines = 0;

        garbageQueue.clear();
        garbageHole = (int) (Math.random() * STAGESIZEX);

        magicString = "";

        makeNextPiece();
        gameLoop();
    }

    public boolean holdPiece() {
        if (!held) {
            int temp;

            // if first hold
            if (heldPiece == -1) {
                heldPiece = currentPiece;
                makeNextPiece();
            } else {
                // swap
                temp = currentPiece;
                currentPiece = heldPiece;
                heldPiece = temp;

                currentPiecePosition = new Point(3, 17);
                currentPieceRotation = 0;

                topOutCheck();
            }

            held = true;
            return true;
        } else {
            return false;
        }
    }

    public void makeNextPiece() {
        if (nextPieces.size() <= 7) {
            ArrayList<Integer> bag = new ArrayList<Integer>();
            for (int i = 0; i < 7; i++) {
                bag.add(i);
            }
            Collections.shuffle(bag);
            nextPieces.addAll(bag);
        }

        spawnPiece();

        topOutCheck();
    }

    public void spawnPiece() {
        currentPiecePosition = new Point(3, 17);
        currentPieceRotation = 0;
        currentPiece = nextPieces.get(0);
        nextPieces.remove(0);
        held = false;
        settSpin(false);
    }

    public boolean collides(int x, int y, int rotation) {
        for (Point point : pieces[currentPiece][rotation]) {
            // first we check if the piece is inside borders
            if ((0 <= point.y + y && point.y + y < STAGESIZEY) && (0 <= point.x + x && point.x + x < STAGESIZEX)) {
                // check for the collision with other pieces
                if (stage[point.y + y][point.x + x] != 7) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean rotatePiece(int d) {
        int newRotation = (currentPieceRotation + d + 4) % 4;

        int special = -1;

        if (currentPieceRotation == 0 && newRotation == 1) {
            special = 0;
        } else if (currentPieceRotation == 1 && newRotation == 0) {
            special = 1;
        } else if (currentPieceRotation == 1 && newRotation == 2) {
            special = 2;
        } else if (currentPieceRotation == 2 && newRotation == 1) {
            special = 3;
        } else if (currentPieceRotation == 2 && newRotation == 3) {
            special = 4;
        } else if (currentPieceRotation == 3 && newRotation == 2) {
            special = 5;
        } else if (currentPieceRotation == 3 && newRotation == 0) {
            special = 6;
        } else if (currentPieceRotation == 0 && newRotation == 3) {
            special = 7;
        } else if (currentPieceRotation == 0 && newRotation == 2) {
            special = 8;
        } else if (currentPieceRotation == 2 && newRotation == 0) {
            special = 9;
        } else if (currentPieceRotation == 1 && newRotation == 3) {
            special = 10;
        } else if (currentPieceRotation == 3 && newRotation == 1) {
            special = 11;
        }

        int pieceType = currentPiece == 4 ? 1 : 0;

        int maxtries = kicktable[pieceType][special].length;

        for (int tries = 0; tries < maxtries; tries++) {
            if (movePiece(currentPiecePosition.x + kicktable[pieceType][special][tries].x,
                    currentPiecePosition.y - kicktable[pieceType][special][tries].y, newRotation)) {
                settSpin(checkTSpin());
                return true;
            }
        }

        return false;
    }

    public boolean movePiece(int newX, int newY, int newR) {
        if (!collides(newX, newY, newR)) {
            counter = 0;
            currentPiecePosition.x = newX;
            currentPiecePosition.y = newY;
            currentPieceRotation = newR;
            settSpin(false);
            return true;
        }
        return false;
    }

    public boolean movePieceRelative(int xOffset, int yOffset) {
        return movePiece(currentPiecePosition.x + xOffset, currentPiecePosition.y + yOffset, currentPieceRotation);
    }

    public void hardDropPiece() {
        int lines = 0;
        while (!collides(currentPiecePosition.x, currentPiecePosition.y + lines + 1, currentPieceRotation)) {
            lines++;
        }
        if (lines > 0) {
            movePieceRelative(0, +lines);
            addScore(lines * 2);
        }
        placePiece();
    }

    public void placePiece() {

        totalPiecesPlaced++;

        for (Point point : getCurrentPiece()) {
            stage[currentPiecePosition.y + point.y][currentPiecePosition.x + point.x] = currentPiece;
        }

        if (zone) {
            clearLinesZone();
        } else {
            int linesCleared = clearLines();

            if (linesCleared > 0) {
                combo++;
                if (player != null) {
                    if (linesCleared == 4 || currentPieceHasSpun) {
                        setB2b(getB2b() + 1);
                        if(getB2b() > 0) {
                            for (int i = 0; i < getB2b() + 2; i++) {
                                player.playSound(player.getEyeLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.parseSound(), 1f,
                                        (float) Math.pow(2, (double) (getB2b() * 2 - 12) / 12));
                            }
                        }
                    } else {
                        for (int i = 0; i < linesCleared * 2; i++) {
                            player.playSound(player.getEyeLocation(), XSound.BLOCK_NOTE_BLOCK_HARP.parseSound(), 1f,
                                    (float) Math.pow(2, (double) (combo * 2 - 12) / 12));
                        }
                        if(getB2b() > 1) {
                            setB2b(-1);
                            for (int i = 0; i < 5; i++) {
                                player.playSound(player.getEyeLocation(), XSound.ENTITY_ARMOR_STAND_HIT.parseSound(), 1f, 1f);
                            }
                        }
                    }
                    if (currentPieceHasSpun) {
                        for (int i = 0; i < linesCleared * 2; i++) {
                            player.playSound(player.getEyeLocation(), XSound.ENTITY_FIREWORK_ROCKET_BLAST.parseSound(),
                                    1f, 0.5f);
                        }
                    }
                }
                if ((totalLinesCleared - totalGarbageReceived) * STAGESIZEX + totalGarbageReceived == totalPiecesPlaced
                        * 4) {
                    if (player != null) {
                        player.playSound(player.getEyeLocation(), XSound.BLOCK_ANVIL_LAND.parseSound(), 1f, 0.5f);
                        Titles.sendTitle(player, 20, 20, 20, "", ChatColor.GOLD + "" + ChatColor.BOLD + "ALL CLEAR");
                    }
                    sendGarbage((int) ((garbagetable[clearTypeToInt(getClearTypeNow(linesCleared))][combo] + 10)
                            * garbageMultiplier.getWorkingValue()));
                } else {
                    sendGarbage((int) (garbagetable[clearTypeToInt(getClearTypeNow(linesCleared))][combo]
                            * garbageMultiplier.getWorkingValue()));
                }
                
                setMagicString(
                        (intToPieceName(currentPiece) + "-" + getClearTypeNow(linesCleared).getDescription()).toUpperCase());
            
                addScore(getClearTypeNow(linesCleared).getScore());
            } else {
                combo = -1;
                tryToPutGarbage();
            }

            debug("tspin=" + currentPieceHasSpun + ";combo=" + combo + ";linescleared=" + linesCleared);

        }

        makeNextPiece();
    }

    private void debug(String s) {
    }

    public int clearLines() {
        int numClears = 0;
        boolean yes;
        for (int i = STAGESIZEY - 1; i > 0; i--) {
            yes = true;
            for (int j = 0; j < STAGESIZEX; j++) {
                if (stage[i][j] == 7) {
                    yes = false;
                    break;
                }
            }
            if (yes) {
                clearLine(i);
                i++;
                numClears++;
            }
        }

        return numClears;
    }

    public void clearLine(int line) {

        for (int j = 0; j < STAGESIZEX; j++) {
            stage[0][j] = 7;
        }

        for (int i = line; i > 0; i--) {
            for (int j = 0; j < STAGESIZEX; j++) {
                stage[i][j] = stage[i - 1][j];
            }
        }

        totalLinesCleared++;
    }

    public void clearLinesZone() {

        boolean yes;
        for (int i = STAGESIZEY - 1 - zonelines; i > 0; i--) {
            yes = true;
            for (int j = 0; j < STAGESIZEX; j++) {
                if (stage[i][j] == 7) {
                    yes = false;
                    break;
                }
            }
            if (yes) {
                zonelines++;
                clearLineZone(i);
            }
        }
    }

    public void clearLineZone(int line) {
        int gap = STAGESIZEY - (line + zonelines);
        for (int i = line; i < line + gap; i++) {
            for (int j = 0; j < STAGESIZEX; j++) {
                stage[i][j] = stage[i + 1][j];
            }
        }
        for (int j = 0; j < STAGESIZEX; j++) {
            stage[STAGESIZEY - zonelines][j] = 16;
        }

        magicString = zonelines + " LINES";
    }

    private void setMagicString(String s) {
        new Thread() {
            @Override
            public void run() {
                magicStringsActive++;
                magicString = s + " " + combo + " combo";
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (magicStringsActive == 1) {
                    magicString = "";
                }
                magicStringsActive--;
                this.interrupt();
            }
        }.start();
    }

    private void gameLoop() {
        
        new Thread() {
            @Override
            public void run() {
                while (!gameover) {
                    //long timeStart = System.nanoTime();
                    if (counter >= (isTouchingGround() ? lockDelay.getWorkingValue() * 1000
                            : (Math.pow(gravity.getWorkingValue(), -1) * 1000))) {
                        if (!movePieceRelative(0, +1)) {
                            placePiece();
                        }
                    }

                    counter += 10;

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    //long timeEnd = System.nanoTime();

                    //long timeElapsed = timeEnd - timeStart;
                    //System.out.println((float)timeElapsed/1000000 + " ms");
                }

                if (player != null) {
                    Room room = Main.inwhichroom.get(player);

                    if (room != null) {
                        if (Main.roommap.containsKey(room.id)) {
                            room.playersalive--;
                            if (room.playersalive < 2) {
                                room.stopRoom();
                            }
                        }
                    }
                }
                this.interrupt();
            }
        }.start();

        gravity.start();
        garbage.start();
        garbageMultiplier.start();
        lockDelay.start();
    }

    private boolean isTouchingGround() {
        if (collides(currentPiecePosition.x, currentPiecePosition.y + 1, currentPieceRotation)) {
            return true;
        }
        return false;
    }
}
