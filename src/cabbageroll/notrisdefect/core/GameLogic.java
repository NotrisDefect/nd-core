package cabbageroll.notrisdefect.core;

import cabbageroll.notrisdefect.core.tables.GarbageTable;
import cabbageroll.notrisdefect.core.tables.KickTable;
import cabbageroll.notrisdefect.core.tables.MaskTable;
import cabbageroll.notrisdefect.core.tables.PieceTable;
import cabbageroll.notrisdefect.core.tables.ScoreTable;

import java.util.Random;
import java.util.Vector;

public abstract class GameLogic {

    public static final int BLOCK_NONE = 0;
    public static final int BLOCK_RED = 1;
    public static final int BLOCK_ORANGE = 2;
    public static final int BLOCK_YELLOW = 3;
    public static final int BLOCK_GREEN = 4;
    public static final int BLOCK_LIGHTBLUE = 5;
    public static final int BLOCK_BLUE = 6;
    public static final int BLOCK_PURPLE = 7;
    public static final int BLOCK_GRAY = 8;
    public static final int BLOCK_WHITE = 9;
    public static final int BLOCK_NUKE = 10;
    public static final int BLOCK_TIMELINE = 100;
    public static final int BLOCK_UNSTABLE = 200;
    public static final int BLOCK_HARD = 300;
    public static final int BLOCK_SQUEEZE = 1000;

    public static final int SPIN_NONE = 0;
    public static final int SPIN_MINI = 1;
    public static final int SPIN_FULL = 2;

    public static final int STATE_DEAD = 0;
    public static final int STATE_ALIVE = 1;
    public static final int STATE_ZONE = 2;
    public static final int STATE_DELAY = 10;
    public static final int STATE_PAUSED = 100;

    public static final int TPS = 20;
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
    private final GarbageTable garbageTable = GarbageTable.TEC;
    private final ScoreTable scoreTable = ScoreTable.NORMAL;
    private final MaskTable maskTable = MaskTable.SRS;
    private PieceTable pieceTable = PieceTable.GUIDELINE;
    private KickTable kickTable = KickTable.SRS_180;
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
    private UsablePiece currentPiece;
    private UsablePiece[] nextPieces;
    private int nextPiecesLeft;
    private UsablePiece heldPiece;
    private boolean held;
    private Vector garbageQueue;
    private int garbageHole;
    private int combo;

    // helper variables
    private int counter;
    private int counterEnd;
    private long zoneActivationTick;
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
    private int zoneCharge;
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

