package cabbageroll.notrisdefect.core;

import cabbageroll.notrisdefect.core.tables.GarbageTable;
import cabbageroll.notrisdefect.core.tables.KickTable;
import cabbageroll.notrisdefect.core.tables.MaskTable;
import cabbageroll.notrisdefect.core.tables.PieceTable;
import cabbageroll.notrisdefect.core.tables.ScoreTable;

import java.awt.Point;
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

    public static final int SPIN_NONE = 0;
    public static final int SPIN_MINI = 1;
    public static final int SPIN_FULL = 2;

    public static final int STATE_DEAD = 0;
    public static final int STATE_ALIVE = 1;
    public static final int STATE_DELAY = 2;
    public static final int STATE_PAUSED = 3;

    public static final int TPS = 20;
    public static final int TICK_TIME = 50;
    private static final int[][] SPINSTATES = {
        {-1, 0, 8, 7},
        {1, -1, 2, 10},
        {9, 3, -1, 4},
        {6, 11, 5, -1},
    };
    private static final int BAGSIZE = 7;
    private static final int PIECEPOINTS = 4;

    private final PieceTable pieceTable = PieceTable.GUIDELINE;
    private final KickTable kickTable = KickTable.SRS_180;
    private final GarbageTable garbageTable = GarbageTable.TETRIO;
    private final ScoreTable scoreTable = ScoreTable.NORMAL;
    private final MaskTable maskTable = MaskTable.SRS;

    private final Property garbageCap = new Property(4d, 60 * TPS, 0.05d, 8d);
    private final Property garbageMultiplier = new Property(1d, 30 * TPS, 0.02d, 2d);
    private final Property gravity = new Property(1d, 30 * TPS, 0.1d, 20d) {
        public int getRealValue() {
            return millisToTicks((int) (Math.pow(getWorkingValue(), -1) * 1000));
        }
    };
    private final Property lockDelay = new Property(2000, 60 * TPS, -20, 500) {
        public int getRealValue() {
            return millisToTicks((int) getWorkingValue());
        }
    };

    protected int STAGESIZEX = 10;
    protected int STAGESIZEY = 40;
    protected int PLAYABLEROWS = 20;
    protected int NEXTPIECES = 5;

    protected int DEFAULTSPAWNX = 3;
    protected int DEFAULTSPAWNY = 18;
    protected int DEFAULTSPAWNROTATION = 0;

    protected int PIECESPAWNDELAY = 100;
    protected int LINECLEARDELAY = 500;
    protected int DAS = 200;
    protected int ARR = 50;

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
    private int leftKeyTime;
    private int rightKeyTime;

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

        hardDropPiece();
    }

    public void doHold() {
        if (checkPausedOrInvalid()) {
            return;
        }

        holdPiece();
    }

    public void doInstantSoftDrop() {
        if (checkPausedOrInvalid()) {
            return;
        }

        instantSoftDrop();
    }

    public void doMoveLeft() {
        if (checkPausedOrInvalid()) {
            return;
        }

        movePieceRelative(-1, 0);
    }

    public void doMoveRight() {
        if (checkPausedOrInvalid()) {
            return;
        }

        movePieceRelative(1, 0);
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

        downKey = true;
    }

    public void doPressLeft() {
        if (checkPausedOrInvalid()) {
            return;
        }

        leftKey = true;
    }

    public void doPressRight() {
        if (checkPausedOrInvalid()) {
            return;
        }

        rightKey = true;
    }

    public void doReleaseDown() {
        if (checkPausedOrInvalid()) {
            return;
        }

        downKey = false;
    }

    public void doReleaseLeft() {
        if (checkPausedOrInvalid()) {
            return;
        }

        leftKey = false;
    }

    public void doReleaseRight() {
        if (checkPausedOrInvalid()) {
            return;
        }

        rightKey = false;
    }

    public void doRotate180() {
        if (checkPausedOrInvalid()) {
            return;
        }

        rotatePiece(2);
    }

    public void doRotateCCW() {
        if (checkPausedOrInvalid()) {
            return;
        }

        rotatePiece(-1);
    }

    public void doRotateCW() {
        if (checkPausedOrInvalid()) {
            return;
        }

        rotatePiece(1);
    }

    public void doSoftDrop() {
        if (checkPausedOrInvalid()) {
            return;
        }

        movePieceRelative(0, 1);
        totalScore += Math.max(0, scoreTable.getSoftDrop());
    }

    public void doStart() {
        doStart(new Random().nextLong());
    }

    public void doStart(double seed) {
        if (gameState != STATE_DEAD) {
            throw new IllegalStateException("Started while already running");
        }

        pieceRandomizer = new Random((long) seed);
        garbageRandomizer = new Random((long) seed);
        initGame();
    }

    public void doTick() {
        tick();
    }

    public void doZone() {
        if (checkPausedOrInvalid()) {
            return;
        }

        startZone();
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

    public Property getLockDelay() {
        return lockDelay;
    }

    public int getLowestPossiblePosition() {
        return lowestPossiblePosition;
    }

    public MaskTable getMaskTable() {
        return maskTable;
    }

    public Piece[] getNextPieces() {
        return nextPieces;
    }

    public int getNextPiecesLeft() {
        return nextPiecesLeft;
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

    protected abstract void evtGameover();

    protected abstract void evtLineClear(int row, int[] content);

    protected abstract void evtLockPiece(Piece piece, int linesCleared, int spinState, int combo, int backToBack);

    protected abstract void evtPerfectClear();

    protected abstract void evtSendGarbage(int n);

    protected abstract void evtSpin();

    private void calcCounterEnd() {
        counterEnd = isTouchingGround() ? lockDelay.getRealValue() : (gravity.getRealValue() / (downKey ? 4 : 1));
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

    private void checkSpin(int tries) {
        int sum = 0;
        int x = currentPiece.getX();
        int y = currentPiece.getY();
        int rot = currentPiece.getRotation();
        Point[] points = maskTable.getPoints(currentPiece.getColor(), currentPiece.getRotation());
        int[] sums = maskTable.getScores(currentPiece.getColor());
        for (int i = 0; i < points.length; i++) {
            if (isSolid(x + points[i].x, y + points[i].y)) {
                sum += sums[i];
            }
        }
        switch (currentPiece.getColor()) {
            /*
            case PIECE_Z:
            case PIECE_L:
            case PIECE_S:
            case PIECE_J:
                if (tries > 0) {
                    if (sum < 0) {
                        spinState = SPIN_NONE;
                    } else {
                        if (sum < 2) {
                            spinState = SPIN_MINI;
                        } else {
                            spinState = SPIN_FULL;
                        }
                        evtSpin();
                    }
                }
                break;
             */
            case PIECE_T:
                if (sum < 11) {
                    spinState = SPIN_NONE;
                } else {
                    if (sum < 13) {
                        spinState = SPIN_MINI;
                    } else {
                        spinState = SPIN_FULL;
                    }
                    evtSpin();
                }
                break;
        }
    }

    private void clearLine(int line) {

        evtLineClear(line, (int[]) stage[line].clone());

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
                if (stage[i][j] == PIECE_NONE) {
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

    private void dasLeft() {
        int num = 0;
        int x = currentPiece.getX();
        while (!isColliding(x + num - 1, currentPiece.getY(), currentPiece.getRotation())) {
            num--;
        }
        movePieceRelative(num, 0);
    }

    private void dasRight() {
        int num = 0;
        int x = currentPiece.getX();
        while (!isColliding(x + num + 1, currentPiece.getY(), currentPiece.getRotation())) {
            num++;
        }
        movePieceRelative(num, 0);
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
            if (heldPiece == null) {
                heldPiece = new Piece(currentPiece.getColor(), DEFAULTSPAWNX, DEFAULTSPAWNY, DEFAULTSPAWNROTATION);
                makeNextPiece();
            } else {
                Piece temp = new Piece(currentPiece.getColor(), DEFAULTSPAWNX, DEFAULTSPAWNY, DEFAULTSPAWNROTATION);
                currentPiece = heldPiece;
                heldPiece = temp;
                calcCurrentPieceLowestPossiblePosition();
                counter = 0;
                calcCounterEnd();
                while (counter == counterEnd) {
                    if (!movePieceRelative(0, +1)) {
                        lockPiece();
                    } else {
                        calcCounterEnd();
                    }
                }
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

        leftKeyTime = 0;
        rightKeyTime = 0;

        gravity.reset();
        garbageCap.reset();
        garbageMultiplier.reset();
        lockDelay.reset();

        makeNextPiece();
    }

    private void instantSoftDrop() {
        int num = 0;
        int y = currentPiece.getY();
        while (!isColliding(currentPiece.getX(), y + num + 1, currentPiece.getRotation())) {
            num++;
        }
        totalScore += (long) scoreTable.getSoftDrop() * num;
        movePieceRelative(0, num);
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
                counter = 0;
                counterEnd = millisToTicks(LINECLEARDELAY);
            } else {
                combo = -1;
                tryToPutGarbage();
                counter = 0;
                counterEnd = millisToTicks(PIECESPAWNDELAY);
            }
            if (counter != counterEnd) {
                gameState = STATE_DELAY;
            }
            evtLockPiece(currentPiece, linesCleared, spinState, combo, backToBack);

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
            counter = 0;
            currentPiece.setX(newX);
            currentPiece.setY(newY);
            currentPiece.setRotation(newR);
            spinState = SPIN_NONE;
            calcCurrentPieceLowestPossiblePosition();
            calcCounterEnd();
            while (counter == counterEnd) {
                if (!movePieceRelative(0, +1)) {
                    lockPiece();
                } else {
                    calcCounterEnd();
                }
            }
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
            if (j == garbageHole) {
                stage[STAGESIZEY - 1][j] = PIECE_NONE;
            } else {
                stage[STAGESIZEY - 1][j] = PIECE_GARBAGE;
            }
        }

        totalGarbageReceived++;
    }

    private void receiveGarbage(int n) {
        garbageQueue.add(new Integer(n));
    }

    private void removeOneGarbageFromQueue() {
        garbageQueue.set(0, new Integer(((Integer) garbageQueue.get(0)).intValue() - 1));
        if (((Integer) garbageQueue.get(0)).intValue() == 0) {
            garbageQueue.remove(0);
            garbageHole = garbageRandomizer.nextInt(STAGESIZEX);
        }
    }

    private void rotatePiece(int d) {
        int oldRotation = currentPiece.getRotation();
        int newRotation = (currentPiece.getRotation() + d + 4) % 4;
        int piece = currentPiece.getColor();
        int state = SPINSTATES[oldRotation][newRotation];

        for (int tries = 0; tries < kickTable.maxTries(piece, state); tries++) {
            if (movePiece(currentPiece.getX() + kickTable.getX(piece, state, tries), currentPiece.getY() - kickTable.getY(piece, state, tries), newRotation)) {
                checkSpin(tries);
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
        counter = 0;
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
        if (gameState >= STATE_PAUSED) {
            return;
        }

        if (gameState == STATE_DEAD) {
            throw new IllegalStateException("Ticked while dead");
        }

        if (gameState == STATE_DELAY) {
            if (counter >= counterEnd) {
                gameState = STATE_ALIVE;
                counter = 0;
                calcCounterEnd();
                while (counter == counterEnd) {
                    if (!movePieceRelative(0, +1)) {
                        lockPiece();
                    } else {
                        calcCounterEnd();
                    }
                }
            }
        } else {
            if (leftKey) {
                if (leftKeyTime == 0 || leftKeyTime >= millisToTicks(DAS) && (leftKeyTime - millisToTicks(DAS)) % millisToTicks(ARR) == 0) {
                    movePieceRelative(-1, 0);
                }
                leftKeyTime++;
            } else {
                leftKeyTime = 0;
            }

            if (rightKey) {
                if (rightKeyTime == 0 || rightKeyTime >= millisToTicks(DAS) && (rightKeyTime - millisToTicks(DAS)) % millisToTicks(ARR) == 0) {
                    movePieceRelative(1, 0);
                }
                rightKeyTime++;
            } else {
                rightKeyTime = 0;
            }

            if (counter >= counterEnd && !movePieceRelative(0, +1)) {
                lockPiece();
            }
        }

        if (ticksPassed % TPS == TPS - 1) {
            gravity.tick(ticksPassed);
            garbageCap.tick(ticksPassed);
            garbageMultiplier.tick(ticksPassed);
            lockDelay.tick(ticksPassed);
        }

        ticksPassed++;
        counter++;
    }

    private int ticksToMillis(int n) {
        return n * TICK_TIME;
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
