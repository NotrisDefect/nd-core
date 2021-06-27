package tetrcore;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public abstract class GameLogic {
    /*
    abcd abcd
    abc1 xabc
    ab1d xabd
    ab11 xxab
    a1cd xacd
    a1c1 xxac
    a11d xxad
    a111 xxxa
    1bcd xbcd
    1bc1 xxbc
    1b1d xxbd
    1b11 xxxb
    11cd xxcd
    11c1 xxxc
    111d xxxd
    1111 xxxx
    */

    // private static final int MAXIMUMMOVES = 15;
    public static int STAGESIZEX = 10;
    public static int STAGESIZEY = 40;
    public static int PLAYABLEROWS = 20;
    public static int NEXTPIECESMAX = 5;
    private final PieceSet pieceSet = PieceSet.srs;
    private final Kicktable kicktable = Kicktable.kicktable_srs_guideline_180;
    private final Garbagetable garbagetable = Garbagetable.tetrio;
    private final ArrayList garbageQueue = new ArrayList();
    private final Property garbage = new Property(4d, 60, 0.05d, 8d);
    private final Property garbageMultiplier = new Property(1d, 30, 0.1d, 8d);
    private final Property gravity = new Property(1d, 30, 0.1d, 20d);
    private final Property lockDelay = new Property(2d, 30, -0.02d, 0.5d);
    private final int[][] stage = new int[STAGESIZEY][STAGESIZEX];
    public String magicString = "";
    private Piece[] nextPieces = new Piece[14];
    private int nextPiecesLeft;
    private Piece current;
    private int magicStringsActive = 0;
    private int garbageHole;
    private double counter = 0;
    private int zoneLines;
    private boolean zone;
    private int heldPiece = -1;
    private boolean held;
    // private int timesMoved = 0;
    private boolean gameover;
    private int combo;
    private int backToBack;
    private long totalScore;
    private long totalLinesCleared;
    private long totalPiecesPlaced;
    private long totalGarbageReceived;
    // cache
    private int currentPieceLowestPossiblePosition;
    private boolean currentPieceHasSpun;
    private boolean currentPieceHasSpunMini;
    private Random garbageRandomizer;
    private Random pieceRandomizer;

    private static void debug(String s) {
    }

    private static String intToPieceName(int p) {
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
                return "x";
        }
    }

    private static boolean isInsideBounds(int x, int y) {
        return y >= 0 && STAGESIZEY > y && x >= 0 && STAGESIZEX > x;
    }

    private static void shuffleArray(int[] ar) {
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = ((rnd.nextInt() % (i + 1) + i + 1)) % (i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    // -external <do> <what> [how] [extra]

    public final void extAbortGame() {
        gameover();
    }

    public final void extAddGarbage(int n) {
        receiveGarbage(n);
    }

    public final void extDropPieceHard() {
        hardDropPiece();
    }

    public final void extDropPieceSoft() {
        movePieceRelative(0, 1);
    }

    public final void extDropPieceSoftMax() {
        sonicDrop();
    }

    public final void extHoldPiece() {
        holdPiece();
    }

    public final void extMovePieceLeft() {
        movePieceRelative(-1, 0);
    }

    public final void extMovePieceLeftMax() {

    }

    public final void extMovePieceRight() {
        movePieceRelative(1, 0);
    }

    public final void extMovePieceRightMax() {

    }

    public final void extRotatePiece180() {
        rotatePiece(2);
    }

    public final void extRotatePieceCCW() {
        rotatePiece(-1);
    }

    public final void extRotatePieceCW() {
        rotatePiece(1);
    }

    public final void extStartGame() {
        long seed = new Random().nextLong();
        pieceRandomizer = new Random(seed);
        garbageRandomizer = new Random(seed);
        initGame();
    }

    public final void extStartGame(long seed) {
        pieceRandomizer = new Random(seed);
        garbageRandomizer = new Random(seed);
        initGame();
    }

    public final void extStartZone() {
        startZone();
    }

    public Point[] genPiece(int piece, int rotation) {
        return pieceSet.getPiece(piece, rotation);
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

    public int getCurrentPieceLowestPossiblePosition() {
        return currentPieceLowestPossiblePosition;
    }

    public boolean getGameover() {
        return gameover;
    }

    public int getGarbageHole() {
        return garbageHole;
    }

    public ArrayList getGarbageQueue() {
        return garbageQueue;
    }

    public int getHeldPiece() {
        return heldPiece;
    }

    public String getMagicString() {
        return magicString;
    }

    private void setMagicString(final String s) {
        new Thread() {
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

    public int getMagicStringsActive() {
        return magicStringsActive;
    }

    public Piece[] getNextPieces() {
        return nextPieces;
    }

    public long getScore() {
        return totalScore;
    }

    public int[][] getStage() {
        return stage;
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

    public int getZonelines() {
        return zoneLines;
    }

    public abstract void whatWhenGameover();

    public abstract void whatWhenLineClear(int row, int[] content);

    public abstract void whatWhenPerfectClear();

    public abstract void whatWhenSendGarbage(int n);

    public abstract void whatWhenSpin();

    private void calculateCurrentPieceLowestPossiblePosition() {
        int result = current.getY();
        while (!collides(current.getX(), result + 1, current.getRotation())) {
            result++;
        }
        currentPieceLowestPossiblePosition = result;
    }

    private void checkTSpin() {
        if (current.getPieceNumber() == 6) {
            boolean[] corners = {true, true, true, true};
            if (isInsideBounds(current.getX(), current.getY())) {
                if (stage[current.getY()][current.getX()] == 7) {
                    corners[0] = false;
                }
            }

            if (isInsideBounds(current.getX() + 2, current.getY())) {
                if (stage[current.getY()][current.getX() + 2] == 7) {
                    corners[1] = false;
                }
            }

            if (isInsideBounds(current.getX(), current.getY() + 2)) {
                if (stage[current.getY() + 2][current.getX()] == 7) {
                    corners[2] = false;
                }
            }

            if (isInsideBounds(current.getX() + 2, current.getY() + 2)) {
                if (stage[current.getY() + 2][current.getX() + 2] == 7) {
                    corners[3] = false;
                }
            }

            int cornersFilled = 0;
            for (int i = 0; i < 4; i++) {
                if (corners[i]) {
                    cornersFilled++;
                }
            }

            if (cornersFilled >= 3) {

                currentPieceHasSpunMini = true;

                switch (current.getRotation()) {
                    case 0:
                        if (corners[0] && corners[1]) {
                            currentPieceHasSpunMini = false;
                        }
                        break;
                    case 1:
                        if (corners[1] && corners[3]) {
                            currentPieceHasSpunMini = false;
                        }
                        break;
                    case 2:
                        if (corners[3] && corners[2]) {
                            currentPieceHasSpunMini = false;
                        }
                        break;
                    case 3:
                        if (corners[2] && corners[0]) {
                            currentPieceHasSpunMini = false;
                        }
                        break;
                }
                currentPieceHasSpun = true;
                whatWhenSpin();
            }
        }
    }

    private void clearLine(int line) {

        whatWhenLineClear(line, (int[]) stage[line].clone());

        for (int i = line; i > 0; i--) {
            if (STAGESIZEX >= 0) System.arraycopy(stage[i - 1], 0, stage[i], 0, STAGESIZEX);
        }

        for (int j = 0; j < STAGESIZEX; j++) {
            stage[0][j] = 7;
        }

        totalLinesCleared++;

    }

    private void clearLineZone(int line) {
        int gap = STAGESIZEY - (line + zoneLines);
        for (int i = line; i < line + gap; i++) {
            if (STAGESIZEX >= 0) System.arraycopy(stage[i + 1], 0, stage[i], 0, STAGESIZEX);
        }
        for (int j = 0; j < STAGESIZEX; j++) {
            stage[STAGESIZEY - zoneLines][j] = 16;
        }

        magicString = zoneLines + " LINES";
    }

    private int clearLines() {
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

    private void clearLinesZone() {

        boolean yes;
        for (int i = STAGESIZEY - 1 - zoneLines; i > 0; i--) {
            yes = true;
            for (int j = 0; j < STAGESIZEX; j++) {
                if (stage[i][j] == 7) {
                    yes = false;
                    break;
                }
            }
            if (yes) {
                zoneLines++;
                clearLineZone(i);
            }
        }
    }

    private int clearTypeToInt(ScoreType c) {
        return c.getId();
    }

    private boolean collides(int x, int y, int rotation) {
        Point[] temp;

        temp = pieceSet.getPiece(current.getPieceNumber(), rotation);
        for (int i = 0; i < 4; i++) {
            Point point = temp[i];
            if (isInsideBounds(x + point.x, y + point.y)) {
                if (stage[point.y + y][point.x + x] != 7) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private void gameLoop() {

        new Thread() {
            public void run() {
                while (!gameover) {
                    // long timeStart = System.nanoTime();
                    if (counter >= (isTouchingGround() ? lockDelay.getWorkingValue() * 1000
                        : (Math.pow(gravity.getWorkingValue(), -1) * 1000))) {
                        if (!movePieceRelative(0, +1)) {
                            lockPiece();
                        }
                    }

                    counter += 10;

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // long timeEnd = System.nanoTime();

                    // long timeElapsed = timeEnd - timeStart;
                    // System.out.println((float)timeElapsed/1000000 + " ms");
                }
                whatWhenGameover();
            }
        }.start();

        gravity.start();
        garbage.start();
        garbageMultiplier.start();
        lockDelay.start();
    }

    private void gameover() {
        gameover = true;
        whatWhenGameover();
    }

    private ScoreType getClearType(int lines) {
        switch (lines) {
            case 1:
                if (currentPieceHasSpun) {
                    if (currentPieceHasSpunMini) {
                        return ScoreType.SPIN_MINI_SINGLE;
                    } else {
                        return ScoreType.SPIN_SINGLE;
                    }
                } else {
                    return ScoreType.SINGLE;
                }
            case 2:
                if (currentPieceHasSpun) {
                    if (currentPieceHasSpunMini) {
                        return ScoreType.SPIN_MINI_DOUBLE;
                    } else {
                        return ScoreType.SPIN_DOUBLE;
                    }
                } else {
                    return ScoreType.DOUBLE;
                }
            case 3:
                if (currentPieceHasSpun) {
                    if (currentPieceHasSpunMini) {
                        return ScoreType.WTF;
                    } else {
                        return ScoreType.SPIN_TRIPLE;
                    }
                } else {
                    return ScoreType.TRIPLE;
                }
            case 4:
                if (currentPieceHasSpun) {
                    if (currentPieceHasSpunMini) {
                        return ScoreType.WTF;
                    } else {
                        return ScoreType.SPIN_LONG;
                    }
                } else {
                    return ScoreType.LONG;
                }
            default:
                return ScoreType.WTF;
        }
    }

    private void hardDropPiece() {
        int lines = 0;
        while (!collides(current.getX(), current.getY() + lines + 1, current.getRotation())) {
            lines++;
        }
        if (lines > 0) {
            movePieceRelative(0, +lines);
            totalScore += lines * 2L;
        }
        lockPiece();
    }

    private boolean holdPiece() {
        if (!held) {
            int temp;

            // if first hold
            if (heldPiece == -1) {
                heldPiece = current.getPieceNumber();
                makeNextPiece();
            } else {
                // swap
                temp = current.getPieceNumber();
                current = new Piece(heldPiece, 3, 17, 0);
                heldPiece = temp;
            }
            calculateCurrentPieceLowestPossiblePosition();
            held = true;
            topOutCheck();
            return true;
        } else {
            return false;
        }
    }

    private void initGame() {
        for (int i = 0; i < STAGESIZEY; i++) {
            for (int j = 0; j < STAGESIZEX; j++) {
                stage[i][j] = 7;
            }
        }
        gameover = false;
        nextPieces = new Piece[14];
        heldPiece = -1;
        held = false;
        totalScore = 0;
        combo = -1;
        backToBack = -1;

        totalLinesCleared = 0;
        totalPiecesPlaced = 0;
        totalGarbageReceived = 0;

        zone = false;
        zoneLines = 0;

        garbageQueue.clear();
        garbageHole = garbageRandomizer.nextInt(STAGESIZEX);

        magicString = "";
        makeNextPiece();
        gameLoop();
    }

    private boolean isTouchingGround() {
        return collides(current.getX(), current.getY() + 1, current.getRotation());
    }

    private void lockPiece() {
        Point[] temp;

        totalPiecesPlaced++;

        temp = pieceSet.getPiece(current.getPieceNumber(), current.getRotation());
        for (int i = 0; i < 4; i++) {
            Point p = temp[i];
            stage[current.getY() + p.y][current.getX() + p.x] = current.getPieceNumber();
        }

        // check for too high placement
        int fails = 0;
        temp = pieceSet.getPiece(current.getPieceNumber(), current.getRotation());
        for (int i = 0; i < 4; i++) {
            Point point = temp[i];
            if (current.getY() + point.y >= STAGESIZEY - PLAYABLEROWS) {
                break;
            } else {
                fails++;
            }
        }
        if (fails == 4) {
            gameover = true;
        }
        ///////////////////

        if (zone) {
            clearLinesZone();
        } else {
            int linesCleared = clearLines();

            if (linesCleared > 0) {
                combo++;

                if ((totalLinesCleared - totalGarbageReceived) * STAGESIZEX + totalGarbageReceived == totalPiecesPlaced * 4) {
                    sendGarbage(10);
                    whatWhenPerfectClear();
                }
                sendGarbage((int) (garbagetable.get(clearTypeToInt(getClearType(linesCleared)), combo) * garbageMultiplier.getWorkingValue()));


                setMagicString((intToPieceName(current.getPieceNumber()) + "-" + getClearType(linesCleared).getDescription()).toUpperCase());

                totalScore += getClearType(linesCleared).getScore();
            } else {
                combo = -1;
                tryToPutGarbage();
            }

            debug("tspin=" + currentPieceHasSpun + ";combo=" + combo + ";linescleared=" + linesCleared);

        }

        makeNextPiece();
    }

    private void makeNextPiece() {
        if (nextPiecesLeft <= 7) {
            int[] bag = new int[7];
            for (int i = 0; i < 7; i++) {
                bag[i] = i;
            }
            shuffleArray(bag);
            for (int i = 0; i < 7; i++) {
                nextPieces[nextPiecesLeft + i] = new Piece(bag[i]);
            }
            nextPiecesLeft += 7;
        }

        spawnPiece();

        topOutCheck();
    }

    private boolean movePiece(int newX, int newY, int newR) {
        if (!collides(newX, newY, newR)) {
            counter = 0;
            totalScore += Math.max(0, newY - current.getY());
            current.setX(newX);
            current.setY(newY);
            current.setRotation(newR);
            currentPieceHasSpun = false;
            calculateCurrentPieceLowestPossiblePosition();
            return true;
        }
        return false;
    }

    private boolean movePieceRelative(int xOffset, int yOffset) {
        return movePiece(current.getX() + xOffset, current.getY() + yOffset, current.getRotation());
    }

    private void putGarbageLine() {
        for (int i = 0; i < STAGESIZEY - 1; i++) {
            if (STAGESIZEX >= 0) System.arraycopy(stage[i + 1], 0, stage[i], 0, STAGESIZEX);
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

    private void receiveGarbage(int n) {
        garbageQueue.add(n);
    }

    private boolean rotatePiece(int d) {
        int oldRotation = current.getRotation();
        int newRotation = (current.getRotation() + d + 4) % 4;
        int piece = current.getPieceNumber();

        int state;

        switch (oldRotation) {
            case 0:
                switch (newRotation) {
                    case 1:
                        state = 0;
                        break;
                    case 2:
                        state = 8;
                        break;
                    case 3:
                        state = 7;
                        break;
                    default:
                        return false;
                }
                break;
            case 1:
                switch (newRotation) {
                    case 0:
                        state = 1;
                        break;
                    case 2:
                        state = 2;
                        break;
                    case 3:
                        state = 10;
                        break;
                    default:
                        return false;
                }
                break;
            case 2:
                switch (newRotation) {
                    case 0:
                        state = 9;
                        break;
                    case 1:
                        state = 3;
                        break;
                    case 3:
                        state = 4;
                        break;
                    default:
                        return false;
                }
                break;
            case 3:
                switch (newRotation) {
                    case 0:
                        state = 6;
                        break;
                    case 1:
                        state = 11;
                        break;
                    case 2:
                        state = 5;
                        break;
                    default:
                        return false;
                }
                break;
            default:
                return false;
        }

        for (int tries = 0; tries < kicktable.maxTries(piece, state); tries++) {
            if (movePiece(current.getX() + kicktable.getX(piece, state, tries), current.getY() - kicktable.getY(piece, state, tries), newRotation)) {
                checkTSpin();
                return true;
            }
        }

        return false;
    }

    private void sendGarbage(int n) {
        int garbageRemaining = n;
        while (!garbageQueue.isEmpty() && garbageRemaining > 0) {
            garbageQueue.set(0, ((Integer) garbageQueue.get(0)).intValue() - 1);
            if (((Integer) garbageQueue.get(0)).intValue() == 0) {
                garbageQueue.remove(0);
                garbageHole = garbageRandomizer.nextInt(STAGESIZEX);
            }
            garbageRemaining--;
        }

        if (garbageRemaining > 0) {
            whatWhenSendGarbage(n);
        }
    }

    private void sonicDrop() {
        int num = 0;
        int y = current.getY();
        while (!collides(current.getX(), y + num + 1, current.getRotation())) {
            num++;
        }
        movePieceRelative(0, num);
    }

    private void spawnPiece() {
        current = new Piece(nextPieces[0].getPieceNumber(), 3, 17, 0);
        for (int i = 0; i < 13; i++) {
            nextPieces[i] = nextPieces[i + 1];
        }
        nextPiecesLeft--;
        held = false;
        currentPieceHasSpun = false;
        calculateCurrentPieceLowestPossiblePosition();
    }

    private void startZone() {
        zone = true;
        magicString = "Zone activated";
    }

    private void stopZone() {
        zone = false;
        for (int i = 0; i < STAGESIZEY; i++) {
            for (int j = 0; j < STAGESIZEX; j++) {
                if (STAGESIZEY - zoneLines - 1 - i >= 0) {
                    stage[STAGESIZEY - 1 - i][j] = stage[STAGESIZEY - zoneLines - 1 - i][j];
                } else {
                    stage[STAGESIZEY - 1 - i][j] = 7;
                }
            }
        }
        zoneLines = 0;
    }

    private void topOutCheck() {

        Point[] temp = pieceSet.getPiece(current.getPieceNumber(), current.getRotation());
        for (int i = 0; i < 4; i++) {
            Point point = temp[i];
            if (stage[point.y + current.getY()][point.x + current.getX()] != 7) {
                if (zone) {
                    stopZone();
                } else {
                    gameover();
                }
            }
        }
    }

    private void tryToPutGarbage() {
        for (int h = 0; h < garbage.getWorkingValue(); h++) {
            if (!garbageQueue.isEmpty()) {
                putGarbageLine();

                garbageQueue.set(0, ((Integer) garbageQueue.get(0)).intValue() - 1);
                if (((Integer) garbageQueue.get(0)).intValue() == 0) {
                    garbageQueue.remove(0);
                    garbageHole = garbageRandomizer.nextInt(STAGESIZEX);
                }
            }
        }
    }

    private static class ScoreType {
        public static ScoreType SINGLE = new ScoreType(100, "Single");
        public static ScoreType DOUBLE = new ScoreType(300, "Double");
        public static ScoreType TRIPLE = new ScoreType(500, "Triple");
        public static ScoreType LONG = new ScoreType(800, "Long");
        public static ScoreType SPIN_MINI = new ScoreType(100, "Spin Mini");
        public static ScoreType SPIN = new ScoreType(400, "Spin");
        public static ScoreType SPIN_MINI_SINGLE = new ScoreType(200, "Spin Mini Single");
        public static ScoreType SPIN_SINGLE = new ScoreType(800, "Spin Single");
        public static ScoreType SPIN_MINI_DOUBLE = new ScoreType(400, "Spin Mini Double");
        public static ScoreType SPIN_DOUBLE = new ScoreType(1200, "Spin Double");
        public static ScoreType SPIN_TRIPLE = new ScoreType(1600, "Spin Triple");
        public static ScoreType SPIN_LONG = new ScoreType(2600, "Spin Long");
        public static ScoreType COMBO = new ScoreType(50, "Combo");
        public static ScoreType ALL_CLEAR = new ScoreType(3500, "All Clear");
        public static ScoreType SD = new ScoreType(1, "Soft Drop");
        public static ScoreType HD = new ScoreType(2, "Hard Drop");
        public static ScoreType WTF = new ScoreType(69420, "Unknown");
        private static int count = 0;
        private final int score;
        private final String description;
        private final int id;

        ScoreType(int score, String description) {
            this.id = count++;
            this.score = score;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public int getId() {
            return id;
        }

        public int getScore() {
            return score;
        }
    }

    private static class Property {
        private final double base;
        private final int delay;
        private final double delta;
        private final boolean mode;
        private final double limit;

        private double workingValue;

        private Property(double base, int delay, double delta, double limit) {
            this.base = base;
            this.delay = delay;
            this.delta = delta;
            mode = delta > 0;
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

                public void run() {
                    new Thread() {
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
                    }.start();
                }
            };

            timer.schedule(task, getIncreaseDelay() * 1000L);
        }

        private void tick() {
            workingValue += delta;
        }

    }
}
