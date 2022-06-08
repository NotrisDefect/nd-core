package cabbageroll.notrisdefect.core;

import cabbageroll.notrisdefect.core.tables.GarbageTable;
import cabbageroll.notrisdefect.core.tables.KickTable;
import cabbageroll.notrisdefect.core.tables.MaskTable;
import cabbageroll.notrisdefect.core.tables.PieceTable;
import cabbageroll.notrisdefect.core.tables.ScoreTable;

import java.util.Random;
import java.util.Vector;

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

    public static final int PIECE_NONE = 0;
    public static final int PIECE_Z = 1;
    public static final int PIECE_L = 2;
    public static final int PIECE_O = 3;
    public static final int PIECE_S = 4;
    public static final int PIECE_I = 5;
    public static final int PIECE_J = 6;
    public static final int PIECE_T = 7;
    public static final int PIECE_GARBAGE = 8;
    public static final int PIECE_ZONE = 9;
    public static final int PIECE_NUKE = 10;

    public static final int SPIN_NONE = 0;
    public static final int SPIN_MINI = 1;
    public static final int SPIN_FULL = 2;

    public static final int STATE_DEAD = 0;
    public static final int STATE_ALIVE = 1;
    public static final int STATE_DELAY = 2;
    public static final int STATE_PAUSED = 3;

    public static final int TPS = 20;
    public static final int TICK_TIME = 50;
    public static final int BAGSIZE = 7;
    public static final int PIECEPOINTS = 4;
    private static final int[][] SPINSTATES = {
        {-1, 0, 8, 7},
        {1, -1, 2, 10},
        {9, 3, -1, 4},
        {6, 11, 5, -1},
    };
    private final PieceTable pieceTable = PieceTable.GUIDELINE;
    private final KickTable kickTable = KickTable.SRS_180;
    private final GarbageTable garbageTable = GarbageTable.TETRIO;
    private final ScoreTable scoreTable = ScoreTable.NORMAL;
    private final MaskTable maskTable = MaskTable.SRS;

    private final Property garbageCap = new Property(4d, 60 * TPS, 0.05d, 8d);
    private final Property garbageMultiplier = new Property(1d, 30 * TPS, 0.02d, 2d);
    private final Property gravity = new Property(1d, 30 * TPS, 0.1d, 20d) {
        public int getRealValue() {
            return millisToTicks((int) ((double) 1000 / getWorkingValue()));
        }
    };
    private final Property lockDelay = new Property(2000, 60 * TPS, -20, 500) {
        public int getRealValue() {
            return millisToTicks((int) getWorkingValue());
        }
    };
    private int STAGESIZEX = 10;
    private int STAGESIZEY = 40;
    private int PLAYABLEROWS = 20;
    private int NEXTPIECES = 5;
    private int DEFAULTSPAWNX = 3;
    private int DEFAULTSPAWNY = 18;
    private int DEFAULTSPAWNROTATION = 0;
    private int PIECESPAWNDELAY = 100;
    private int LINECLEARDELAY = 500;
    private int DAS;
    private int ARR;
    private int SDF;
    private boolean ENABLENUKES = false;
    private boolean ENABLEALLSPIN = false;
    private boolean ENABLEALWAYSGARBAGE = false;
    // permanent once a round starts
    private Random garbageRandomizer;
    private Random pieceRandomizer;
    // gameplay related stuff
    private int[][] stage;
    private Piece currentPiece;
    private Piece[] nextPieces;
    private int nextPiecesLeft;
    private Piece heldPiece;
    private boolean held;
    private Vector garbageQueue;
    private int garbageHole;
    private int combo;
    // helper variables
    private int counter;
    private int counterEnd;
    private int gameState;
    private int spinState;
    private int lowestPossiblePosition;
    // incremental
    private long ticksPassed;
    private long totalScore;
    private long totalLinesCleared;
    private long totalPiecesPlaced;
    private long totalGarbageReceived;
    private long totalGarbageSent;
    // not fully implemented
    private int zoneLines;
    private boolean zone;
    private int backToBack;
    // keys
    private boolean leftKey;
    private boolean rightKey;
    private boolean downKey;
    private boolean enqLeftKey;
    private boolean enqRightKey;
    private boolean enqDownKey;
    private boolean deqLeftKey;
    private boolean deqRightKey;
    private boolean deqDownKey;
    private int leftKeyTime;
    private int rightKeyTime;
    private int downKeyTime;
    private boolean checkLeftKey;
    private boolean checkRightKey;
    private boolean checkDownKey;
    private boolean scheduleHardDrop;
    private boolean scheduleRotateCCW;
    private boolean scheduleRotateCW;
    private boolean scheduleRotate180;
    private boolean scheduleHold;
    protected GameLogic(int ARR, int DAS, int SDF) {
        this.ARR = ARR;
        this.DAS = DAS;
        this.SDF = SDF;
    }

    public void doAbort() {
        if (gameState == STATE_DEAD) {
            throw new IllegalStateException("Aborted while dead");
        }

        die();
    }

    public void doAddGarbage(int n) {
        if (gameState == STATE_DEAD) {
            throw new IllegalStateException("Added garbage while dead");
        }

        receiveGarbage(n);
    }

    public void doHardDrop() {
        if (checkPausedOrInvalid()) {
            return;
        }

        scheduleHardDrop = true;
    }

    public void doHold() {
        if (checkPausedOrInvalid()) {
            return;
        }

        scheduleHold = true;
    }

    public void doPause() {
        if (gameState == STATE_DEAD) {
            throw new IllegalStateException("Paused while dead");
        }

        togglePause();
    }

    public void doPressDown() {
        if (checkPausedOrInvalid()) {
            return;
        }

        enqDownKey = true;
    }

    public void doPressLeft() {
        if (checkPausedOrInvalid()) {
            return;
        }

        enqLeftKey = true;
    }

    public void doPressRight() {
        if (checkPausedOrInvalid()) {
            return;
        }

        enqRightKey = true;
    }

    public void doReleaseDown() {
        if (checkPausedOrInvalid()) {
            return;
        }

        deqDownKey = true;
    }

    public void doReleaseLeft() {
        if (checkPausedOrInvalid()) {
            return;
        }

        deqLeftKey = true;
    }

    public void doReleaseRight() {
        if (checkPausedOrInvalid()) {
            return;
        }

        deqRightKey = true;
    }

    public void doRotate180() {
        if (checkPausedOrInvalid()) {
            return;
        }

        scheduleRotate180 = true;
    }

    public void doRotateCCW() {
        if (checkPausedOrInvalid()) {
            return;
        }

        scheduleRotateCCW = true;
    }

    public void doRotateCW() {
        if (checkPausedOrInvalid()) {
            return;
        }

        scheduleRotateCW = true;
    }

    public void doStart() {
        doStart(new Random().nextLong());
    }

    public void doStart(double seed) {
        if (gameState != STATE_DEAD) {
            throw new IllegalStateException("Started while already running");
        }

        if (ARR < 0 || DAS < 0 || SDF < 0) {
            throw new IllegalStateException("ARR, DAS and SDF must be positive");
        }

        pieceRandomizer = new Random((long) seed);
        garbageRandomizer = new Random((long) seed);
        initGame();
    }

    public void doTick() {
        if (gameState == STATE_DEAD) {
            throw new IllegalStateException("Ticked while dead");
        }

        leftKey = enqLeftKey;
        rightKey = enqRightKey;
        downKey = enqDownKey;

        tick();
    }

    public void doZone() {
        if (checkPausedOrInvalid()) {
            return;
        }

        startZone();
    }

    public int getARR() {
        return ARR;
    }

    public void setARR(int ARR) {
        this.ARR = ARR;
    }

    public int getBackToBack() {
        return backToBack;
    }

    public int getCombo() {
        return combo;
    }

    public int getCounter() {
        return counter;
    }

    public int getCounterEnd() {
        return counterEnd;
    }

    public Piece getCurrentPiece() {
        return currentPiece;
    }

    public int getDAS() {
        return DAS;
    }

    public void setDAS(int DAS) {
        this.DAS = DAS;
    }

    public int getDEFAULTSPAWNROTATION() {
        return DEFAULTSPAWNROTATION;
    }

    public void setDEFAULTSPAWNROTATION(int DEFAULTSPAWNROTATION) {
        this.DEFAULTSPAWNROTATION = DEFAULTSPAWNROTATION;
    }

    public int getDEFAULTSPAWNX() {
        return DEFAULTSPAWNX;
    }

    public void setDEFAULTSPAWNX(int DEFAULTSPAWNX) {
        this.DEFAULTSPAWNX = DEFAULTSPAWNX;
    }

    public int getDEFAULTSPAWNY() {
        return DEFAULTSPAWNY;
    }

    public void setDEFAULTSPAWNY(int DEFAULTSPAWNY) {
        this.DEFAULTSPAWNY = DEFAULTSPAWNY;
    }

    public int getGameState() {
        return gameState;
    }

    public Property getGarbageCap() {
        return garbageCap;
    }

    public int getGarbageHole() {
        return garbageHole;
    }

    public Property getGarbageMultiplier() {
        return garbageMultiplier;
    }

    public Vector getGarbageQueue() {
        return garbageQueue;
    }

    public Random getGarbageRandomizer() {
        return garbageRandomizer;
    }

    public GarbageTable getGarbageTable() {
        return garbageTable;
    }

    public Property getGravity() {
        return gravity;
    }

    public boolean getHeld() {
        return held;
    }

    public Piece getHeldPiece() {
        return heldPiece;
    }

    public KickTable getKickTable() {
        return kickTable;
    }

    public int getLINECLEARDELAY() {
        return LINECLEARDELAY;
    }

    public void setLINECLEARDELAY(int LINECLEARDELAY) {
        this.LINECLEARDELAY = LINECLEARDELAY;
    }

    public Property getLockDelay() {
        return lockDelay;
    }

    public int getLowestPossiblePosition() {
        return lowestPossiblePosition;
    }

    public MaskTable getMaskTable() {
        return maskTable;
    }

    public int getNEXTPIECES() {
        return NEXTPIECES;
    }

    public void setNEXTPIECES(int NEXTPIECES) {
        this.NEXTPIECES = NEXTPIECES;
    }

    public Piece[] getNextPieces() {
        return nextPieces;
    }

    public int getNextPiecesLeft() {
        return nextPiecesLeft;
    }

    public int getPIECESPAWNDELAY() {
        return PIECESPAWNDELAY;
    }

    public void setPIECESPAWNDELAY(int PIECESPAWNDELAY) {
        this.PIECESPAWNDELAY = PIECESPAWNDELAY;
    }

    public int getPLAYABLEROWS() {
        return PLAYABLEROWS;
    }

    public void setPLAYABLEROWS(int PLAYABLEROWS) {
        this.PLAYABLEROWS = PLAYABLEROWS;
    }

    public Random getPieceRandomizer() {
        return pieceRandomizer;
    }

    public PieceTable getPieceTable() {
        return pieceTable;
    }

    public Point[] getPoints(int piece, int rotation) {
        return pieceTable.getPiece(piece - 1, rotation);
    }

    public int getSDF() {
        return SDF;
    }

    public void setSDF(int SDF) {
        this.SDF = SDF;
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

    public ScoreTable getScoreTable() {
        return scoreTable;
    }

    public int getSpinState() {
        return spinState;
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

    public boolean getZone() {
        return zone;
    }

    public int getZoneLines() {
        return zoneLines;
    }

    public int getZonelines() {
        return zoneLines;
    }

    public boolean isENABLEALLSPIN() {
        return ENABLEALLSPIN;
    }

    public void setENABLEALLSPIN(boolean ENABLEALLSPIN) {
        this.ENABLEALLSPIN = ENABLEALLSPIN;
    }

    public boolean isENABLEALWAYSGARBAGE() {
        return ENABLEALWAYSGARBAGE;
    }

    public void setENABLEALWAYSGARBAGE(boolean ENABLEALWAYSGARBAGE) {
        this.ENABLEALWAYSGARBAGE = ENABLEALWAYSGARBAGE;
    }

    public boolean isENABLENUKES() {
        return ENABLENUKES;
    }

    public void setENABLENUKES(boolean ENABLENUKES) {
        this.ENABLENUKES = ENABLENUKES;
    }

    protected abstract void evtGameover();

    protected abstract void evtLineClear(int row, int[] content);

    protected abstract void evtLockPiece(Piece piece, int linesCleared, int spinState, int combo, int backToBack, boolean nuke);

    protected abstract void evtPerfectClear();

    protected abstract void evtSendGarbage(int n);

    protected abstract void evtSpin();

    private void calcCounterEnd() {
        counterEnd = isTouchingGround() ? lockDelay.getRealValue() : (gravity.getRealValue() / (downKey ? SDF : 1));
    }

    private void calcCurrentPieceLowestPossiblePosition() {
        int result = currentPiece.getY();
        while (!isColliding(currentPiece.getX(), result + 1, currentPiece.getRotation())) {
            result++;
        }
        lowestPossiblePosition = result;
    }

    private void checkBlockOut() {
        Point[] piece = getPoints(currentPiece.getColor(), currentPiece.getRotation());
        for (int i = 0; i < PIECEPOINTS; i++) {
            Point point = piece[i];
            if (stage[point.y + currentPiece.getY()][point.x + currentPiece.getX()] != PIECE_NONE) {
                tryToDie();
            }
        }
    }

    private void checkLockOut() {
        Point[] piece = getPoints(currentPiece.getColor(), currentPiece.getRotation());
        for (int i = 0; i < PIECEPOINTS; i++) {
            Point point = piece[i];
            if (currentPiece.getY() + point.y >= STAGESIZEY - PLAYABLEROWS) {
                return;
            }
        }
        tryToDie();
    }

    private boolean checkPausedOrInvalid() {
        if (gameState == STATE_DEAD || gameState >= STATE_PAUSED) {
            throw new IllegalStateException("Moved while dead or paused (" + gameState + ")");
        }

        return gameState == STATE_DELAY;
    }

    private void checkSpin(int tries, int oldRotation, int newRotation) {
        int x = currentPiece.getX();
        int y = currentPiece.getY();

        Point[] points = maskTable.getPoints(currentPiece.getColor(), currentPiece.getRotation());
        int[] values = maskTable.getScores(currentPiece.getColor());

        int sum = 0;

        for (int i = 0; i < points.length; i++) {
            if (isSolid(x + points[i].x, y + points[i].y)) {
                sum += values[i];
            }
        }
        if (currentPiece.getColor() == PIECE_T || (ENABLEALLSPIN && currentPiece.getColor() != PIECE_O && currentPiece.getColor() != PIECE_I)) {
            if (sum < 11) {
                spinState = SPIN_NONE;
            } else {
                if (sum < 13) {
                    if (tries == 4 && ((oldRotation == 0 || oldRotation == 2) && (newRotation == 1 || newRotation == 3))) {
                        spinState = SPIN_FULL;
                    } else {
                        spinState = SPIN_MINI;
                    }
                } else {
                    spinState = SPIN_FULL;
                }
                evtSpin();
            }
        }
    }

    private void clearLine(int line) {
        int[] lineCopy = new int[STAGESIZEX];

        System.arraycopy(stage[line], 0, lineCopy, 0, STAGESIZEX);

        evtLineClear(line, lineCopy);

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
            stage[STAGESIZEY - zoneLines][j] = PIECE_ZONE;
        }
    }

    private int clearLines() {
        int linesCleared = 0;
        boolean isFull;
        for (int i = STAGESIZEY - 1; i >= 0; i--) {
            isFull = true;
            for (int j = 0; j < STAGESIZEX; j++) {
                if (stage[i][j] == PIECE_NONE || stage[i][j] == PIECE_NUKE) {
                    isFull = false;
                    break;
                }
            }
            if (isFull) {
                clearLine(i);
                i++;
                linesCleared++;
            }
        }

        return linesCleared;
    }

    private void clearLinesZone() {
        boolean isFull;
        for (int i = STAGESIZEY - 1 - zoneLines; i >= 0; i--) {
            isFull = true;
            for (int j = 0; j < STAGESIZEX; j++) {
                if (stage[i][j] == PIECE_NONE) {
                    isFull = false;
                    break;
                }
            }
            if (isFull) {
                zoneLines++;
                clearLineZone(i);
            }
        }
    }

    private void die() {
        gameState = STATE_DEAD;
        evtGameover();
    }

    private void hardDropPiece() {
        int lines = 0;
        while (!isColliding(currentPiece.getX(), currentPiece.getY() + lines + 1, currentPiece.getRotation())) {
            lines++;
        }
        if (lines > 0) {
            movePieceRelative(0, +lines);
            totalScore += (long) lines * scoreTable.getHardDrop();
        }
        lockPiece();
    }

    private void holdPiece() {
        if (!held) {
            counter = 0;
            calcCounterEnd();
            if (heldPiece == null) {
                heldPiece = new Piece(currentPiece.getColor(), DEFAULTSPAWNX, DEFAULTSPAWNY, DEFAULTSPAWNROTATION);
                makeNextPiece();
            } else {
                Piece temp = new Piece(currentPiece.getColor(), DEFAULTSPAWNX, DEFAULTSPAWNY, DEFAULTSPAWNROTATION);
                currentPiece = heldPiece;
                heldPiece = temp;
                calcCurrentPieceLowestPossiblePosition();
                checkBlockOut();
            }
            held = true;
        }
    }

    private void initGame() {
        gameState = STATE_ALIVE;
        stage = new int[STAGESIZEY][STAGESIZEX];
        for (int i = 0; i < STAGESIZEY; i++) {
            for (int j = 0; j < STAGESIZEX; j++) {
                stage[i][j] = PIECE_NONE;
            }
        }

        nextPieces = new Piece[NEXTPIECES + BAGSIZE];
        nextPiecesLeft = 0;

        heldPiece = null;
        held = false;

        garbageQueue = new Vector();
        garbageHole = garbageRandomizer.nextInt(STAGESIZEX);

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

        leftKey = false;
        rightKey = false;
        downKey = false;
        enqLeftKey = false;
        enqRightKey = false;
        enqDownKey = false;
        deqLeftKey = false;
        deqRightKey = false;
        deqDownKey = false;

        leftKeyTime = 0;
        rightKeyTime = 0;
        downKeyTime = 0;

        checkLeftKey = true;
        checkRightKey = true;
        checkDownKey = true;

        scheduleHardDrop = false;
        scheduleRotateCCW = false;
        scheduleRotateCW = false;
        scheduleRotate180 = false;
        scheduleHold = false;

        gravity.reset();
        garbageCap.reset();
        garbageMultiplier.reset();
        lockDelay.reset();

        makeNextPiece();
    }

    private boolean isColliding(int x, int y, int rotation) {
        Point[] temp;

        temp = getPoints(currentPiece.getColor(), rotation);
        for (int i = 0; i < PIECEPOINTS; i++) {
            Point point = temp[i];
            if (isInsideBounds(x + point.x, y + point.y)) {
                if (isSolid(x + point.x, y + point.y)) {
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

    private boolean isSolid(int x, int y) {
        return !isInsideBounds(x, y) || stage[y][x] != PIECE_NONE;
    }

    private boolean isTouchingGround() {
        return isColliding(currentPiece.getX(), currentPiece.getY() + 1, currentPiece.getRotation());
    }

    private void lockPiece() {
        Point[] temp;

        totalPiecesPlaced++;

        temp = getPoints(currentPiece.getColor(), currentPiece.getRotation());
        for (int i = 0; i < PIECEPOINTS; i++) {
            Point p = temp[i];
            stage[currentPiece.getY() + p.y][currentPiece.getX() + p.x] = currentPiece.getColor();
        }

        checkLockOut();

        if (zone) {
            clearLinesZone();
        } else {

            int linesCleared = clearLines();
            boolean nuke = false;

            if (ENABLENUKES) {
                Point[] points = getPoints(currentPiece.getColor(), currentPiece.getRotation());
                for (int i = 0; i < PIECEPOINTS; i++) {
                    Point p = points[i];
                    int x = currentPiece.getX() + p.x;
                    int y = currentPiece.getY() + p.y;
                    if (isInsideBounds(x, y + 1) && stage[y + 1][x] == PIECE_NUKE) {
                        clearLine(y + 1);
                        nuke = true;
                        break;
                    }
                }
            }

            if (linesCleared > 0) {
                combo++;

                if ((totalLinesCleared - totalGarbageReceived) * STAGESIZEX + totalGarbageReceived == totalPiecesPlaced * PIECEPOINTS) {
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

                totalScore += scoreTable.get(linesCleared, spinState) * (backToBack > 0 ? scoreTable.getB2bMulti() : 1) + (long) combo * scoreTable.getCombo();

                if (ENABLEALWAYSGARBAGE) {
                    tryToPutGarbage();
                }
                counter = 0;
                counterEnd = millisToTicks(LINECLEARDELAY);
            } else {
                if (nuke) {
                    combo++;
                } else {
                    combo = -1;
                    tryToPutGarbage();
                }

                counter = 0;
                counterEnd = millisToTicks(PIECESPAWNDELAY);
            }

            evtLockPiece(currentPiece, linesCleared, spinState, combo, backToBack, nuke);

        }

        makeNextPiece();
    }

    private void makeNextPiece() {
        while (nextPiecesLeft <= NEXTPIECES) {
            int[] bag = {PIECE_Z, PIECE_L, PIECE_O, PIECE_S, PIECE_I, PIECE_J, PIECE_T};
            shuffleArray(bag);
            for (int i = 0; i < BAGSIZE; i++) {
                nextPieces[nextPiecesLeft + i] = new Piece(bag[i], DEFAULTSPAWNX, DEFAULTSPAWNY, DEFAULTSPAWNROTATION);
            }
            nextPiecesLeft += BAGSIZE;
        }

        spawnPiece();

        checkBlockOut();
    }

    private int millisToTicks(int n) {
        return n / TICK_TIME;
    }

    private boolean movePiece(int newX, int newY, int newR) {
        if (!isColliding(newX, newY, newR)) {
            currentPiece.setX(newX);
            currentPiece.setY(newY);
            currentPiece.setRotation(newR);
            spinState = SPIN_NONE;
            calcCurrentPieceLowestPossiblePosition();
            counter = 0;
            calcCounterEnd();
            return true;
        }
        return false;
    }

    private boolean movePieceRelative(int xOffset, int yOffset) {
        return movePieceRelative(xOffset, yOffset, 0);
    }

    private boolean movePieceRelative(int xOffset, int yOffset, int rOffset) {
        return movePiece(currentPiece.getX() + xOffset, currentPiece.getY() + yOffset, currentPiece.getRotation() + rOffset);
    }

    private void putGarbageLine() {
        // topout
        for (int j = 0; j < STAGESIZEX; j++) {
            if (stage[0][j] != PIECE_NONE) {
                tryToDie();
                return;
            }
        }

        for (int i = 0; i < STAGESIZEY - 1; i++) {
            System.arraycopy(stage[i + 1], 0, stage[i], 0, STAGESIZEX);
        }

        for (int j = 0; j < STAGESIZEX; j++) {
            stage[STAGESIZEY - 1][j] = PIECE_GARBAGE;
        }

        stage[STAGESIZEY - 1][garbageHole] = ENABLENUKES ? PIECE_NUKE : PIECE_NONE;

        if (ENABLENUKES) {
            garbageHole = garbageRandomizer.nextInt(STAGESIZEX);
        }

        totalGarbageReceived++;
    }

    private void receiveGarbage(int n) {
        garbageQueue.addElement(new Integer(n));
    }

    private void removeOneGarbageFromQueue() {
        garbageQueue.setElementAt(new Integer(((Integer) garbageQueue.elementAt(0)).intValue() - 1), 0);
        if (((Integer) garbageQueue.elementAt(0)).intValue() == 0) {
            garbageQueue.removeElementAt(0);
            garbageHole = garbageRandomizer.nextInt(STAGESIZEX);
        }
    }

    private void rotatePiece(int d) {
        int oldRotation = currentPiece.getRotation();
        int newRotation = (currentPiece.getRotation() + d + 4) % 4;
        int piece = currentPiece.getColor();
        int state = SPINSTATES[oldRotation][newRotation];

        for (int tries = 0; tries < kickTable.maxTries(piece, state); tries++) {
            if (movePiece(
                currentPiece.getX() + kickTable.getX(piece, state, tries),
                currentPiece.getY() - kickTable.getY(piece, state, tries),
                newRotation)
            ) {
                checkSpin(tries, oldRotation, newRotation);
                return;
            }
        }
    }

    private void sendGarbage(int n) {
        int garbageRemaining = n;
        while (!garbageQueue.isEmpty() && garbageRemaining > 0) {
            removeOneGarbageFromQueue();
            garbageRemaining--;
        }

        if (garbageRemaining > 0) {
            totalGarbageSent += n;
            evtSendGarbage(n);
        }
    }

    private void shuffleArray(int[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int index = pieceRandomizer.nextInt(i + 1);
            int a = arr[index];
            arr[index] = arr[i];
            arr[i] = a;
        }
    }

    private void spawnPiece() {
        currentPiece = nextPieces[0];
        System.arraycopy(nextPieces, 1, nextPieces, 0, nextPieces.length - 1);
        nextPiecesLeft--;
        held = false;
        spinState = SPIN_NONE;
        calcCurrentPieceLowestPossiblePosition();
    }

    private void startZone() {
        zone = true;
        zoneLines = 0;
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
    }

    private void tick() {
        if (gameState >= STATE_PAUSED || gameState == STATE_DEAD) {
            return;
        }

        if (gameState == STATE_DELAY) {
            if (counter == counterEnd) {
                gameState = STATE_ALIVE;
                counter = 0;
                calcCounterEnd();
                tick();
                return;
            }
        } else {
            if (counter == counterEnd) {
                if (!movePieceRelative(0, +1)) {
                    lockPiece();
                }
                tick();
                return;
            }

            if (checkLeftKey) {
                checkLeftKey = false;
                if (leftKey) {
                    leftKeyTime++;
                    if (leftKeyTime == 1 || (leftKeyTime > millisToTicks(DAS) && (millisToTicks(ARR) == 0 || (leftKeyTime - millisToTicks(DAS)) % millisToTicks(ARR) == 0))) {
                        movePieceRelative(-1, 0);
                        tick();
                        return;
                    }
                } else {
                    leftKeyTime = 0;
                }
            }

            if (checkRightKey) {
                checkRightKey = false;
                if (rightKey) {
                    rightKeyTime++;
                    if (rightKeyTime == 1 || (rightKeyTime > millisToTicks(DAS) && (millisToTicks(ARR) == 0 || (rightKeyTime - millisToTicks(DAS)) % millisToTicks(ARR) == 0))) {
                        movePieceRelative(1, 0);
                        tick();
                        return;
                    }
                } else {
                    rightKeyTime = 0;
                }
            }

            if (checkDownKey) {
                checkDownKey = false;
                if (downKey) {
                    downKeyTime++;
                    if (downKeyTime == 1) {
                        movePieceRelative(0, 1);
                        tick();
                        return;
                    }
                } else {
                    downKeyTime = 0;
                }
            }

            if (scheduleHardDrop) {
                scheduleHardDrop = false;
                hardDropPiece();
                tick();
                return;
            }

            if (scheduleRotateCCW) {
                scheduleRotateCCW = false;
                rotatePiece(-1);
                tick();
                return;
            }

            if (scheduleRotateCW) {
                scheduleRotateCW = false;
                rotatePiece(1);
                tick();
                return;
            }

            if (scheduleRotate180) {
                scheduleRotate180 = false;
                rotatePiece(2);
                tick();
                return;
            }

            if (scheduleHold) {
                scheduleHold = false;
                holdPiece();
                tick();
                return;
            }
        }

        if (ticksPassed % TPS == TPS - 1) {
            gravity.tick(ticksPassed);
            garbageCap.tick(ticksPassed);
            garbageMultiplier.tick(ticksPassed);
            lockDelay.tick(ticksPassed);
        }

        counter++;
        ticksPassed++;

        checkLeftKey = true;
        checkRightKey = true;
        checkDownKey = true;

        if (deqLeftKey) {
            enqLeftKey = false;
            deqLeftKey = false;
        }
        if (deqRightKey) {
            enqRightKey = false;
            deqRightKey = false;
        }
        if (deqDownKey) {
            enqDownKey = false;
            deqDownKey = false;
        }
    }

    private void togglePause() {
        if (gameState < STATE_PAUSED) {
            gameState += STATE_PAUSED;
        } else {
            gameState -= STATE_PAUSED;
        }
    }

    private void tryToDie() {
        if (zone) {
            stopZone();
        } else {
            die();
        }
    }

    private void tryToPutGarbage() {
        for (int howMuch = 0; howMuch < garbageCap.getWorkingValue(); howMuch++) {
            if (garbageQueue.isEmpty()) {
                break;
            }
            putGarbageLine();

            removeOneGarbageFromQueue();
        }
    }

}