    public UsablePiece getCurrentPiece() {
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

    public UsablePiece getHeldPiece() {
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

    public UsablePiece[] getNextPieces() {
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

    public void lumines() {
        pieceTable = PieceTable.LUMINES;
        kickTable = KickTable.LUMINES;
        DEFAULTSPAWNX = 7;
        DEFAULTSPAWNY = 0;
        STAGESIZEX = 16;
        STAGESIZEY = 10;
        PLAYABLEROWS = 8;
        NEXTPIECES = 3;
    }

    protected abstract void evtGameover();

    protected abstract void evtLineClear(int row, int[] content);

    protected abstract void evtLockPiece(UsablePiece piece, int linesCleared, int spinState, int combo, int backToBack, boolean nuke);

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
        Point[] piece = currentPiece.getPoints();
        for (int i = 0; i < piece.length; i++) {
            Point point = piece[i];
            if (stage[point.y + currentPiece.getY()][point.x + currentPiece.getX()] != BLOCK_NONE) {
                tryToDie();
            }
        }
    }

    private void checkLockOut() {
        Point[] piece = currentPiece.getPoints();
        for (int i = 0; i < piece.length; i++) {
            Point point = piece[i];
            if (currentPiece.getY() + point.y >= STAGESIZEY - PLAYABLEROWS) {
                return;
            }
        }
        tryToDie();
    }

    private boolean checkPausedOrInvalid() {
        if (gameState == STATE_DEAD || gameState > STATE_PAUSED) {
            throw new IllegalStateException("Moved while dead or paused (" + gameState + ")");
        }

        return gameState > STATE_DELAY;
    }

    private void checkSpin(int tries, int oldRotation, int newRotation) {
        int x = currentPiece.getX();
        int y = currentPiece.getY();

        Point[] points = maskTable.getPoints(currentPiece.getOrdinal(), currentPiece.getRotation());
        int[] values = maskTable.getScores(currentPiece.getOrdinal());

        int sum = 0;

        for (int i = 0; i < points.length; i++) {
            if (isSolid(x + points[i].x, y + points[i].y)) {
                sum += values[i];
            }
        }
        if (pieceTable != PieceTable.GUIDELINE) {
            return;
        }

        if (currentPiece.getColors()[0] == BLOCK_PURPLE || (ENABLEALLSPIN && currentPiece.getColors()[0] != BLOCK_YELLOW && currentPiece.getColors()[0] != BLOCK_LIGHTBLUE)) {
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

        for (int i = 0; i < STAGESIZEX; i++) {
            stage[line][i] = BLOCK_NONE;
        }
    }

    private void clearLineZone(int line) {
        int gap = STAGESIZEY - (line + zoneLines);
        for (int i = line; i < line + gap; i++) {
            System.arraycopy(stage[i + 1], 0, stage[i], 0, STAGESIZEX);
        }
        for (int j = 0; j < STAGESIZEX; j++) {
            stage[STAGESIZEY - zoneLines][j] = BLOCK_WHITE;
        }
    }

    private int clearLines() {
        int currentLinesCleared = 0;
        int[] lines = new int[pieceTable.mostPiecePoints()];
        for (int i = STAGESIZEY - 1; i >= 0; i--) {
            if (isLineFull(i)) {
                clearLine(i);
                lines[currentLinesCleared++] = i;
            }
        }

        if (currentLinesCleared > 0) {
            int[] lines2 = new int[currentLinesCleared];
            System.arraycopy(lines, 0, lines2, 0, currentLinesCleared);
            semiCollapse(lines2);
            totalLinesCleared += currentLinesCleared;
        }

        return currentLinesCleared;
    }

    private void clearLinesZone() {
        for (int i = STAGESIZEY - 1 - zoneLines; i >= 0; i--) {
            if (isLineFull(i)) {
                zoneLines++;
                clearLineZone(i);
            }
        }
    }

    private void collapse() {
        //bandaid
        for (int h = 0; h < STAGESIZEY; h++) {
            for (int i = STAGESIZEY - 1; i > 0; i--) {
                for (int j = 0; j < STAGESIZEX; j++) {
                    if (stage[i][j] == BLOCK_NONE) {
                        stage[i][j] = stage[i - 1][j];
                        stage[i - 1][j] = BLOCK_NONE;
                    }
                }
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
                heldPiece = new UsablePiece(currentPiece.getOrdinal(), DEFAULTSPAWNX, DEFAULTSPAWNY, DEFAULTSPAWNROTATION);
                makeNextPiece();
            } else {
                UsablePiece temp = new UsablePiece(currentPiece.getOrdinal(), DEFAULTSPAWNX, DEFAULTSPAWNY, DEFAULTSPAWNROTATION);
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
                stage[i][j] = BLOCK_NONE;
            }
        }

        nextPieces = new UsablePiece[NEXTPIECES + pieceTable.amount()];
        nextPiecesLeft = 0;

        heldPiece = null;
        held = false;

        garbageQueue = new Vector();
        garbageHole = garbageRandomizer.nextInt(STAGESIZEX);

        zoneLines = 0;
        zoneCharge = 0;
        zoneActivationTick = -1;

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
        Point[] temp = currentPiece.getPoints(rotation);
        for (int i = 0; i < temp.length; i++) {
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

    private boolean isLineFull(int row) {
        for (int i = 0; i < STAGESIZEX; i++) {
            if (stage[row][i] == BLOCK_NONE || stage[row][i] == BLOCK_NUKE) {
                return false;
            }
        }
        return true;
    }

    private boolean isSolid(int x, int y) {
        return !isInsideBounds(x, y) || stage[y][x] != BLOCK_NONE;
    }

    private boolean isTouchingGround() {
        return isColliding(currentPiece.getX(), currentPiece.getY() + 1, currentPiece.getRotation());
    }

    private void lockPiece() {
        Point[] temp = currentPiece.getPoints();

        totalPiecesPlaced++;

        for (int i = 0; i < temp.length; i++) {
            Point p = temp[i];
            stage[currentPiece.getY() + p.y][currentPiece.getX() + p.x] = currentPiece.getColors()[i];
        }

        checkLockOut();

        counter = 0;
        counterEnd = millisToTicks(PIECESPAWNDELAY);

        if (gameState == STATE_ZONE) {
            clearLinesZone();
        } else if (pieceTable == PieceTable.LUMINES) {
            collapse();
        } else {
            int linesCleared = clearLines();
            boolean nuke = false;

            if (ENABLENUKES) {
                Point[] points = currentPiece.getPoints();
                for (int i = 0; i < temp.length; i++) {
                    Point p = points[i];
                    int x = currentPiece.getX() + p.x;
                    int y = currentPiece.getY() + p.y;
                    if (isInsideBounds(x, y + 1) && stage[y + 1][x] == BLOCK_NUKE) {
                        clearLine(y + 1);
                        nuke = true;
                        break;
                    }
                }
            }

            if (linesCleared > 0) {
                counterEnd += millisToTicks(LINECLEARDELAY);
                combo++;

                if ((totalLinesCleared - totalGarbageReceived) * STAGESIZEX + totalGarbageReceived == totalPiecesPlaced * temp.length) {
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

                // bandaid
                if (totalLinesCleared % 6 == 0) {
                    zoneCharge = Math.min(zoneCharge + 1, 4);
                }

            } else {
                if (nuke) {
                    combo++;
                } else {
                    combo = -1;
                    tryToPutGarbage();
                }
            }

            evtLockPiece(currentPiece, linesCleared, spinState, combo, backToBack, nuke);

        }

        gameState += STATE_DELAY;
        makeNextPiece();
    }

    private void makeNextPiece() {
        while (nextPiecesLeft <= NEXTPIECES) {
            int[] bag = new int[pieceTable.amount()];
            for (int i = 0; i < pieceTable.amount(); i++) {
                bag[i] = i;
            }
            shuffleArray(bag);
            for (int i = 0; i < pieceTable.amount(); i++) {
                nextPieces[nextPiecesLeft + i] = new UsablePiece(bag[i], DEFAULTSPAWNX, DEFAULTSPAWNY, DEFAULTSPAWNROTATION);
            }
            nextPiecesLeft += pieceTable.amount();
        }

        spawnPiece();

        checkBlockOut();
    }

    private int millisToTicks(int n) {
        return n * TPS / 1000;
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
            if (stage[0][j] != BLOCK_NONE) {
                tryToDie();
                return;
            }
        }

        for (int i = 0; i < STAGESIZEY - 1; i++) {
            System.arraycopy(stage[i + 1], 0, stage[i], 0, STAGESIZEX);
        }

        for (int j = 0; j < STAGESIZEX; j++) {
            stage[STAGESIZEY - 1][j] = BLOCK_GRAY;
        }

        stage[STAGESIZEY - 1][garbageHole] = ENABLENUKES ? BLOCK_NUKE : BLOCK_NONE;

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
        int piece = currentPiece.getOrdinal();
        int newRotation = (oldRotation + d + pieceTable.rotations(piece)) % pieceTable.rotations(piece);
        int state = kickTable.getState(oldRotation, newRotation);

        for (int tries = 0; tries < kickTable.maxTries(piece, state); tries++) {
            if (movePiece(currentPiece.getX() + kickTable.getX(piece, state, tries), currentPiece.getY() - kickTable.getY(piece, state, tries), newRotation)) {
                checkSpin(tries, oldRotation, newRotation);
                return;
            }
        }
    }

    private void semiCollapse(int[] emptyLines) {
        int gap = 0;

        for (int i = emptyLines[0]; i > emptyLines.length; i--) {
            if (gap < emptyLines.length && (i - gap) == emptyLines[gap]) {
                gap++;
                i++;
            } else {
                System.arraycopy(stage[i - gap], 0, stage[i], 0, STAGESIZEX);
            }
        }

        for (int i = 0; i < emptyLines.length; i++) {
            for (int j = 0; j < STAGESIZEX; j++) {
                stage[i][j] = BLOCK_NONE;
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
        if (zoneCharge > 0) {
            gameState = gameState - STATE_ALIVE + STATE_ZONE;
            zoneLines = 0;
            zoneActivationTick = ticksPassed;
        }
    }

    private void stopZone() {
        for (int i = 0; i < GarbageTable.ZONEBONUS[zoneCharge][zoneLines].length; i++) {
            sendGarbage(GarbageTable.ZONEBONUS[zoneCharge][zoneLines][i]);
        }
        zoneCharge = 0;
        gameState = gameState - STATE_ZONE + STATE_ALIVE;
        for (int i = 0; i < STAGESIZEY; i++) {
            for (int j = 0; j < STAGESIZEX; j++) {
                if (STAGESIZEY - zoneLines - 1 - i >= 0) {
                    stage[STAGESIZEY - 1 - i][j] = stage[STAGESIZEY - zoneLines - 1 - i][j];
                } else {
                    stage[STAGESIZEY - 1 - i][j] = BLOCK_NONE;
                }
            }
        }
    }

    private void tick() {
        if (gameState > STATE_PAUSED || gameState == STATE_DEAD) {
            return;
        }

        if (gameState > STATE_DELAY) {
            if (counter == counterEnd) {
                gameState -= STATE_DELAY;
                counter = 0;
                calcCounterEnd();
                tick();
                return;
            }
        }

        if (gameState == STATE_ZONE) {
            if (ticksPassed == zoneActivationTick + (long) zoneCharge * 5 * TPS) {
                stopZone();
            }
        } else if (gameState == STATE_ALIVE) {
            if (counter == counterEnd) {
                if (!movePieceRelative(0, +1)) {
                    lockPiece();
                }
                tick();
                return;
            }
        }

        if (gameState < STATE_DELAY) {
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

        counter++;
        ticksPassed++;

        if (ticksPassed % TPS == 0) {
            gravity.tick(ticksPassed);
            garbageCap.tick(ticksPassed);
            garbageMultiplier.tick(ticksPassed);
            lockDelay.tick(ticksPassed);
        }

        if (ticksPassed % 128 == 0 && pieceTable == PieceTable.LUMINES) {
            int squares = 0;
            for (int i = 0; i < STAGESIZEY - 1; i++) {
                for (int j = 0; j < STAGESIZEX - 1; j++) {
                    if (stage[i][j] != BLOCK_NONE &&
                        (stage[i][j] % BLOCK_TIMELINE == stage[i][j + 1] % BLOCK_TIMELINE) &&
                        (stage[i][j] % BLOCK_TIMELINE == stage[i + 1][j] % BLOCK_TIMELINE) &&
                        (stage[i][j] % BLOCK_TIMELINE == stage[i + 1][j + 1] % BLOCK_TIMELINE)
                    ) {
                        squares++;
                        stage[i][j] = stage[i][j] % BLOCK_TIMELINE + BLOCK_TIMELINE;
                        stage[i][j + 1] = stage[i][j + 1] % BLOCK_TIMELINE + BLOCK_TIMELINE;
                        stage[i + 1][j] = stage[i + 1][j] % BLOCK_TIMELINE + BLOCK_TIMELINE;
                        stage[i + 1][j + 1] = stage[i + 1][j + 1] % BLOCK_TIMELINE + BLOCK_TIMELINE;
                    }
                }
            }
            for (int i = 0; i < STAGESIZEY; i++) {
                for (int j = 0; j < STAGESIZEX; j++) {
                    if (BLOCK_RED + BLOCK_TIMELINE <= stage[i][j] && stage[i][j] <= BLOCK_WHITE + BLOCK_TIMELINE) {
                        stage[i][j] = BLOCK_NONE;
                    }
                }
            }
            collapse();
        }

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
        if (gameState > STATE_PAUSED) {
            gameState -= STATE_PAUSED;
        } else {
            gameState += STATE_PAUSED;
        }
    }

    private void tryToDie() {
        if (gameState == STATE_ZONE) {
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

    public class UsablePiece {

        private final int ordinal;
        private int x;
        private int y;
        private int rotation;

        public UsablePiece(int ordinal, int x, int y, int rotation) {
            this.ordinal = ordinal;
            this.x = x;
            this.y = y;
            this.rotation = rotation;
        }

        public int[] getColors() {
            return pieceTable.getPiece(ordinal, rotation).getColors();
        }

        public int getOrdinal() {
            return ordinal;
        }

        public Point[] getPoints() {
            return pieceTable.getPiece(ordinal, rotation).getPoints();
        }

        public Point[] getPoints(int rotation) {
            return pieceTable.getPiece(ordinal, rotation).getPoints();
        }

        public int getRotation() {
            return rotation;
        }

        public void setRotation(int rotation) {
            this.rotation = rotation;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

    }

}
