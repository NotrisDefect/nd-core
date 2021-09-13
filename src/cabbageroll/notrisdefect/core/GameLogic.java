package cabbageroll.notrisdefect.core;

import cabbageroll.notrisdefect.core.tables.GarbageTable;
import cabbageroll.notrisdefect.core.tables.KickTable;
import cabbageroll.notrisdefect.core.tables.PieceTable;
import cabbageroll.notrisdefect.core.tables.ScoreTable;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

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

    public static final byte PIECE_Z = 0;
    public static final byte PIECE_L = 1;
    public static final byte PIECE_O = 2;
    public static final byte PIECE_S = 3;
    public static final byte PIECE_I = 4;
    public static final byte PIECE_J = 5;
    public static final byte PIECE_T = 6;
    public static final byte PIECE_NONE = 7;
    public static final byte PIECE_GARBAGE = 8;
    public static final byte PIECE_ZONE = 16;
    public static final byte SPIN_NONE = 0;
    public static final byte SPIN_MINI = 1;
    public static final byte SPIN_FULL = 2;

    private static final byte[][] STATES = {
        {-1, 0, 8, 7},
        {1, -1, 2, 10},
        {9, 3, -1, 4},
        {6, 11, 5, -1},
    };

    public static int STAGESIZEX = 10;
    public static int STAGESIZEY = 40;
    public static int PLAYABLEROWS = 20;
    public static int NEXTPIECESMAX = 5;
    public static int TPS = 20;

    private final PieceTable pieceTable = PieceTable.srs;
    private final KickTable kickTable = KickTable.KICK_TABLE_SRS_GUIDELINE_180;
    private final GarbageTable garbageTable = GarbageTable.tetrio;
    private final ScoreTable scoreTable = ScoreTable.normal;

    private final Property garbage = new Property(4d, 60 * TPS, 0.05d, 8d);
    private final Property garbageMultiplier = new Property(1d, 30 * TPS, 0.1d, 8d);
    private final Property gravity = new Property(1d, 30 * TPS, 0.1d, 20d);
    private final Property lockDelay = new Property(2d, 30 * TPS, -0.02d, 0.5d);

    private final ArrayList garbageQueue = new ArrayList();
    private final int[][] stage = new int[STAGESIZEY][STAGESIZEX];
    public String magicString = "";
    private Piece[] nextPieces;
    private int nextPiecesLeft;
    private Piece current;
    private long ticksPassed;
    private int magicStringsActive = 0;
    private int garbageHole;
    private double counter;
    private int zoneLines;
    private boolean zone;
    private int heldPiece;
    private boolean held;
    private boolean dead;
    private int combo;
    private int backToBack;

    private long totalScore;
    private long totalLinesCleared;
    private long totalPiecesPlaced;
    private long totalGarbageReceived;

    private int currentPieceLowestPossiblePosition;
    private int spinState;

    private Random garbageRandomizer;
    private Random pieceRandomizer;

    private static char convIntToPieceName(int p) {
        switch (p) {
            case PIECE_Z:
                return 'Z';
            case PIECE_L:
                return 'L';
            case PIECE_O:
                return 'O';
            case PIECE_S:
                return 'S';
            case PIECE_I:
                return 'I';
            case PIECE_J:
                return 'J';
            case PIECE_T:
                return 'T';
            case PIECE_NONE:
                return '_';
            case PIECE_GARBAGE:
                return '#';
            case PIECE_ZONE:
                return 'W';
            default:
                return '?';
        }
    }

    private static void debug(String s) {
    }

    public abstract void evtGameover();

    public abstract void evtLineClear(int row, int[] content);

    public abstract void evtPerfectClear();

    public abstract void evtSendGarbage(int n);

    public abstract void evtSpin();

    // -external <do> <what> [how] [extra]

    public final void extAbortGame() {
        die();
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
        instantSoftDrop();
    }

    public final void extHoldPiece() {
        holdPiece();
    }

    public final void extMovePieceLeft() {
        movePieceRelative(-1, 0);
    }

    public final void extMovePieceLeftMax() {
        dasLeft();
    }

    public final void extMovePieceRight() {
        movePieceRelative(1, 0);
    }

    public final void extMovePieceRightMax() {
        dasRight();
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
        extStartGame(new Random().nextLong());
    }

    public final void extStartGame(double seed) {
        pieceRandomizer = new Random((long) seed);
        garbageRandomizer = new Random((long) seed);
        initGame();
    }

    public final void extStartZone() {
        startZone();
    }

    public final void extTick() {
        tick();
    }

    public Point[] genPiece(int piece, int rotation) {
        return pieceTable.getPiece(piece, rotation);
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

    public long getTicksPassed() {
        return ticksPassed;
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

    public boolean isDead() {
        return dead;
    }

    private void calcCurrentPieceLowestPossiblePosition() {
        int result = current.getY();
        while (!isColliding(current.getX(), result + 1, current.getRotation())) {
            result++;
        }
        currentPieceLowestPossiblePosition = result;
    }

    private void checkTSpin() {
        if (current.getPieceNumber() == PIECE_T) {
            boolean[] corners = {true, true, true, true};
            if (isInsideBounds(current.getX(), current.getY())) {
                if (stage[current.getY()][current.getX()] == PIECE_NONE) {
                    corners[0] = false;
                }
            }

            if (isInsideBounds(current.getX() + 2, current.getY())) {
                if (stage[current.getY()][current.getX() + 2] == PIECE_NONE) {
                    corners[1] = false;
                }
            }

            if (isInsideBounds(current.getX(), current.getY() + 2)) {
                if (stage[current.getY() + 2][current.getX()] == PIECE_NONE) {
                    corners[2] = false;
                }
            }

            if (isInsideBounds(current.getX() + 2, current.getY() + 2)) {
                if (stage[current.getY() + 2][current.getX() + 2] == PIECE_NONE) {
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
                spinState = SPIN_MINI;

                switch (current.getRotation()) {
                    case 0:
                        if (corners[0] && corners[1]) {
                            spinState = SPIN_FULL;
                        }
                        break;
                    case 1:
                        if (corners[1] && corners[3]) {
                            spinState = SPIN_FULL;
                        }
                        break;
                    case 2:
                        if (corners[3] && corners[2]) {
                            spinState = SPIN_FULL;
                        }
                        break;
                    case 3:
                        if (corners[2] && corners[0]) {
                            spinState = SPIN_FULL;
                        }
                        break;
                }
                evtSpin();
            }
        }
    }

    private void checkTopOut() {

        Point[] temp = pieceTable.getPiece(current.getPieceNumber(), current.getRotation());
        for (int i = 0; i < 4; i++) {
            Point point = temp[i];
            if (stage[point.y + current.getY()][point.x + current.getX()] != PIECE_NONE) {
                if (zone) {
                    stopZone();
                } else {
                    die();
                }
            }
        }
    }

    private void clearLine(int line) {

        evtLineClear(line, stage[line]);

        for (int i = line; i > 0; i--) {
            System.arraycopy(stage[i - 1], 0, stage[i], 0, STAGESIZEX);
        }

        for (int j = 0; j < STAGESIZEX; j++) {
            stage[0][j] = PIECE_NONE;
        }

        totalLinesCleared++;

    }

    private void clearLineZone(int line) {
        int gap = STAGESIZEY - (line + zoneLines);
        for (int i = line; i < line + gap; i++) {
            System.arraycopy(stage[i + 1], 0, stage[i], 0, STAGESIZEX);
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
                if (stage[i][j] == PIECE_NONE) {
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
                if (stage[i][j] == PIECE_NONE) {
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

    private void dasLeft() {
        int num = 0;
        int x = current.getX();
        while (!isColliding(x + num - 1, current.getY(), current.getRotation())) {
            num--;
        }
        movePieceRelative(num, 0);
    }

    private void dasRight() {
        int num = 0;
        int x = current.getX();
        while (!isColliding(x + num + 1, current.getY(), current.getRotation())) {
            num++;
        }
        movePieceRelative(num, 0);
    }

    private void die() {
        dead = true;
        evtGameover();
    }

    private void hardDropPiece() {
        int lines = 0;
        while (!isColliding(current.getX(), current.getY() + lines + 1, current.getRotation())) {
            lines++;
        }
        if (lines > 0) {
            movePieceRelative(0, +lines);
            totalScore += lines * 2L;
        }
        lockPiece();
    }

    private void holdPiece() {
        if (!held) {
            int temp;

            // if first hold
            if (heldPiece == PIECE_NONE) {
                heldPiece = current.getPieceNumber();
                makeNextPiece();
            } else {
                // swap
                temp = current.getPieceNumber();
                current = new Piece(heldPiece, 3, 17, 0);
                heldPiece = temp;
            }
            calcCurrentPieceLowestPossiblePosition();
            held = true;
            checkTopOut();
        }
    }

    private void initGame() {
        for (int i = 0; i < STAGESIZEY; i++) {
            for (int j = 0; j < STAGESIZEX; j++) {
                stage[i][j] = PIECE_NONE;
            }
        }
        dead = false;
        nextPieces = new Piece[14];
        nextPiecesLeft = 0;
        heldPiece = PIECE_NONE;
        held = false;
        totalScore = 0;
        combo = -1;
        backToBack = -1;
        counter = 0;

        totalLinesCleared = 0;
        totalPiecesPlaced = 0;
        totalGarbageReceived = 0;

        zone = false;
        zoneLines = 0;

        garbageQueue.clear();
        garbageHole = garbageRandomizer.nextInt(STAGESIZEX);

        magicString = "";
        makeNextPiece();

        ticksPassed = 0;

        gravity.start();
        garbage.start();
        garbageMultiplier.start();
        lockDelay.start();

    }

    private void instantSoftDrop() {
        int num = 0;
        int y = current.getY();
        while (!isColliding(current.getX(), y + num + 1, current.getRotation())) {
            num++;
        }
        movePieceRelative(0, num);
    }

    private boolean isColliding(int x, int y, int rotation) {
        Point[] temp;

        temp = pieceTable.getPiece(current.getPieceNumber(), rotation);
        for (int i = 0; i < 4; i++) {
            Point point = temp[i];
            if (isInsideBounds(x + point.x, y + point.y)) {
                if (stage[point.y + y][point.x + x] != PIECE_NONE) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean isInsideBounds(int x, int y) {
        return y >= 0 && STAGESIZEY > y && x >= 0 && STAGESIZEX > x;
    }

    private boolean isTouchingGround() {
        return isColliding(current.getX(), current.getY() + 1, current.getRotation());
    }

    private String linesToString(int lines) {
        switch (lines) {
            case 1:
                return "SINGLE";
            case 2:
                return "DOUBLE";
            case 3:
                return "TRIPLE";
            case 4:
                return "NOTRIS";
            default:
                return "";
        }
    }

    private void lockPiece() {
        Point[] temp;

        totalPiecesPlaced++;

        temp = pieceTable.getPiece(current.getPieceNumber(), current.getRotation());
        for (int i = 0; i < 4; i++) {
            Point p = temp[i];
            stage[current.getY() + p.y][current.getX() + p.x] = current.getPieceNumber();
        }

        // check for too high placement
        int fails = 0;
        temp = pieceTable.getPiece(current.getPieceNumber(), current.getRotation());
        for (int i = 0; i < 4; i++) {
            Point point = temp[i];
            if (current.getY() + point.y >= STAGESIZEY - PLAYABLEROWS) {
                break;
            } else {
                fails++;
            }
        }
        if (fails == 4) {
            die();
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
                    totalScore += 3500;
                    evtPerfectClear();
                }
                sendGarbage((int) (garbageTable.get(linesCleared, spinState, combo) * garbageMultiplier.getWorkingValue()));

                setMagicString((convIntToPieceName(current.getPieceNumber()) + " " + spinToString(spinState) + " " + linesToString(linesCleared)));

                totalScore += scoreTable.get(linesCleared, spinState);
            } else {
                combo = -1;
                tryToPutGarbage();
            }

            debug("tspin=" + spinState + ";combo=" + combo + ";linescleared=" + linesCleared);

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

        checkTopOut();
    }

    private boolean movePiece(int newX, int newY, int newR) {
        if (!isColliding(newX, newY, newR)) {
            counter = 0;
            totalScore += Math.max(0, newY - current.getY());
            current.setX(newX);
            current.setY(newY);
            current.setRotation(newR);
            spinState = SPIN_NONE;
            calcCurrentPieceLowestPossiblePosition();
            return true;
        }
        return false;
    }

    private boolean movePieceRelative(int xOffset, int yOffset) {
        return movePiece(current.getX() + xOffset, current.getY() + yOffset, current.getRotation());
    }

    private void primitiveGameLoop() {
        new Thread() {
            public void run() {
                while (!dead) {
                    if (counter >= (isTouchingGround() ? lockDelay.getWorkingValue() * 1000 : (Math.pow(gravity.getWorkingValue(), -1) * 1000))) {
                        if (!movePieceRelative(0, +1)) {
                            lockPiece();
                        }
                    }

                    counter += 10;

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                evtGameover();
            }
        }.start();

        gravity.start();
        garbage.start();
        garbageMultiplier.start();
        lockDelay.start();
    }

    private void putGarbageLine() {
        for (int i = 0; i < STAGESIZEY - 1; i++) {
            System.arraycopy(stage[i + 1], 0, stage[i], 0, STAGESIZEX);
        }
        for (int j = 0; j < STAGESIZEX; j++) {
            if (j == garbageHole) {
                stage[STAGESIZEY - 1][j] = PIECE_NONE;
            } else {
                stage[STAGESIZEY - 1][j] = 8;
            }
        }

        totalGarbageReceived++;
    }

    private void receiveGarbage(int n) {
        garbageQueue.add(new Integer(n));
    }

    private void rotatePiece(int d) {
        int oldRotation = current.getRotation();
        int newRotation = (current.getRotation() + d + 4) % 4;
        int piece = current.getPieceNumber();
        int state = STATES[oldRotation][newRotation];

        for (int tries = 0; tries < kickTable.maxTries(piece, state); tries++) {
            if (movePiece(current.getX() + kickTable.getX(piece, state, tries), current.getY() - kickTable.getY(piece, state, tries), newRotation)) {
                checkTSpin();
                return;
            }
        }

    }

    private void sendGarbage(int n) {
        int garbageRemaining = n;
        while (!garbageQueue.isEmpty() && garbageRemaining > 0) {
            garbageQueue.set(0, new Integer(((Integer) garbageQueue.get(0)).intValue() - 1));
            if (((Integer) garbageQueue.get(0)).intValue() == 0) {
                garbageQueue.remove(0);
                garbageHole = garbageRandomizer.nextInt(STAGESIZEX);
            }
            garbageRemaining--;
        }

        if (garbageRemaining > 0) {
            evtSendGarbage(n);
        }
    }

    private void shuffleArray(int[] ar) {
        for (int i = ar.length - 1; i > 0; i--) {
            int index = ((pieceRandomizer.nextInt() % (i + 1) + i + 1)) % (i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    private void spawnPiece() {
        current = new Piece(nextPieces[0].getPieceNumber(), 3, 17, 0);
        System.arraycopy(nextPieces, 1, nextPieces, 0, 13);
        nextPiecesLeft--;
        held = false;
        spinState = SPIN_NONE;
        calcCurrentPieceLowestPossiblePosition();
    }

    private String spinToString(int spin) {
        switch (spin) {
            case SPIN_MINI:
                return "SPIN MINI";
            case SPIN_FULL:
                return "SPIN";
            default:
                return "";
        }
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
                    stage[STAGESIZEY - 1 - i][j] = PIECE_NONE;
                }
            }
        }
        zoneLines = 0;
    }

    private void tick() {
        ticksPassed++;
        if (counter >= (isTouchingGround() ? lockDelay.getWorkingValue() * 1000 : (Math.pow(gravity.getWorkingValue(), -1) * 1000))) {
            if (!movePieceRelative(0, +1)) {
                lockPiece();
            }
        }
        if (ticksPassed % TPS == 0) {
            gravity.tick(ticksPassed);
            garbage.tick(ticksPassed);
            garbageMultiplier.tick(ticksPassed);
            lockDelay.tick(ticksPassed);
        }

        counter += 10;
    }

    private void tryToPutGarbage() {
        for (int h = 0; h < garbage.getWorkingValue(); h++) {
            if (!garbageQueue.isEmpty()) {
                putGarbageLine();

                garbageQueue.set(0, new Integer(((Integer) garbageQueue.get(0)).intValue() - 1));
                if (((Integer) garbageQueue.get(0)).intValue() == 0) {
                    garbageQueue.remove(0);
                    garbageHole = garbageRandomizer.nextInt(STAGESIZEX);
                }
            }
        }
    }

    private static class Property {
        private final double base;
        private final long delay;
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

        public long getDelay() {
            return delay;
        }

        public void tick(long n) {
            if (n > delay) {
                if (mode ? (getWorkingValue() < getLimit()) : (getLimit() < getWorkingValue())) {
                    workingValue += delta;
                } else {
                    workingValue = limit;
                }
            }
        }

        private long getIncreaseDelay() {
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
        }

    }
}
