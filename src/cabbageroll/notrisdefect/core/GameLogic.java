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

    public static final int TPS = 20;
    private static final byte[][] STATES = {
        {-1, 0, 8, 7},
        {1, -1, 2, 10},
        {9, 3, -1, 4},
        {6, 11, 5, -1},
    };
    private static final int BAG_SIZE = 7;
    private final PieceTable pieceTable = PieceTable.GUIDELINE;
    private final KickTable kickTable = KickTable.SRS_180;
    private final GarbageTable garbageTable = GarbageTable.TETRIO;
    private final ScoreTable scoreTable = ScoreTable.NORMAL;
    private final Property garbageCap = new Property(4d, 60, 0.05d, 8d);
    private final Property garbageMultiplier = new Property(1d, 30, 0.1d, 8d);
    private final Property gravity = new Property(1d, 30, 0.1d, 20d);
    private final Property lockDelay = new Property(2d, 30, -0.02d, 0.5d);
    private int STAGESIZEX = 10;
    private int STAGESIZEY = 40;
    private int PLAYABLEROWS = 20;
    private int NEXTPIECESMAX = 5;
    private Random garbageRandomizer;
    private Random pieceRandomizer;
    private boolean dead;
    private int[][] stage;
    private Piece current;
    private Piece[] nextPieces;
    private int nextPiecesLeft;
    private Piece heldPiece;
    private boolean held;
    private ArrayList garbageQueue;
    private int garbageHole;
    private double counter;
    private double limit;
    private int zoneLines;
    private boolean zone;
    private int combo;
    private int backToBack;
    private long ticksPassed;
    private long totalScore;
    private long totalLinesCleared;
    private long totalPiecesPlaced;
    private long totalGarbageReceived;
    private long totalGarbageSent;
    private int lowestPossiblePosition;
    private int spinState;

    public abstract void evtGameover();

    public abstract void evtLineClear(int row, int[] content);

    public abstract void evtLockPiece(Piece piece, int linesCleared, int spinState, int combo, int backToBack);

    public abstract void evtPerfectClear();

    public abstract void evtSendGarbage(int n);

    public abstract void evtSpin();

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
        totalScore += Math.max(0, scoreTable.getSoftDrop());
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

    // -external <do> <what> [how] [extra]

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

    public int getGarbageHole() {
        return garbageHole;
    }

    public ArrayList getGarbageQueue() {
        return garbageQueue;
    }

    public Piece getHeldPiece() {
        return heldPiece;
    }

    public double getLimit() {
        return limit;
    }

    public int getLowestPossiblePosition() {
        return lowestPossiblePosition;
    }

    public int getNEXTPIECESMAX() {
        return NEXTPIECESMAX;
    }

    public void setNEXTPIECESMAX(int NEXTPIECESMAX) {
        this.NEXTPIECESMAX = NEXTPIECESMAX;
    }

    public Piece[] getNextPieces() {
        return nextPieces;
    }

    public int getPLAYABLEROWS() {
        return PLAYABLEROWS;
    }

    public void setPLAYABLEROWS(int PLAYABLEROWS) {
        this.PLAYABLEROWS = PLAYABLEROWS;
    }

    public Point[] getPoints(int piece, int rotation) {
        return pieceTable.getPiece(piece, rotation);
    }

    public int getSTAGESIZEX() {
        return STAGESIZEX;
    }

    public void setSTAGESIZEX(int STAGESIZEX) {
        this.STAGESIZEX = STAGESIZEX;
    }

    public int getSTAGESIZEY() {
        return STAGESIZEY;
    }

    public void setSTAGESIZEY(int STAGESIZEY) {
        this.STAGESIZEY = STAGESIZEY;
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

    public long getTotalGarbageSent() {
        return totalGarbageSent;
    }

    public long getTotalLinesCleared() {
        return totalLinesCleared;
    }

    public long getTotalPiecesPlaced() {
        return totalPiecesPlaced;
    }

    public long getTotalScore() {
        return totalScore;
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
        lowestPossiblePosition = result;
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
            totalScore += lines * scoreTable.getHardDrop();
        }
        lockPiece();
    }

    private void holdPiece() {
        if (!held) {
            if (heldPiece == null) {
                heldPiece = new Piece(current);
                makeNextPiece();
            } else {
                Piece temp = new Piece(current);
                current = heldPiece;
                heldPiece = temp;
                calcCurrentPieceLowestPossiblePosition();
                checkTopOut();
            }
            held = true;
        }
    }

    private void initGame() {
        dead = false;
        stage = new int[STAGESIZEY][STAGESIZEX];
        for (int i = 0; i < STAGESIZEY; i++) {
            for (int j = 0; j < STAGESIZEX; j++) {
                stage[i][j] = PIECE_NONE;
            }
        }

        nextPieces = new Piece[NEXTPIECESMAX + BAG_SIZE];
        nextPiecesLeft = 0;

        heldPiece = null;
        held = false;

        garbageQueue = new ArrayList();
        garbageHole = garbageRandomizer.nextInt(STAGESIZEX);

        counter = 0;

        zoneLines = 0;
        zone = false;

        combo = -1;
        backToBack = -1;

        ticksPassed = 0;
        totalScore = 0;
        totalLinesCleared = 0;
        totalPiecesPlaced = 0;
        totalGarbageReceived = 0;
        totalGarbageSent = 0;

        gravity.reset();
        garbageCap.reset();
        garbageMultiplier.reset();
        lockDelay.reset();

        makeNextPiece();
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
                    totalScore += scoreTable.getAllClear();
                    evtPerfectClear();
                }
                sendGarbage((int) (garbageTable.get(linesCleared, spinState, combo) * garbageMultiplier.getWorkingValue()));

                if (linesCleared == 4 || spinState != SPIN_NONE) {
                    backToBack++;
                } else {
                    backToBack = -1;
                }

                totalScore += scoreTable.get(linesCleared, spinState) * scoreTable.getB2bMulti() * (backToBack > 0 ? 1 : 0) + combo * scoreTable.getCombo();

            } else {
                combo = -1;
                tryToPutGarbage();
            }

            evtLockPiece(current, linesCleared, spinState, combo, backToBack);

        }

        makeNextPiece();
    }

    private void makeNextPiece() {
        while (nextPiecesLeft <= NEXTPIECESMAX) {
            int[] bag = new int[BAG_SIZE];
            for (int i = 0; i < BAG_SIZE; i++) {
                bag[i] = i;
            }
            shuffleArray(bag);
            for (int i = 0; i < BAG_SIZE; i++) {
                nextPieces[nextPiecesLeft + i] = new Piece(bag[i]);
            }
            nextPiecesLeft += BAG_SIZE;
        }

        spawnPiece();

        checkTopOut();
    }

    private boolean movePiece(int newX, int newY, int newR) {
        if (!isColliding(newX, newY, newR)) {
            counter = 0;
            current.setX(newX);
            current.setY(newY);
            current.setRotation(newR);
            spinState = SPIN_NONE;
            calcCurrentPieceLowestPossiblePosition();
            limit = isTouchingGround() ? lockDelay.getWorkingValue() : (Math.pow(gravity.getWorkingValue(), -1));
            return true;
        }
        return false;
    }

    private boolean movePieceRelative(int xOffset, int yOffset) {
        return movePiece(current.getX() + xOffset, current.getY() + yOffset, current.getRotation());
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
            totalGarbageSent += n;
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
        current = nextPieces[0];
        System.arraycopy(nextPieces, 1, nextPieces, 0, nextPieces.length - 1);
        nextPiecesLeft--;
        held = false;
        spinState = SPIN_NONE;
        calcCurrentPieceLowestPossiblePosition();
    }

    private void startZone() {
        zone = true;
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

        counter += 1 / (double) TPS;

        if (counter >= limit) {
            if (!movePieceRelative(0, +1)) {
                lockPiece();
            }
        }

        if (ticksPassed % TPS == 0) {
            gravity.tick(ticksPassed);
            garbageCap.tick(ticksPassed);
            garbageMultiplier.tick(ticksPassed);
            lockDelay.tick(ticksPassed);
        }
    }

    private void tryToPutGarbage() {
        for (int h = 0; h < garbageCap.getWorkingValue(); h++) {
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
            if (n > delay * TPS) {
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

        private void reset() {
            workingValue = base;
        }

    }
}
