package tetrcore;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

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

        ClearType(int score) {
            this.score = score;
            this.description = null;
        }

        ClearType(int score, String description) {
            this.score = score;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public int getScore() {
            return score;
        }

    }
    
    private class Piece {
        private int piece;
        private Point position;
        private int rotation;
        
        @SuppressWarnings("unused")
        public Piece(int piece, Point position) {
            this.piece = piece;
            this.position = position;
            this.rotation = 0;
        }
        
        @SuppressWarnings("unused")
        public Piece(int piece, Point position, int rotation) {
            this.piece = piece;
            this.position = position;
            this.rotation = rotation;
        }

        public int getPiece() {
            return piece;
        }

        public Point getPosition() {
            return position;
        }

        public int getRotation() {
            return rotation;
        }

        public void setPiece(int piece) {
            this.piece = piece;
        }

        public void setPosition(Point position) {
            this.position = position;
        }

        public void setRotation(int rotation) {
            this.rotation = rotation;
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

        private int getIncreaseDelay() {
            return delay;
        }

        private double getLimit() {
            return limit;
        }

        private double getWorkingValue() {
            return workingValue;
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

        private void tick() {
            workingValue += delta;
        }

    }

    public static int STAGESIZEX = 10;
    public static int STAGESIZEY = 40;
    public static int VISIBLEROWS = 24;
    public static int NEXTPIECESMAX = 5;
    
    private static final int MAXIMUMMOVES = 15;
    public static int getMaximummoves() {
        return MAXIMUMMOVES;
    }

    public static int getNEXTPIECESMAX() {
        return NEXTPIECESMAX;
    }
    
    public static int getSTAGESIZEX() {
        return STAGESIZEX;
    }
    
    public static int getSTAGESIZEY() {
        return STAGESIZEY;
    }

    public static int getVISIBLEROWS() {
        return VISIBLEROWS;
    }

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
    private int zonelines;

    private boolean zone;
    private Piece current;
    private int heldPiece = -1;
    private boolean held;
    private ArrayList<Integer> nextPieces = new ArrayList<Integer>();
    private boolean currentPieceHasSpun;
    private boolean tSpinMini;
    private int combo;
    private int backToBack;
    
    private long score;
    private long totalLinesCleared = 0;
    private long totalPiecesPlaced = 0;
    private long totalGarbageReceived = 0;

    private int[][] stage = new int[STAGESIZEY][STAGESIZEX];

    public boolean checkTSpin() {
        if (current.getPiece() == 6) {
            boolean corners[] = { true, true, true, true };
            if (!isOutOfBounds(current.getPosition().x, current.getPosition().y)) {
                if (stage[current.getPosition().y][current.getPosition().x] == 7) {
                    corners[0] = false;
                }
            }

            if (!isOutOfBounds(current.getPosition().x + 2, current.getPosition().y)) {
                if (stage[current.getPosition().y][current.getPosition().x + 2] == 7) {
                    corners[1] = false;
                }
            }

            if (!isOutOfBounds(current.getPosition().x, current.getPosition().y + 2)) {
                if (stage[current.getPosition().y + 2][current.getPosition().x] == 7) {
                    corners[2] = false;
                }
            }

            if (!isOutOfBounds(current.getPosition().x + 2, current.getPosition().y + 2)) {
                if (stage[current.getPosition().y + 2][current.getPosition().x + 2] == 7) {
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

                
                tSpinMini = true;

                switch (current.getRotation()) {
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

    public boolean collides(int x, int y, int rotation) {
        for (Point point : pieces[current.getPiece()][rotation]) {
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

    public int getBackToBack() {
        return backToBack;
    }

    public int getCombo() {
        return combo;
    }

    public double getCounter() {
        return counter;
    }

    public Piece getCurrentPiece() {
        return current;
    }

    public int getCurrentPieceInt() {
        return current.getPiece();
    }

    public Point getCurrentPiecePosition() {
        return current.getPosition();
    }

    public int getCurrentPieceRotation() {
        return current.getRotation();
    }

    public boolean getGameover() {
        return gameover;
    }

    public int getGarbageHole() {
        return garbageHole;
    }

    public ArrayList<Integer> getGarbageQueue() {
        return garbageQueue;
    }

    public int[][] getGarbagetable() {
        return garbagetable;
    }
    
    public int getHeldPiece() {
        return heldPiece;
    }

    public String getMagicString() {
        return magicString;
    }

    public int getMagicStringsActive() {
        return magicStringsActive;
    }

    public ArrayList<Integer> getNextPieces() {
        return nextPieces;
    }

    public Point[][][] getPieces() {
        return pieces;
    }

    public long getScore() {
        return score;
    }

    public int[][] getStage() {
        return stage;
    }

    public int getTimesMoved() {
        return timesMoved;
    }

    public long getTotalGarbageReceived() {
        return totalGarbageReceived;
    }

    public long getTotalLinesCleared() {
        return totalLinesCleared;
    }

    public long getTotalPiecesPlaced() {
        return totalPiecesPlaced;
    }

    public boolean getZone() {
        return zone;
    }

    public int getZonelines() {
        return zonelines;
    }

    public void hardDropPiece() {
        int lines = 0;
        while (!collides(current.getPosition().x, current.getPosition().y + lines + 1, current.getRotation())) {
            lines++;
        }
        if (lines > 0) {
            movePieceRelative(0, +lines);
            addScore(lines * 2);
        }
        placePiece();
    }

    public boolean holdPiece() {
        if (!held) {
            int temp;

            // if first hold
            if (heldPiece == -1) {
                heldPiece = current.getPiece();
                makeNextPiece();
            } else {
                // swap
                temp = current.getPiece();
                setCurrentPiece(heldPiece);
                heldPiece = temp;

                current.setPosition(new Point(3, 17));
                current.setRotation(0);

                topOutCheck();
            }

            held = true;
            return true;
        } else {
            return false;
        }
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
        setBackToBack(-1);

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

    public boolean isHeld() {
        return held;
    }

    public boolean istSpin() {
        return currentPieceHasSpun;
    }

    public boolean istSpinMini() {
        return tSpinMini;
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

    public boolean movePiece(int newX, int newY, int newR) {
        if (!collides(newX, newY, newR)) {
            setCounter(0);
            addScore(Math.max(0, newY - current.getPosition().y));
            current.setPosition(new Point(newX, newY));
            current.setRotation(newR);
            settSpin(false);
            return true;
        }
        return false;
    }

    public boolean movePieceRelative(int xOffset, int yOffset) {
        return movePiece(current.getPosition().x + xOffset, current.getPosition().y + yOffset, current.getRotation());
    }

    public void placePiece() {

        totalPiecesPlaced++;

        for (Point point : pieces[current.getPiece()][current.getRotation()]) {
            stage[current.getPosition().y + point.y][current.getPosition().x + point.x] = current.getPiece();
        }

        if (zone) {
            clearLinesZone();
        } else {
            int linesCleared = clearLines();

            if (linesCleared > 0) {
                combo++;
                
                if ((totalLinesCleared - totalGarbageReceived) * STAGESIZEX + totalGarbageReceived == totalPiecesPlaced
                        * 4) {
                    
                    sendGarbage((int) ((garbagetable[clearTypeToInt(getClearType(linesCleared))][combo] + 10)
                            * garbageMultiplier.getWorkingValue()));
                } else {
                    sendGarbage((int) (garbagetable[clearTypeToInt(getClearType(linesCleared))][combo]
                            * garbageMultiplier.getWorkingValue()));
                }
                
                setMagicString(
                        (intToPieceName(current.getPiece()) + "-" + getClearType(linesCleared).getDescription()).toUpperCase());
            
                addScore(getClearType(linesCleared).getScore());
            } else {
                combo = -1;
                tryToPutGarbage();
            }

            debug("tspin=" + currentPieceHasSpun + ";combo=" + combo + ";linescleared=" + linesCleared);

        }

        makeNextPiece();
    }

    public void receiveGarbage(int n) {
        garbageQueue.add(n);
    }

    public boolean rotatePiece(int d) {
        int newRotation = (current.getRotation() + d + 4) % 4;

        int special = -1;

        if (current.getRotation() == 0 && newRotation == 1) {
            special = 0;
        } else if (current.getRotation() == 1 && newRotation == 0) {
            special = 1;
        } else if (current.getRotation() == 1 && newRotation == 2) {
            special = 2;
        } else if (current.getRotation() == 2 && newRotation == 1) {
            special = 3;
        } else if (current.getRotation() == 2 && newRotation == 3) {
            special = 4;
        } else if (current.getRotation() == 3 && newRotation == 2) {
            special = 5;
        } else if (current.getRotation() == 3 && newRotation == 0) {
            special = 6;
        } else if (current.getRotation() == 0 && newRotation == 3) {
            special = 7;
        } else if (current.getRotation() == 0 && newRotation == 2) {
            special = 8;
        } else if (current.getRotation() == 2 && newRotation == 0) {
            special = 9;
        } else if (current.getRotation() == 1 && newRotation == 3) {
            special = 10;
        } else if (current.getRotation() == 3 && newRotation == 1) {
            special = 11;
        }

        int pieceType = current.getPiece() == 4 ? 1 : 0;

        int maxtries = kicktable[pieceType][special].length;

        for (int tries = 0; tries < maxtries; tries++) {
            if (movePiece(current.getPosition().x + kicktable[pieceType][special][tries].x,
                    current.getPosition().y - kicktable[pieceType][special][tries].y, newRotation)) {
                return true;
            }
        }

        return false;
    }

    public void setCombo(int combo) {
        this.combo = combo;
    }

    public void setCurrentPieceRotation(int rotation) {
        current.setRotation(rotation);
    }

    public void setGameover(boolean gameover) {
        this.gameover = gameover;
    }

    public void setGarbageHole(int garbageHole) {
        this.garbageHole = garbageHole;
    }

    public void setHeld(boolean held) {
        this.held = held;
    }
    
    public void setHeldPiece(int heldPiece) {
        this.heldPiece = heldPiece;
    }

    public void setNextPieces(ArrayList<Integer> nextPieces) {
        this.nextPieces = nextPieces;
    }

    public void setStage(int[][] stage) {
        this.stage = stage;
    }

    public void setTimesMoved(int timesMoved) {
        this.timesMoved = timesMoved;
    }

    public void setTotalLinesCleared(long totalLinesCleared) {
        this.totalLinesCleared = totalLinesCleared;
    }

    public void setTotalPiecesPlaced(long totalPiecesPlaced) {
        this.totalPiecesPlaced = totalPiecesPlaced;
    }

    public void settSpin(boolean tSpin) {
        this.currentPieceHasSpun = tSpin;
    }

    public void settSpinMini(boolean tSpinMini) {
        this.tSpinMini = tSpinMini;
    }

    public void setZone(boolean zone) {
        this.zone = zone;
    }

    public void setZonelines(int zonelines) {
        this.zonelines = zonelines;
    }

    public void spawnPiece() {
        current.setPosition(new Point(3, 17));
        current.setRotation(0);
        current.setPiece(nextPieces.get(0));
        nextPieces.remove(0);
        held = false;
        settSpin(false);
    }

    public void startZone() {
        zone = true;
        magicString = "Zone activated";
    }

    public boolean topOutCheck() {
        for (Point point : pieces[current.getPiece()][current.getRotation()]) {
            if (stage[point.y + current.getPosition().y][point.x + current.getPosition().x] != 7) {
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

    private void addScore(int n) {
        score += n;
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

    private void debug(String s) {
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
                    
                    setCounter(getCounter() + 10);

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
            }
        }.start();

        gravity.start();
        garbage.start();
        garbageMultiplier.start();
        lockDelay.start();
    }

    private ClearType getClearType(int l) {
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

    private boolean isOutOfBounds(int x, int y) {
        if (y < 0 || STAGESIZEY <= y || x < 0 || STAGESIZEX <= x) {
            return true;
        }
        return false;
    }

    private boolean isTouchingGround() {
        if (collides(current.getPosition().x, current.getPosition().y + 1, current.getRotation())) {
            return true;
        }
        return false;
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
            // TODO
        }
    }

    private void setBackToBack(int b2b) {
        this.backToBack = b2b;
    }

    private void setCounter(double counter) {
        this.counter = counter;
    }

    private void setCurrentPiece(int piece) {
        current.setPiece(piece);
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

    private void setScore(long l) {
        score = l;
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
}
